package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Client {

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private byte[] buffer;
    private final int PORT = 5000;

    public Client(DatagramSocket socket, InetAddress serverAddress) {
        this.socket = socket;
        this.serverAddress = serverAddress;
    }

    public void sendMessage() {
        Scanner sc = new Scanner(System.in);
        while(true) {
            try {
                System.out.println("Enter message to send to server:");
                String msg = sc.nextLine();

                //marshalling here
                this.buffer = msg.getBytes();
                DatagramPacket packet = new DatagramPacket(this.buffer, this.buffer.length, this.serverAddress, this.PORT);
                socket.send(packet);

                socket.receive(packet);
                //unmarshalling here
                String response = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Server response: " + response);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        sc.close();
    }

    public static void main(String[] args) {
        Client client;
        try {
            client = new Client(new DatagramSocket(), InetAddress.getByName("localhost"));
            System.out.println("Client is running ...");
            client.sendMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
