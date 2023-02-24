package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import entity.ClientMessage;
import entity.ClientRecord;
import entity.Client;
import java.util.HashMap;

import utils.Marshaller;
import utils.Constants;
import controller.FlightManager;
import controller.ClientManager;

class UDPServer {

    private byte[] buffer = new byte[256];  //assume max request length is 256 bytes
    private static final int PORT = 5000;
    private DatagramSocket socket;
    private Marshaller marshaller;
    private static int idCounter;
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

    public static void main(String[] args) {
        try {
            UDPServer udpServer = new UDPServer(new DatagramSocket(PORT), new Marshaller());
            System.out.println("Server is listening on port " + PORT + "...");

            FlightManager flightManager = new FlightManager();
            flightManager.initialiseDummyData();

            ClientManager clientManager = new ClientManager();

            while (true) {
                DatagramPacket packet = new DatagramPacket(udpServer.buffer, udpServer.buffer.length);
                udpServer.socket.receive(packet);

                // unmarshall header packet
                int[] header = udpServer.marshaller.unmarshallHeaderPacket(packet.getData());
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
                            // add marshaller and logic to retrieve flight array
                            reply = FlightsBySourceDestinationHandler.handleResponse(currID, serviceType, clientMessage, flightManager);
                            System.out.println("replying client...");
                            udpServer.send(reply, client.getAddress(), client.getPort());
                            udpServer.updateRecords(clientMessage,reply);
                            break;
                        case 2:
                            // perform service 2
                            reply = FlightDetailsByIdHandler.handleResponse(currID, serviceType, clientMessage, flightManager);
                            System.out.println("replying client...");
                            udpServer.send(reply, client.getAddress(), client.getPort());
                            udpServer.updateRecords(clientMessage,reply);
                            break;
                        case 3:
                            // perform service 3
                            int[] reservationInfo = udpServer.marshaller.byteArrayToReservationInfo(clientMessage);
                            System.out.println("Request from: " + clientMessage.getClient().printAddress() + ":" + clientMessage.getClient().getPort());
                            System.out.println("Request ID: " + requestId);
                            System.out.println("Flight ID: " + reservationInfo[0] + ", Number of seats: " + reservationInfo[1]);
                            break;
                        case 4:
                            // perform service 4
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

    private static int getID() {
        int currID = idCounter;
        idCounter++;
        return currID;
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
}
