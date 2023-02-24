package controller;

import java.util.ArrayList;
import entity.Flight;
import entity.Client;
import entity.DateTime;
import java.util.List;
import java.util.ArrayList;

public class FlightManager {
    private ArrayList<Flight> flights;

    public FlightManager() {
        this.flights = new ArrayList<>();
    }

    /**
     * @param source, starting point of flight
     * @param destination, ending point of flight
     * @return an ArrayList of flightIds satisfying selected source and destination
     */
    public ArrayList<Integer> getFlightsBySourceDestination(String source, String destination) {
        ArrayList<Integer> flightIds = new ArrayList<>();
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

    public void initialiseDummyData() {
        DateTime dt1 = new DateTime(2023, 1, 1, 1, 11);
        DateTime dt2 = new DateTime(2023, 2, 2, 2, 22);
        DateTime dt3 = new DateTime(2023, 3, 3, 3, 33);
        this.flights.add(new Flight(1, dt1, 11.1f, 5, "SG", "MY"));
        this.flights.add(new Flight(2, dt2, 22.2f, 5, "TH", "AU"));
        this.flights.add(new Flight(3, dt3, 33.3f, 5, "VN", "UK"));
    }
}
