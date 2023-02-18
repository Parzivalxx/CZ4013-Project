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

    public void sendMessage() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            try {
                System.out.println("Enter message to send to server:");
                String msg = sc.nextLine();

                // marshalling here
                this.buffer = msg.getBytes();
                DatagramPacket packet = new DatagramPacket(this.buffer, this.buffer.length, this.serverAddress,
                        this.PORT);
                socket.send(packet);

                socket.receive(packet);
                // unmarshalling here
                String response = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Server response: " + response);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        sc.close();
    }

    public static void printMenu(String[] options) {
        System.out.println("\n\nWelcome to SCSE Flight information system");
        System.out.println("Please select one of the following options by entering the number");
        for (String option : options) {
            System.out.println(option);
        }
        System.out.print("Choose your option : ");
    }

    public void queryFlights() {
        Scanner sc = new Scanner(System.in);
        System.out.println(
                "To query for flights based on flight routes, please enter the desired source and destination.");
        System.out.println("Please enter the desired source location.");
        try {
            String src = sc.nextLine();
            System.out.println("Please enter the desired destination location.");
            String dst = sc.nextLine();
            System.out.println(String.format("Here are the flights travelling from %s to %s", src, dst));
            System.out.println("blahblah");
        } catch (Exception ex) {
            System.out.println("Error: Input is not an integer.");
        }
    }

    public void queryFlightByID() {
        Scanner sc = new Scanner(System.in);
        System.out.println("To query for flights based on flight ID, please enter the FlightID.");
        System.out.println("Please enter the FlightID.");
        try {
            int flightID = sc.nextInt();
            System.out.println(String.format("Here are the details about Flight ID: %d", flightID));
            System.out.println("to be added");
        } catch (Exception ex) {
            System.out.println("Error: Input is not an integer.");
        }
    }

    public void makeReservation() {
        Scanner sc = new Scanner(System.in);
        System.out
                .println("To make reservation for flight, please enter the FlightID and the desired amount of seats.");
        System.out.println("Please enter the FlightID.");
        try {
            int flightID = sc.nextInt();
            System.out.println("Please enter the number of seats you wish to book.");
            int seatCount = sc.nextInt();
            System.out.println(String.format("%d seats booked for Flight ID: %d", seatCount, flightID));
        } catch (Exception ex) {
            System.out.println("Error: Input is not an integer.");
        }
    }

    public void runConsole() {
        String[] options = {
                "[1] Query flights based on flight routes",
                "[2] Query flight ID",
                "[3] Make reservation for flight",
                "[4] Exit",
        };
        Scanner sc = new Scanner(System.in);
        int userInput = 0;
        while (userInput != 4) {
            printMenu(options);
            try {
                userInput = sc.nextInt();
                System.out.println("You entered: " + userInput);
                boolean done = false;
                switch (userInput) {
                    case 1: // invoke flights query
                        queryFlights();
                        break;
                    case 2: // invoke flightID query
                        queryFlightByID();
                        break;
                    case 3: // invoke reservation
                        makeReservation();
                        break;
                    case 4: // invoke reservation
                        System.out.println("Exiting program...");
                        break;
                    default:
                        System.out.println("Error: Please select a valid option.");
                        break;
                }
            } catch (Exception ex) {
                System.out.println("Error: Input is not an integer.");
            }
        }
    }

    public static void main(String[] args) {
        Client client;

        try {
            client = new Client(new DatagramSocket(), InetAddress.getByName("localhost"));
            System.out.println("Client is running ...");
            // client.sendMessage();
            client.runConsole();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    // public static void main(String[] args) {
    // Client client;
    // Scanner sc = new Scanner(System.in);
    // try {
    // client = new Client(new DatagramSocket(), InetAddress.getByName("localhost"),
    // new Marshaller());
    // System.out.println("Client is running ...");

    // while(true) {
    // System.out.println("Choose a service:");
    // System.out.println("1. View a list of flights by specifying a source and
    // destination");
    // System.out.println("2. Get the departure time, airfare and seat availability
    // of a flight by specifying the flight ID");
    // System.out.println("3. Make a reservation by specifying the flight ID and the
    // number of seats to reserve");
    // System.out.println("4. Monitor updates to the seat availability of a flight
    // by specifying the flight ID");
    // System.out.println("5. Exit");

    // int choice = sc.nextInt();
    // switch(choice) {
    // case 1:
    // client.viewFlights(sc);
    // break;
    // case 2:
    // client.getFlightInfo(sc);
    // break;
    // case 3:
    // client.makeReservation(sc);
    // break;
    // case 4:
    // // client.monitorUpdates(sc);
    // break;
    // case 5:
    // sc.close();
    // System.out.println("Exiting...");
    // System.exit(0);
    // break;
    // default:
    // System.out.println("Invalid choice. Please try again.");
    // break;
    // }
    // }
    // } catch (IOException e) {
    // e.printStackTrace();
    // }

    // sc.close();
    // }
}
