package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Server {
    
    private DatagramSocket socket;
    private byte[] buffer = new byte[256];
    private static final int PORT = 5000;

    public Server(DatagramSocket socket) {
        this.socket = socket;
    }

    public void receiveMessage() {
        while(true) {
            try {
                DatagramPacket packet = new DatagramPacket(this.buffer, this.buffer.length);
                socket.receive(packet);
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();

                //unmarshalling here
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Message from IP Address: " + clientAddress + " and port " + clientPort + " is: " + message);

                //marshalling here
                System.out.println("Sending message back to IP Address: " + clientAddress + " and port " + clientPort + "...");
                packet = new DatagramPacket(message.getBytes(), message.getBytes().length, clientAddress, clientPort);
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public static void main(String[] args) {
        Server server;
        try {
            //server listens to port 5000
            server = new Server(new DatagramSocket(PORT));
            System.out.println("Server is listening on port " + PORT + "...");
            server.receiveMessage();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
