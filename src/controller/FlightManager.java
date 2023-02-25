package controller;

import java.util.ArrayList;
import java.util.List;

import entity.Flight;
import entity.Client;
import entity.DateTime;

public class FlightManager {
    private ArrayList<Flight> flights;

    public FlightManager() {
        this.flights = new ArrayList<>();
    }

    public void initialiseFlights() {
        this.flights.add(
            new Flight(1, new DateTime(2023, 1, 1, 12, 30), 100.0f, 100, "Singapore", "Malaysia")
        );

        this.flights.add(
            new Flight(2, new DateTime(2023, 2, 1, 12, 30), 100.0f, 100, "Singapore", "Malaysia")
        );

        this.flights.add(
            new Flight(3, new DateTime(2023, 3, 1, 12, 30), 100.0f, 100, "Singapore", "Malaysia")
        );

        this.flights.add(
            new Flight(4, new DateTime(2023, 3, 1, 12, 30), 700.0f, 100, "Singapore", "Italy")
        );
    }

    /**
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
     * queries a flight from server by Id
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
     * modifies booking for both flight and client
     * @param client, client booking seats
     * @param flightId, flightId that is booking
     * @param seatsBooking, number of seats booking
     * @param isAdding, whether adding or not (as we are implementing decrement also)
     * @return status code for booking modification
     */
    public int modifyBookingsForFlight(Client client, int flightId, int seatsBooking, boolean isAdding) {
        Flight f = this.getFlightById(flightId);
        if (f == null) return 1;
        if (seatsBooking < 1) return 2;

        if (!f.reserveSeats(seatsBooking, isAdding)) return 3;
        if (!client.modifyBooking(flightId, seatsBooking, isAdding)) return 4;
        return 0;
    }
}
