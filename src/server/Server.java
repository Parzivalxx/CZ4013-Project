package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import utils.*;

public class Server {
    
    private DatagramSocket socket;
    private byte[] buffer = new byte[256];  //assume max request length is 256 bytes
    private static final int PORT = 5000;
    private Marshaller marshaller;

    public Server(DatagramSocket socket, Marshaller marshaller) {
        this.socket = socket;
        this.marshaller = marshaller;
    }

    public static void main(String[] args) throws IOException {
        Server server;

        try {
            server = new Server(new DatagramSocket(PORT), new Marshaller());
            System.out.println("Server is listening on port " + PORT + "...");

            while(true) {
                DatagramPacket packet = new DatagramPacket(server.buffer, server.buffer.length);
                server.socket.receive(packet);

                // unmarshall header packet
                int[] header = server.marshaller.unmarshallHeaderPacket(packet.getData());
                int msgLength = header[0], serviceType = header[1];

                switch(serviceType) {
                    case 1:
                        // perform service 1
                        String[] srcAndDest = server.marshaller.byteArrayToSourceAndDestination(packet.getData(), msgLength);
                        System.out.println("Source: " + srcAndDest[0] + ", Destination: " + srcAndDest[1]);
                        break;
                    case 2:
                        // perform service 2
                        int flightId = server.marshaller.byteArrayToFlightId(packet.getData(), msgLength);
                        System.out.println("Flight ID: " + flightId);
                        break;
                    case 3:
                        // perform service 3
                        int[] reservationInfo = server.marshaller.byteArrayToReservationInfo(packet.getData(), msgLength);
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
    }
}
