package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import utils.*;
import entity.*;

public class Client {

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private byte[] buffer;
    private final int PORT = 5000;

    public Client(DatagramSocket socket, InetAddress serverAddress) {
        this.socket = socket;
        this.serverAddress = serverAddress;
    }

    public void sendMessage(Marshaller marshaller) {
        Scanner sc = new Scanner(System.in);
        while(true) {
            try {
                System.out.println("=========== Enter flight details ===========");
                System.out.print("Flight ID: ");
                int flightId = sc.nextInt();
                System.out.print("Departure time (DD MM YYYY HH MM): ");
                int day = sc.nextInt();
                int month = sc.nextInt();
                int year = sc.nextInt();
                int hour = sc.nextInt();
                int minute = sc.nextInt();
                DateTime departureTime = new DateTime(year, month, day, hour, minute);
                System.out.print("Airfare: ");
                float airfare = sc.nextFloat();
                System.out.print("Seat availability: ");
                int seatAvailability = sc.nextInt();
                System.out.println("Source: ");
                String source = sc.next();
                System.out.println("Destination: ");
                String destination = sc.next();
                Flight flight = new Flight(flightId, departureTime, airfare, seatAvailability, source, destination);

                //marshalling here
                this.buffer = marshaller.flightToByteArray(flight);
                DatagramPacket packet = new DatagramPacket(this.buffer, this.buffer.length, this.serverAddress, this.PORT);
                socket.send(packet);

                // socket.receive(packet);
                // //unmarshalling here
                // String response = new String(packet.getData(), 0, packet.getLength());
                // System.out.println("Server response: " + response);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        sc.close();
    }

    public static void main(String[] args) {
        Client client;
        Marshaller marshaller = new Marshaller();
        try {
            client = new Client(new DatagramSocket(), InetAddress.getByName("localhost"));
            System.out.println("Client is running ...");
            client.sendMessage(marshaller);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
