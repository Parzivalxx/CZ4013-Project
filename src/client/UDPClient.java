package client;

import java.io.IOException;
import java.net.*;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import utils.*;

public class UDPClient {

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private byte[] buffer;
    private final int PORT = 5000;
    private Marshaller marshaller;
    private int idCounter = 1;
    // Timeout properties
    private int timeOut;
    private int maxTries;
    private double failProb;
    private int invSem;

    public UDPClient(DatagramSocket socket, InetAddress serverAddress, Marshaller marshaller) {
        this.socket = socket;
        this.serverAddress = serverAddress;
        this.marshaller = marshaller;
        this.timeOut = Constants.DEFAULT_TIMEOUT;
        this.maxTries = Constants.DEFAULT_MAX_TRIES;
        this.failProb = Constants.DEFAULT_CLIENT_FAILURE_PROB;
        this.invSem = Constants.InvSem.DEFAULT;
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
        System.out.println("To query for flights based on flight routes, please enter the desired source and destination.");
        System.out.println("Please enter the desired source location.");
        try {
            String src = sc.next();
            System.out.println("Please enter the desired destination location.");
            String dst = sc.next();

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
            this.buffer = this.marshaller.viewFlightsToByteArray(serviceType, this.idCounter, src, dst);
            sendAndReceive(buffer);
            this.idCounter++;
//            System.out.println(String.format("Here are the flights travelling from %s to %s", src, dst));
//            System.out.println("insert server reply here");
        } catch (InputMismatchException e) {
            System.out.println(Constants.INVALID_INPUT_MSG);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sc.close();
    }

    public void queryFlightByID() {
        Scanner sc = new Scanner(System.in);
        System.out.println("To query for flights based on flight ID, please enter the FlightID.");
        System.out.println("Please enter the FlightID.");
        try {
            int flightId = sc.nextInt();

            /**
             * Client message:
             * length of message (int: 4 bytes) +
             * serviceType (int: 4 bytes) +
             * flightID (int: 4 bytes)
             */
            int serviceType = 2;
            this.buffer = this.marshaller.getFlightInfoToByteArray(serviceType, this.idCounter, flightId);
            sendAndReceive(buffer);
            this.idCounter++;
//            System.out.println(String.format("Here are the details about Flight ID: %d", flightId));
//            System.out.println("to be added");
        } catch (InputMismatchException e) {
            System.out.println(Constants.INVALID_INPUT_MSG);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sc.close();
    }

    public void makeReservation() {
        Scanner sc = new Scanner(System.in);
        System.out
                .println("To make reservation for flight, please enter the FlightID and the desired amount of seats.");
        System.out.println("Please enter the FlightID.");
        try {
            int flightId = sc.nextInt();
            System.out.println("Please enter the number of seats you wish to book.");
            int numSeats = sc.nextInt();

            /**
             * Client message:
             * length of message (int: 4 bytes) +
             * serviceType (int: 4 bytes) +
             * flightID (int: 4 bytes) +
             * numSeats (int: 4 bytes)
             */
            int serviceType = 3;
            this.buffer = this.marshaller.makeReservationToByteArray(serviceType, this.idCounter, flightId, numSeats);
            sendAndReceive(buffer);
            this.idCounter++;
            System.out.println(String.format("%d seats booked for Flight ID: %d", numSeats, flightId));
        } catch (InputMismatchException e) {
            System.out.println(Constants.INVALID_INPUT_MSG);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sc.close();
    }

    public void sendMessage(byte[] byteArray) {
        if (Math.random() < this.failProb) {
            System.out.println("Client dropping packet to simulate lost request.");
        } else{
            DatagramPacket packet = new DatagramPacket(this.buffer, this.buffer.length, this.serverAddress, this.PORT);
            try {
                this.socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void receiveMessage() throws IOException, TimeoutException {
        DatagramPacket packet = new DatagramPacket(this.buffer, this.buffer.length);
        socket.receive(packet);

        //unmarshalling here to be added
        // unmarshall header packet
        int[] header = this.marshaller.unmarshallHeaderPacket(packet.getData());
        int serviceType = header[1];

        // unmarshall query packet
        switch(serviceType) {
            case 1:
                List<Integer> flightIds = this.marshaller.unmarshallFlightIds(header, packet.getData());
                System.out.println("Flight IDs found:");
                for (int flightId : flightIds) {
                    System.out.println(flightId);
                }
                break;
            
        }

        // String message = new String(packet.getData(), 0, packet.getLength());
        // System.out.println(message);
    }

    public void sendAndReceive(byte[] message) throws IOException, TimeoutException {
        int tries = 0;
        System.out.println("Awaiting server reply...");
        socket.setSoTimeout(timeOut);
        do{
            try {
                this.sendMessage(message);
                this.receiveMessage();
                break;
            } catch (SocketTimeoutException e) {
                tries++;
                if (this.maxTries > 0 && tries == this.maxTries) {
                    System.out.println(String.format("Max tries of %d reached.", this.maxTries));
                    break;
                }
                if(this.invSem==0){
                    System.out.printf("Timeout %d, retrying...\n", tries);
                }
                else System.out.println("No reply from server");
            }
        } while (this.invSem != Constants.InvSem.NONE); // if using either at least once or at most once
    }

    public void runConsole() {
        String[] options = {
                "[1] Query flights based on flight routes",
                "[2] Query flight ID",
                "[3] Make reservation for flight",
                "[4] Exit",
        };

        int userInput = 0;
        Scanner sc = new Scanner(System.in);
        if (userInput != 4) {
            printMenu(options);
            try {
                userInput = sc.nextInt();
                System.out.println("You entered: " + userInput);
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
                    case 4: // WIP (register callback)
                        System.out.println("Exiting program...");
                        break;
                    default:
                        System.out.println("Error: Please select a valid option.");
                        break;
                }
            } catch (InputMismatchException e) {
                System.out.println(Constants.INVALID_INPUT_MSG);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sc.close();
    }

    public static void main(String[] args) {
        UDPClient client;
        try {
            client = new UDPClient(new DatagramSocket(), InetAddress.getByName("localhost"), new Marshaller());
            System.out.println("Client is running ...");
            // client.sendMessage();
            client.runConsole();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
