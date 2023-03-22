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
import entity.Callback;
import utils.Marshaller;
import utils.Constants;
import controller.FlightManager;
import controller.ClientManager;
import controller.CallbackManager;


class UDPServer {

    private byte[] buffer = new byte[Constants.MAX_PACKET_SIZE];
    private static final int PORT = Constants.DEFAULT_PORT;
    private DatagramSocket socket;
    private Marshaller marshaller;
    private HashMap<ClientRecord, byte[]> clientRecords;
    private double failProb;
    private int invSem;

    private UDPServer(DatagramSocket socket, Marshaller marshaller) throws SocketException {
        this.socket = socket;
        this.marshaller = marshaller;
        this.clientRecords = new HashMap<>();
        this.failProb = Constants.ENABLE_LOSS_OF_REQUEST? Constants.DEFAULT_SERVER_FAILURE_PROB : 0;

        this.invSem = Constants.InvSem.DEFAULT;
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
            CallbackManager callbackManager = new CallbackManager();

            while (true) {
                DatagramPacket packet = new DatagramPacket(udpServer.buffer, udpServer.buffer.length);
                udpServer.socket.receive(packet);

                // unmarshall header packet
                int[] header;
                int queryLength, serviceType, requestId;
                header = udpServer.marshaller.byteArrayToHeader(packet.getData());
                queryLength = header[0];
                serviceType = header[1];
                requestId = header[2];

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
                    String resultString = "";
                    System.out.println("Request from: " + clientMessage.getClient().printAddress() + ":" + clientMessage.getClient().getPort());
                    System.out.println("Request ID: " + requestId);
                    System.out.println("Service Type: " + serviceType);
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

                            resultString = flight == null ? "Invalid flight ID" : 
                                "Departure time: " + flight.getDepartureTime().toString() + ", airfare: " + flight.getAirfare() + 
                                ", seats left: " + flight.getSeatAvailability();


                            //marshall the return message
                            reply = udpServer.marshaller.flightToByteArray(serviceType, requestId, resultString);

                            //send the return message
                            udpServer.send(reply, client.getAddress(), client.getPort());
                            udpServer.updateRecords(clientMessage, reply);
                            break;
                        case 3:
                            // perform service 3
                            //unmarshall the clientMessage to get the flightId and number of seats
                            int[] reservationInfo = udpServer.marshaller.byteArrayToReservationInfo(clientMessage);

                            //try to reserve seats for specified flightId
                            int[] bookingResult = flightManager.modifyBookingsForFlight(client, reservationInfo[0], reservationInfo[1], true);
                            switch(bookingResult[0]){
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
                            // if booking was successful
                            if (bookingResult[0] == 0) {
                                // get the list of clients to update
                                List<Callback> updating = callbackManager.getCallbacksToUpdate(reservationInfo[0]);
                                for (Callback cb : updating) {
                                    //marshall the return message
                                    //change service type to 7
                                    serviceType = 7;
                                    reply = udpServer.marshaller.callbackUpdateToByteArray(serviceType, requestId,
                                            reservationInfo[0], bookingResult[1]);
                                    //send the return message
                                    udpServer.send(reply, cb.getClientAddress(), cb.getClientPort());
                                }
                            }
                            break;

                        case 4:
                            // perform service 4
                            // unmarshall the clientMessage to get the flightId and monitor interval
                            int[] monitorInfo = udpServer.marshaller.byteArrayToMonitorInfo(clientMessage);

                            if (flightManager.getFlightById(monitorInfo[0]) == null) {
                                resultString = "Creation of callback failed, flightId does not exist";
                            } else {
                                System.out.println("Interval: " + monitorInfo[1]);
                                callbackManager.registerCallback(client.getAddress(), client.getPort(),
                                        monitorInfo[0], monitorInfo[1]);
                                resultString = "Creation of callback successful.";
                            }
                            reply = udpServer.marshaller.callbackResultToByteArray(serviceType, requestId, resultString);

                            //send the return message
                            udpServer.send(reply, client.getAddress(), client.getPort());
                            udpServer.updateRecords(clientMessage, reply);
                            break;

                        case 5:
                            // perform service 5

                            // get client's reservations
                            Map<Integer, Integer> reservations = client.getPersonalBookings();

                            // marshall the client's reservations
                            reply = udpServer.marshaller.reservationHistoryToByteArray(serviceType, requestId, reservations);

                            // send the client's reservations
                            udpServer.send(reply, client.getAddress(), client.getPort());
                            udpServer.updateRecords(clientMessage, reply);
                            break;

                        case 6:
                            //perform service 6

                            // unmarshall the clientMessage to get the flightId and number of seats
                            int[] cancellationInfo = udpServer.marshaller.byteArrayToReservationInfo(clientMessage);

                            // try to cancel seats for specified flightId
                            int[] cancelResult = flightManager.modifyBookingsForFlight(client, cancellationInfo[0], cancellationInfo[1], false);

                            String cancelResultString = "";

                            switch(cancelResult[0]){
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

                            // marshall the return message
                            reply = udpServer.marshaller.reservationResultToByteArray(serviceType, requestId, cancelResultString);

                            // send reply
                            udpServer.send(reply, client.getAddress(), client.getPort());
                            udpServer.updateRecords(clientMessage, reply);
                            // if booking was successful
                            if (cancelResult[0] == 0) {
                                // get the list of clients to update
                                List<Callback> updating = callbackManager.getCallbacksToUpdate(cancellationInfo[0]);
                                for (Callback cb : updating) {
                                    //marshall the return message
                                    serviceType = 7;
                                    reply = udpServer.marshaller.callbackUpdateToByteArray(serviceType, requestId,
                                            cancellationInfo[0], cancelResult[1]);
                                    //send the return message
                                    udpServer.send(reply, cb.getClientAddress(), cb.getClientPort());
                                }
                            }
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

    private void send(byte[] message, InetAddress clientAddress, int clientPort) throws IOException {
        if (Math.random() < this.failProb) {
            System.out.println("Server dropping packet to simulate lost request.");
        }
        else {
            DatagramPacket packet = new DatagramPacket(message, message.length, clientAddress, clientPort);
            this.socket.send(packet);
        }
    }
}
