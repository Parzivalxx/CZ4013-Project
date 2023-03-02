package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import entity.ClientMessage;
import entity.ClientRecord;
import entity.Flight;
import entity.Client;
import utils.Marshaller;
import utils.Constants;
import controller.FlightManager;
import controller.ClientManager;

class UDPServer {

    private byte[] buffer = new byte[Constants.MAX_PACKET_SIZE];
    private static final int PORT = Constants.DEFAULT_PORT;
    private DatagramSocket socket;
    private Marshaller marshaller;
    private int idCounter;
    private HashMap<ClientRecord, byte[]> clientRecords;
    private double failProb;
    private int invSem;

    private UDPServer(DatagramSocket socket, Marshaller marshaller) throws SocketException {
        this.socket = socket;
        this.marshaller = marshaller;
        this.idCounter = 0;
        this.clientRecords = new HashMap<>();
        this.failProb = Constants.DEFAULT_SERVER_FAILURE_PROB;
        this.invSem = Constants.InvSem.DEFAULT;
    }

    private int getID() {
        int currID = idCounter;
        idCounter++;
        return currID;
    }

    private boolean checkAndResend(Client client, int requestId) {
        ClientRecord record = new ClientRecord(client, requestId);
        boolean isKeyPresent = this.clientRecords.containsKey(record);
        System.out.println("duplicated request:"+ isKeyPresent);
        if (isKeyPresent) {
            byte[] packageByte = this.clientRecords.get(record);
            try {
                System.out.println("Duplicate request detected. Resending...");
                this.send(packageByte, client.getAddress(), client.getPort());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return isKeyPresent;
    }

    private void updateRecords(ClientMessage message, byte[] payload) {
        ClientRecord record = new ClientRecord(message.getClient(), message.getRequestId());
        this.clientRecords.put(record, payload);
    }

    public static void main(String[] args) {
        try {
            UDPServer udpServer = new UDPServer(new DatagramSocket(PORT), new Marshaller());
            System.out.println("Server is listening on port " + PORT + "...");

            FlightManager flightManager = new FlightManager();
            flightManager.initialiseFlights();

            ClientManager clientManager = new ClientManager();

            while (true) {
                DatagramPacket packet = new DatagramPacket(udpServer.buffer, udpServer.buffer.length);
                udpServer.socket.receive(packet);

                // unmarshall header packet
                int[] header = udpServer.marshaller.byteArrayToHeader(packet.getData());
                int queryLength = header[0], serviceType = header[1], requestId = header[2];

                Client client;
                client = clientManager.getClientByAddressAndPort(packet.getAddress(), packet.getPort());
                if (client == null) {
                    client = new Client(packet.getAddress(), packet.getPort());
                    clientManager.addClient(client);
                }

                boolean handled;
                if (udpServer.invSem == Constants.InvSem.AT_MOST_ONCE) {
                    handled = udpServer.checkAndResend(client, requestId);
                } else handled = false;
                
                ClientMessage clientMessage = new ClientMessage(client, queryLength, serviceType, requestId, packet.getData());

                if (!handled) {
                    byte[] reply;
                    int currID = udpServer.getID();
                    System.out.println("Request from: " + clientMessage.getClient().printAddress() + ":" + clientMessage.getClient().getPort());
                    System.out.println("Request ID: " + requestId);
                    switch (serviceType) {
                        case 1:
                            // perform service 1
                            //unmarshall the clientMessage to get the source and destination
                            String[] srcAndDest = udpServer.marshaller.byteArrayToSourceAndDestination(clientMessage);

                            //get flightIds from specified source and destination
                            List<Integer> flightIds = flightManager.getFlightsBySourceDestination(srcAndDest[0], srcAndDest[1]);

                            //marshall the return message
                            reply = udpServer.marshaller.flightIdsToByteArray(serviceType, requestId, flightIds);

                            //send the return message
                            udpServer.send(reply, client.getAddress(), client.getPort());
                            udpServer.updateRecords(clientMessage, reply);
                            break;

                        case 2:
                            // perform service 2
                            //unmarshall the clientMessage to get the flightId
                            int flightId = udpServer.marshaller.byteArrayToFlightId(clientMessage);

                            //get flight details from specified flightId
                            Flight flight = flightManager.getFlightById(flightId);

                            //marshall the return message
                            reply = udpServer.marshaller.flightToByteArray(serviceType, requestId, flight);

                            //send the return message
                            udpServer.send(reply, client.getAddress(), client.getPort());
                            udpServer.updateRecords(clientMessage, reply);
                            break;
                        case 3:
                            // perform service 3
                            //unmarshall the clientMessage to get the flightId and number of seats
                            int[] reservationInfo = udpServer.marshaller.byteArrayToReservationInfo(clientMessage);

                            //try to reserve seats for specified flightId
                            int result = flightManager.modifyBookingsForFlight(client, reservationInfo[0], reservationInfo[1], true);
                            String resultString = "";
                            switch(result){
                                case 0:
                                    resultString = "Reservation successful.";
                                    break;
                                case 1:
                                    resultString = "Reservation failed. Invalid flight ID.";
                                    break;
                                case 2:
                                    resultString = "Reservation failed. Invalid number of seats.";
                                    break;
                                case 3:
                                    resultString = "Reservation failed. Not enough seats available.";
                                    break;
                                case 4:
                                    resultString = "Reservation failed. Client cannot modify booking.";
                                    break;
                            }

                            //marshall the return message
                            reply = udpServer.marshaller.reservationResultToByteArray(serviceType, requestId, resultString);

                            //send the return message
                            udpServer.send(reply, client.getAddress(), client.getPort());
                            udpServer.updateRecords(clientMessage, reply);
                            break;

                        case 4:
                            // perform service 4
                            break;

                        case 5:
                            //perform service 5

                            //TODO: get client's reservations
                            Map<Integer, Integer> reservations = client.getPersonalBookings();

                            //TODO: marshall the client's reservations
                            reply = udpServer.marshaller.reservationHistoryToByteArray(serviceType, requestId, reservations);

                            //TODO: send the client's reservations
                            udpServer.send(reply, client.getAddress(), client.getPort());
                            udpServer.updateRecords(clientMessage, reply);
                            break;

                        case 6:
                            //perform service 6
                            //unmarshall the clientMessage to get the flightId and number of seats
                            int[] cancellationInfo = udpServer.marshaller.byteArrayToReservationInfo(clientMessage);

                            //try to cancel seats for specified flightId
                            int cancelResult = flightManager.modifyBookingsForFlight(client, cancellationInfo[0], cancellationInfo[1], false);

                            String cancelResultString = "";

                            switch(cancelResult){
                                case 0:
                                    cancelResultString = "Cancellation successful.";
                                    break;
                                case 1:
                                    cancelResultString = "Cancellation failed. Invalid flight ID.";
                                    break;
                                case 2:
                                    cancelResultString = "Cancellation failed. Invalid number of seats.";
                                    break;
                                case 3:
                                    cancelResultString = "Cancellation failed. Not enough seats booked.";
                                    break;
                                case 4:
                                    cancelResultString = "Cancellation failed. Client cannot modify booking.";
                                    break;
                            }

                            //marshall the return message
                            reply = udpServer.marshaller.reservationResultToByteArray(serviceType, requestId, cancelResultString);

                            //send the return message
                            udpServer.send(reply, client.getAddress(), client.getPort());
                            udpServer.updateRecords(clientMessage, reply);
                            break;
                            
                        default:
                            System.out.println("Invalid service type.");
                            break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    // private byte[] addHeaders(byte[] packageByte, int id, int serviceNum) throws IOException {
    //     List message = new ArrayList();
    //     Utils.append(message, id);
    //     Utils.append(message, serviceNum);
    //     byte[] header = Utils.byteUnboxing(message);

    //     ByteArrayOutputStream baos = new ByteArrayOutputStream();
    //     baos.write(header);
    //     baos.write(packageByte);

    //     return baos.toByteArray();
    // }

    // private int getID() {
    //     this.idCounter++;
    //     return this.idCounter;
    // }

    private void send(byte[] message, InetAddress clientAddress, int clientPort) throws IOException {
        if (Math.random() < this.failProb) {
            System.out.println("Server dropping packet to simulate lost request.");
        }
        else {
            DatagramPacket packet = new DatagramPacket(message, message.length, clientAddress, clientPort);
            this.socket.send(packet);
        }
    }


    // private ClientMessage receive() throws IOException {

    //     DatagramPacket receivePacket;
    //     byte[] header = new byte[4];
    //     DatagramPacket headerPacket = new DatagramPacket(header, header.length);
    //     this.udpSocket.receive(headerPacket);

    //     int messageLength = Utils.unmarshalInteger(headerPacket.getData(), 0);

    //     byte[] receiveData = new byte[messageLength];
    //     receivePacket = new DatagramPacket(receiveData, receiveData.length);
    //     this.udpSocket.receive(receivePacket);

    //     int responseID = Utils.unmarshalInteger(receivePacket.getData(), 0);
    //     int serviceType = Utils.unmarshalInteger(receivePacket.getData(), Constants.INT_SIZE);
    //     InetAddress clientAddress = receivePacket.getAddress();
    //     int clientPort = receivePacket.getPort();

    //     return new ClientMessage(
    //             responseID,
    //             Arrays.copyOfRange(receivePacket.getData(), 2 * Constants.INT_SIZE, messageLength),
    //             clientAddress,
    //             clientPort,
    //             serviceType,
    //             messageLength
    //     );
    // }
}
