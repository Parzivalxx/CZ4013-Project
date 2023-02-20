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
import java.util.ArrayList;
import utils.Marshaller;
import utils.Constants;
import controller.FlightManager;
import controller.ClientManager;

class UDPServer {

    private DatagramSocket socket;
    private byte[] buffer = new byte[256];  //assume max request length is 256 bytes
    private static final int PORT = 5000;
    private int idCounter;
    private HashMap<ClientRecord, byte[]> clientRecords;
    private Marshaller marshaller;
    private double failProb;

    private UDPServer(DatagramSocket socket, Marshaller marshaller) throws SocketException {
        this.socket = socket;
        this.idCounter = 0;
        this.clientRecords = new HashMap<>();
        this.failProb = Constants.DEFAULT_SERVER_FAILURE_PROB;
    }

    public static void main(String[] args) throws Exception {
        try {
            UDPServer udpServer = new UDPServer(new DatagramSocket(PORT), new Marshaller());
            System.out.println("Server is listening on port " + PORT + "...");

            FlightManager flightManager = new FlightManager();
            flightManager.initialiseDummyData();

            ClientManager clientManager = new ClientManager();
            String invocationSemantic = args[0];

            while(true) {
                DatagramPacket packet = new DatagramPacket(udpServer.buffer, udpServer.buffer.length);
                udpServer.socket.receive(packet);

                // unmarshall header packet
                int[] header = udpServer.marshaller.unmarshallHeaderPacket(packet.getData());
                int msgLength = header[0], serviceType = header[1];

                Client client;
                client = clientManager.getClientByAddressAndPort(packet.getAddress(), packet.getPort());
                if (client == null) {
                    client = new Client(packet.getAddress(), packet.getPort());
                    clientManager.addClient(client);
                }

                boolean handled;

                // unmarshall responseID
                int responseID = 1; // dummy value
//                if (invocationSemantic.equals("atmostonce")) {
//                    handled = udpServer.checkandResend()
//                }

                // initialize Client object and add to clientManager

                switch(serviceType) {
                    case 1:
                        // perform service 1
                        String[] srcAndDest = udpServer.marshaller.byteArrayToSourceAndDestination(packet.getData(), msgLength);
                        System.out.println("Source: " + srcAndDest[0] + ", Destination: " + srcAndDest[1]);
                        break;
                    case 2:
                        // perform service 2
                        int flightId = udpServer.marshaller.byteArrayToFlightId(packet.getData(), msgLength);
                        System.out.println("Flight ID: " + flightId);
                        break;
                    case 3:
                        // perform service 3
                        int[] reservationInfo = udpServer.marshaller.byteArrayToReservationInfo(packet.getData(), msgLength);
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
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        udpServer.socket.close();
    }

    private byte[] addHeaders(byte[] packageByte, int id, int serviceNum) throws IOException {
        List message = new ArrayList();
        Utils.append(message, id);
        Utils.append(message, serviceNum);
        byte[] header = Utils.byteUnboxing(message);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(header);
        baos.write(packageByte);

        return baos.toByteArray();
    }

    private int getID() {
        this.idCounter++;
        return this.idCounter;
    }

    private void send(byte[] message, InetAddress clientAddress, int clientPort) throws IOException, InterruptedException {
        if (Math.random() < this.failProb) {
            System.out.println("Server dropping packet to simulate lost request");
        } else {
            byte[] header = Utils.marshal(message.length);
            DatagramPacket headerPacket = new DatagramPacket(header, header.length, clientAddress, clientPort);
            this.udpSocket.send(headerPacket);

            DatagramPacket sendPacket = new DatagramPacket(message, message.length, clientAddress, clientPort);
            this.udpSocket.send(sendPacket);
        }
    }


    private ClientMessage receive() throws IOException {

        DatagramPacket receivePacket;
        byte[] header = new byte[4];
        DatagramPacket headerPacket = new DatagramPacket(header, header.length);
        this.udpSocket.receive(headerPacket);

        int messageLength = Utils.unmarshalInteger(headerPacket.getData(), 0);

        byte[] receiveData = new byte[messageLength];
        receivePacket = new DatagramPacket(receiveData, receiveData.length);
        this.udpSocket.receive(receivePacket);

        int responseID = Utils.unmarshalInteger(receivePacket.getData(), 0);
        int serviceType = Utils.unmarshalInteger(receivePacket.getData(), Constants.INT_SIZE);
        InetAddress clientAddress = receivePacket.getAddress();
        int clientPort = receivePacket.getPort();

        return new ClientMessage(
                responseID,
                Arrays.copyOfRange(receivePacket.getData(), 2 * Constants.INT_SIZE, messageLength),
                clientAddress,
                clientPort,
                serviceType,
                messageLength
        );
    }

    private boolean checkandResend(ClientMessage message) {
        ClientRecord record = new ClientRecord(message.clientAddress, message.clientPort,message.responseId);
        boolean isKeyPresent = this.memo.containsKey(record);
        if (isKeyPresent) {
            byte[] packageByte = this.memo.get(record);
            try {
                System.out.println("Duplicate request detected. Resending...");
                this.send(packageByte, message.clientAddress, message.clientPort);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return isKeyPresent;
    }

    private void updateMemo(ClientMessage message, byte[] payload) {
        ClientRecord record = new ClientRecord(message.clientAddress, message.clientPort, message.responseId);
        this.memo.put(record, payload);
    }
}
