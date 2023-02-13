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

    public static void printMenu(String[] options){
        System.out.println("\n\nWelcome to SCSE Flight information system");
        System.out.println("Please select one of the following options by entering the number");
        for (String option : options){
            System.out.println(option);
        }
        System.out.print("Choose your option : ");
    }

    public void queryFlights(){
        Scanner sc = new Scanner(System.in);
        System.out.println("To query for flights based on flight routes, please enter the desired source and destination.");
        System.out.println("Please enter the desired source location.");
        try {
            String src = sc.nextLine();
            System.out.println("Please enter the desired destination location.");
            String dst = sc.nextLine();
            System.out.println(String.format("Here are the flights travelling from %s to %s",src,dst));
            System.out.println("blahblah");
        }catch (Exception ex){
            System.out.println("Error: Input is not an integer.");
        }
    }

    public void queryFlightByID(){
        Scanner sc = new Scanner(System.in);
        System.out.println("To query for flights based on flight ID, please enter the FlightID.");
        System.out.println("Please enter the FlightID.");
        try {
            int flightID = sc.nextInt();
            System.out.println(String.format("Here are the details about Flight ID: %d",flightID));
            System.out.println("to be added");
        }catch (Exception ex){
            System.out.println("Error: Input is not an integer.");
        }
    }

    public void makeReservation(){
        Scanner sc = new Scanner(System.in);
        System.out.println("To make reservation for flight, please enter the FlightID and the desired amount of seats.");
        System.out.println("Please enter the FlightID.");
        try {
            int flightID = sc.nextInt();
            System.out.println("Please enter the number of seats you wish to book.");
            int seatCount = sc.nextInt();
            System.out.println(String.format("%d seats booked for Flight ID: %d",seatCount,flightID));
        }catch (Exception ex){
            System.out.println("Error: Input is not an integer.");
        }
    }

    public void runConsole(){
        String[] options = {
                "[1] Query flights based on flight routes",
                "[2] Query flight ID",
                "[3] Make reservation for flight",
                "[4] Exit",
        };
        Scanner sc = new Scanner(System.in);
        int userInput = 0;
        while(userInput!=4){
            printMenu(options);
            try {
                    userInput = sc.nextInt();
                    System.out.println("You entered: " + userInput);
                    boolean done = false;
                    switch (userInput) {
                        case 1: //invoke flights query
                            queryFlights();
                            break;
                        case 2: //invoke flightID query
                            queryFlightByID();
                            break;
                        case 3: //invoke reservation
                            makeReservation();
                            break;
                        case 4: //invoke reservation
                            System.out.println("Exiting program...");
                            break;
                        default:
                            System.out.println("Error: Please select a valid option.");
                            break;
                    }
                } catch (Exception ex){
                System.out.println("Error: Input is not an integer.");
            }
        }
    }

    public static void main(String[] args) {
        Client client;

        try {
            client = new Client(new DatagramSocket(), InetAddress.getByName("localhost"));
            System.out.println("Client is running ...");
//            client.sendMessage();
            client.runConsole();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
