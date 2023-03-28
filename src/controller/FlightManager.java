package controller;

import java.util.ArrayList;
import java.util.List;

import entity.Flight;
import entity.Client;
import entity.DateTime;

public class FlightManager {
    private List<Flight> flights;

    public FlightManager() {
        this.flights = new ArrayList<>();
    }

    public void initialiseFlights() {
        this.flights.add(
            new Flight(1, new DateTime(2023, 1, 1, 12, 30), 100.00f, 100, "Singapore", "Malaysia")
        );

        this.flights.add(
            new Flight(2, new DateTime(2023, 2, 1, 12, 30), 100.00f, 100, "Singapore", "Malaysia")
        );

        this.flights.add(
            new Flight(3, new DateTime(2023, 3, 1, 12, 30), 100.0f, 100, "Singapore", "Malaysia")
        );

        this.flights.add(
            new Flight(4, new DateTime(2023, 3, 1, 12, 30), 700.0f, 100, "Singapore", "Italy")
        );
    }

    /**
     * Service 1: Get a list of flightIds that satisfy the source and destination
     * @param source, starting point of flight
     * @param destination, ending point of flight
     * @return a List of flightIds satisfying selected source and destination
     */
    public List<Integer> getFlightsBySourceDestination(String source, String destination) {
        List<Integer> flightIds = new ArrayList<>();

        for (Flight f : this.flights) {
            if (source.equalsIgnoreCase(f.getSource()) && destination.equalsIgnoreCase(f.getDestination())) {
                flightIds.add(f.getFlightId());
            }
        }
        return flightIds;
    }

    /**
     * Service 2: Get a flight object by flightId
     * @param flightId
     * @return queried flight
     */
    public Flight getFlightById(int flightId) {
        for (Flight f : this.flights) {
            if (flightId == f.getFlightId()) {
                return f;
            }
        }
        return null;
    }

    /**
     * Service 3: Modifies booking for both flight and client
     * @param client, client booking seats
     * @param flightId, flightId that is booking
     * @param seatsBooking, number of seats booking
     * @param isBooking, whether we are booking the seats (true)
     * @return int array, {status, number of seats left in flight}
     */
    public int[] modifyBookingsForFlight(Client client, int flightId, int seatsBooking, boolean isBooking) {
        System.out.println("FlightManager: modifyBookingsForFlight: " + client.printAddress() + ":" + client.getPort());
        Flight f = this.getFlightById(flightId);

        if (f == null) return new int[]{1, -1};

        if (seatsBooking < 1) return new int[]{2, -1};

        if (!f.reserveSeats(seatsBooking, isBooking)) return new int[]{3, -1};

        if (!client.modifyBooking(flightId, seatsBooking, isBooking)) {
            // if enter this block, means client is cancelling seats and not enough currently booked
            // need to reduce back the flight seats as operation unsuccessful
            f.reserveSeats(seatsBooking, !isBooking);
            return new int[] {4, -1};
        }

        return new int[]{0, f.getSeatAvailability()};
    }
}
