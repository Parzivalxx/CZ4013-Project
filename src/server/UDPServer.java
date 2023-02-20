package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import entity.ClientRecord;
import java.util.HashMap;
import java.util.ArrayList;

class UDPServer {

    private DatagramSocket udpSocket;
    private static final int PORT = 5000;
    private int idCounter;
    private HashMap<ClientRecord, byte[]> clientRecords;
    private double failProb;

    private UDPServer(int port) throws SocketException {
        this.udpSocket = udpSocket;
        this.idCounter = 0;
        this.clientRecords = new HashMap<>();
        this.failProb = Constants.DEFAULT_SERVER_FAILURE_PROB;
    }

    public static void main(String[] args) throws Exception {
        boolean moreQuotes = true;

        int port = Constants.DEFAULT_SERVER_PORT;
        boolean handled;

        UDPServer udpServer = new UDPServer(port);

        FlightManager flightManager = new FlightManager();
        flightManager.initialiseDummyData();

        while (moreQuotes) {
            try {
                ClientMessage message = udpServer.receive();

                if (Constants.InvoSem.DEFAULT != Constants.InvoSem.AT_MOST_ONCE) handled = false;
                else handled = udpServer.checkAndSendOldResponse(message);
                if (!handled) {
                    int curID = udpServer.getID();
                    byte[] packageByte;
                    switch (message.serviceType) {
                        case Constants.SERVICE_GET_FLIGHT_DETAILS:
                            packageByte = ServerFlightDetails.handleResponse(curID, message.payload, flightManager);
                            packageByte = udpServer.addHeaders(packageByte, curID, message.serviceType);
                            udpServer.updateMemo(message, packageByte);
                            udpServer.send(packageByte, message.clientAddress, message.clientPort);
                            break;
                        case Constants.SERVICE_GET_FLIGHT_BY_SOURCE_DESTINATION:
                            packageByte = ServerFlightsBySourceDestination.handleResponse(curID, message.payload, flightManager);
                            packageByte = udpServer.addHeaders(packageByte, curID, message.serviceType);
                            udpServer.updateMemo(message, packageByte);
                            udpServer.send(packageByte, message.clientAddress, message.clientPort);
                            break;
                        case Constants.SERVICE_RESERVE_SEATS:
                            packageByte = ServerReserveSeats.handleResponse(curID, message.payload, flightManager);
                            packageByte = udpServer.addHeaders(packageByte, curID, message.serviceType);
                            udpServer.updateMemo(message, packageByte);
                            udpServer.send(packageByte, message.clientAddress, message.clientPort);
                            break;
                        case Constants.SERVICE_MONITOR_AVAILABILITY:
                            packageByte = ServerMonitorAvailability.handleResponse(curID, message.payload, flightManager,
                                    message.clientAddress, message.clientPort, udpServer.udpSocket);
                            packageByte = udpServer.addHeaders(packageByte, curID, message.serviceType);
                            udpServer.updateMemo(message, packageByte);
                            udpServer.send(packageByte, message.clientAddress, message.clientPort);
                            break;
                        case Constants.SERVICE_GET_FLIGHTS_BY_PRICE:
                            packageByte = ServerFlightsByPrice.handleResponse(curID, message.payload, flightManager);
                            packageByte = udpServer.addHeaders(packageByte, curID, message.serviceType);
                            udpServer.updateMemo(message, packageByte);
                            udpServer.send(packageByte, message.clientAddress, message.clientPort);
                            break;
                        case Constants.SERVICE_TOP_UP_ACCOUNT:
                            packageByte = ServerTopUpAccount.handleResponse(curID, message.payload, flightManager);
                            packageByte = udpServer.addHeaders(packageByte, curID, message.serviceType);
                            udpServer.updateMemo(message, packageByte);
                            udpServer.send(packageByte, message.clientAddress, message.clientPort);
                            break;
                        default:
                            System.out.println(Constants.UNRECOGNIZE_SVC_MSG);
                    }
                }
                System.out.println(Constants.SEPARATOR);

            } catch (IOException e) {
                e.printStackTrace();
                moreQuotes = false;
            }
        }
        udpServer.udpSocket.close();
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

    private boolean checkAndSendOldResponse(ClientMessage message) {
        ClientRecord record = new ClientRecord(message.clientAddress, message.clientPort, message.responseId);
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
