package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import utils.*;

public class Client {

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private byte[] buffer;
    private final int PORT = 5000;
    private Marshaller marshaller;

    public Client(DatagramSocket socket, InetAddress serverAddress, Marshaller marshaller) {
        this.socket = socket;
        this.serverAddress = serverAddress;
        this.marshaller = marshaller;
    }

    public void sendMessage(byte[] byteArray) {
        DatagramPacket packet = new DatagramPacket(this.buffer, this.buffer.length, this.serverAddress, this.PORT);
        try {
            this.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void viewFlights(Scanner sc) {
        System.out.println("Enter source: ");
        String source = sc.next();
        System.out.println("Enter destination: ");
        String destination = sc.next();

        /**
         * Client message: 
         * length of message (int: 4 bytes) + 
         * serviceType (int: 4 bytes) + 
         * length of source (int: 4 bytes) + 
         * source + 
         * length of destination (int: 4 bytes) + 
         * destination
         */
        int serviceType = 1;
        this.buffer = this.marshaller.viewFlightsToByteArray(serviceType, source, destination);
        sendMessage(buffer);
    }

    public void getFlightInfo(Scanner sc) {
        System.out.println("Enter flight ID: ");
        int flightId = sc.nextInt();

        /**
         * Client message: 
         * length of message (int: 4 bytes) + 
         * serviceType (int: 4 bytes) + 
         * flightID (int: 4 bytes)
         */
        int serviceType = 2;
        this.buffer = this.marshaller.getFlightInfoToByteArray(serviceType, flightId);
        sendMessage(buffer);
    }

    public void makeReservation(Scanner sc) {
        System.out.println("Enter flight ID: ");
        int flightId = sc.nextInt();
        System.out.println("Enter number of seats: ");
        int numSeats = sc.nextInt();

        /**
         * Client message: 
         * length of message (int: 4 bytes) + 
         * serviceType (int: 4 bytes) + 
         * flightID (int: 4 bytes) + 
         * numSeats (int: 4 bytes)
         */
        int serviceType = 3;
        this.buffer = this.marshaller.makeReservationToByteArray(serviceType, flightId, numSeats);
        sendMessage(buffer);
    }

    public static void main(String[] args) {
        Client client;
        Scanner sc = new Scanner(System.in);
        try {
            client = new Client(new DatagramSocket(), InetAddress.getByName("localhost"), new Marshaller());
            System.out.println("Client is running ...");
            
            while(true) {
                System.out.println("Choose a service:");
                System.out.println("1. View a list of flights by specifying a source and destination");
                System.out.println("2. Get the departure time, airfare and seat availability of a flight by specifying the flight ID");
                System.out.println("3. Make a reservation by specifying the flight ID and the number of seats to reserve");
                System.out.println("4. Monitor updates to the seat availability of a flight by specifying the flight ID");
                System.out.println("5. Exit");

                int choice = sc.nextInt();
                switch(choice) {
                    case 1:
                        client.viewFlights(sc);
                        break;
                    case 2:
                        client.getFlightInfo(sc);
                        break;
                    case 3:
                        client.makeReservation(sc);
                        break;
                    case 4:
                        // client.monitorUpdates(sc);
                        break;
                    case 5:
                        sc.close();
                        System.out.println("Exiting...");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        sc.close();
    }
}
