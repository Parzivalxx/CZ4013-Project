package client;

import java.io.IOException;
import java.net.*;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import utils.*;

public class UDPClient {

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private byte[] sendBuffer;
    private byte[] receiveBuffer;
    private final int PORT = Constants.DEFAULT_PORT;
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
        this.failProb = Constants.ENABLE_LOSS_OF_REQUEST? Constants.DEFAULT_CLIENT_FAILURE_PROB: 0;
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
        System.out.println(
                "To query for flights based on flight routes, please enter the desired source and destination.");
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
            this.sendBuffer = this.marshaller.viewFlightsToByteArray(serviceType, this.idCounter, src, dst);
            sendAndReceive();
            this.idCounter++;
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
            this.sendBuffer = this.marshaller.flightIdToByteArray(serviceType, this.idCounter, flightId);
            sendAndReceive();
            this.idCounter++;
        } catch (InputMismatchException e) {
            System.out.println(Constants.INVALID_INPUT_MSG);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sc.close();
    }

    public void makeReservation() {
        Scanner sc = new Scanner(System.in);
        System.out.println("To make reservation for flight, please enter the FlightID and the desired amount of seats.");
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
            this.sendBuffer = this.marshaller.makeReservationToByteArray(serviceType, this.idCounter, flightId, numSeats);
            sendAndReceive();
            this.idCounter++;
        } catch (InputMismatchException e) {
            System.out.println(Constants.INVALID_INPUT_MSG);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sc.close();
    }

    public void monitorSeatAvailability() {
        Scanner sc = new Scanner(System.in);
        System.out.println("To monitor seat availability for flight, please enter the FlightID and the length of monitor interval.");
        try {
            System.out.println("Please enter the FlightID.");
            int flightId = sc.nextInt();

            System.out.println("Please enter the length of monitor interval (in minutes).");
            int interval = sc.nextInt();

            /**
             * Client message:
             * length of message (int: 4 bytes) +
             * serviceType (int: 4 bytes) +
             * flightID (int: 4 bytes)
             */
            int serviceType = 4;
            this.sendBuffer = this.marshaller.monitorFlightsToByteArray(serviceType, this.idCounter, flightId, interval);
            sendAndReceiveCallback(interval);
            this.idCounter++;
        } catch (InputMismatchException e) {
            System.out.println(Constants.INVALID_INPUT_MSG);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sc.close();
    }

    public void checkReservationHistory() {
        /**
         * Client message:
         * length of message (int: 4 bytes) +
         * serviceType (int: 4 bytes) +
         * length of name (int: 4 bytes)
        */

        try {
            int serviceType = 5;
            this.sendBuffer = this.marshaller.checkReservationHistoryToByteArray(serviceType, this.idCounter);
            sendAndReceive();
            this.idCounter++;
        } catch (InputMismatchException e) {
            System.out.println(Constants.INVALID_INPUT_MSG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelReservations() {
        Scanner sc = new Scanner(System.in);
        System.out.println("To cancel reservations, please enter the FlightID and the number of seats you wish to cancel.");
        try {
            System.out.println("Please enter the FlightID.");
            int flightId = sc.nextInt();

            System.out.println("Please enter the number of seats you wish to cancel.");
            int numSeats = sc.nextInt();

            /**
             * Client message:
             * length of message (int: 4 bytes) +
             * serviceType (int: 4 bytes) +
             * flightID (int: 4 bytes) +
             * numSeats (int: 4 bytes)
             */
            int serviceType = 6;
            this.sendBuffer = this.marshaller.cancelReservationsToByteArray(serviceType, this.idCounter, flightId, numSeats);
            sendAndReceive();
            this.idCounter++;
        } catch (InputMismatchException e) {
            System.out.println(Constants.INVALID_INPUT_MSG);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sc.close();
    }

    public void sendAndReceive() throws IOException, TimeoutException {
        int tries = 0;
        System.out.println("Awaiting server reply...");
        socket.setSoTimeout(timeOut);
        do{
            try {
                this.sendMessage();
                this.receiveMessage();
                break;
            } catch (SocketTimeoutException e) {
                tries++;
                if (this.maxTries > 0 && tries == this.maxTries) {
                    System.out.println(String.format("Max tries of %d reached.", this.maxTries));
                    break;
                }
                if(this.invSem == 0){
                    System.out.printf("Timeout %d, retrying...\n", tries);
                }
                else System.out.println("No reply from server");
            }
        } while (this.invSem != Constants.InvSem.NONE); // if using either at least once or at most once
    }

    public void sendAndReceiveCallback(int interval) throws IOException, TimeoutException {
        int tries = 0;
        System.out.println("Awaiting server reply...");
        socket.setSoTimeout(timeOut);
        String resultString = "";
        do{
            try {
                this.sendMessage();
                resultString = this.receiveCallbackResult();
                break;
            } catch (SocketTimeoutException e) {
                tries++;
                if (this.maxTries > 0 && tries == this.maxTries) {
                    System.out.println(String.format("Max tries of %d reached.", this.maxTries));
                    break;
                }
                if(this.invSem == 0){
                    System.out.printf("Timeout %d, retrying...\n", tries);
                }
                else System.out.println("No reply from server");
            }
        } while (this.invSem != Constants.InvSem.NONE); // if using either at least once or at most once

        System.out.println(resultString);
        
        if(resultString.equals("Creation of callback successful.")){
            // if callback creation successful, wait for updates
            System.out.println("Monitoring...");
            long intervalExpiry = System.currentTimeMillis() + (interval * 1000 * 60);

            //set socket timeout till interval expiry
            socket.setSoTimeout((int) (intervalExpiry-System.currentTimeMillis()));

            while(System.currentTimeMillis() < intervalExpiry){
                try{
                    this.receiveMessage();
                } catch (SocketTimeoutException e) {
                    //set socket timeout back to original
                    socket.setSoTimeout(timeOut);
                    System.out.println("Monitor Interval has elapsed.");
                }
            }
        }
    }
    public String receiveCallbackResult() throws IOException, TimeoutException {
        this.receiveBuffer = new byte[Constants.MAX_PACKET_SIZE];
        DatagramPacket packet = new DatagramPacket(this.receiveBuffer, this.receiveBuffer.length);
        socket.receive(packet);

        //unmarshalling here to be added
        // unmarshall header packet
        int[] header = this.marshaller.byteArrayToHeader(packet.getData());

        // unmarshall query packet
        String callbackResult = this.marshaller.byteArrayToCallbackResult(header, packet.getData());
        return callbackResult;
    }

    public void sendMessage() {
        if (Math.random() < this.failProb) {
            System.out.println("Client dropping packet to simulate lost request.");
        } else{
            DatagramPacket packet = new DatagramPacket(this.sendBuffer, this.sendBuffer.length, this.serverAddress, this.PORT);
            try {
                this.socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void receiveMessage() throws IOException, TimeoutException {
        this.receiveBuffer = new byte[Constants.MAX_PACKET_SIZE];
        DatagramPacket packet = new DatagramPacket(this.receiveBuffer, this.receiveBuffer.length);
        socket.receive(packet);

        // unmarshall header packet
        int[] header = this.marshaller.byteArrayToHeader(packet.getData());
        int serviceType = header[1];

        // unmarshall query packet
        switch(serviceType) {
            case 1:
                List<Integer> flightIds = this.marshaller.byteArrayToFlightIds(header, packet.getData());
                System.out.println("Flight IDs found:");
                for (int flightId : flightIds) {
                    System.out.println(flightId);
                }
                break;

            case 2:
                String res = this.marshaller.byteArrayToFlight(header, packet.getData());
                System.out.println("Flight details:");
                System.out.println(res);
                break;

            case 3, 6:
                String reservationResult = this.marshaller.byteArrayToReservationResult(header, packet.getData());
                System.out.println(reservationResult);
                break;

            case 4:
                String callbackResult = this.marshaller.byteArrayToCallbackResult(header, packet.getData());
                System.out.println(callbackResult);
                //  add logic to handle when callback creation fails
                break;

            case 5:
                Map<Integer, Integer> reservationHistory = this.marshaller.byteArrayToReservationHistory(header, packet.getData());
                System.out.println("Reservation history:");
                for (Map.Entry<Integer, Integer> entry : reservationHistory.entrySet()) {
                    System.out.println(String.format("Flight ID: %d, Number of seats: %d", entry.getKey(), entry.getValue()));
                }
                break;

            case 7:
                String callbackUpdate = this.marshaller.byteArrayToCallbackUpdate(header, packet.getData());
                System.out.println(callbackUpdate);
                break;
        }       

    }

    public void runConsole() {
        String[] options = {
                "[1] Query flights based on flight routes",
                "[2] Query flight ID",
                "[3] Make reservation for flight",
                "[4] Monitor seat availability for a flight",
                "[5] Check your flight reservation history",
                "[6] Cancel a flight reservation",
                "[7] Exit",
        };
        Scanner sc = new Scanner(System.in);
        int userInput = 0;
        while (userInput != 7) {
            printMenu(options);
            try {

                userInput = sc.nextInt();
                System.out.println("You entered: " + userInput);
                switch (userInput) {
                    case 1:
                        queryFlights();
                        break;
                    case 2:
                        queryFlightByID();
                        break;
                    case 3: // invoke reservation
                        makeReservation();
                        break;
                    case 4:
                        monitorSeatAvailability();
                        break;
                    case 5:
                        checkReservationHistory();
                        break;
                    case 6:
                        cancelReservations();
                        break;
                    case 7: //
                        System.out.println("Exiting client...");
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
            client.runConsole();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
