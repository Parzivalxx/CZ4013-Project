package entity;

public class Flight {
    private int flightId;
    private DateTime departureTime;
    private float airfare;
    private int seatAvailability;
    private String source;
    private String destination;

    /**
     * Constructor
     *
     * @param flightId
     * @param departureTime
     * @param airfare
     * @param seatAvailability
     * @param source
     * @param destination
     */
    public Flight(int flightId, DateTime departureTime, float airfare, int seatAvailability, String source, String destination) {
        this.flightId = flightId;
        this.departureTime = departureTime;
        this.airfare = airfare;
        this.seatAvailability = seatAvailability;
        this.source = source;
        this.destination = destination;
    }

    /**
     * @return flightId
     */
    public int getFlightId() {
        return flightId;
    }

    /**
     * @param flightId the flightId to set
     */
    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    /**
     * @return the departureTime
     */
    public DateTime getDepartureTime() {
        return departureTime;
    }

    /**
     * @param departureTime the departureTime to set
     */
    public void setDepartureTime(DateTime departureTime) {
        this.departureTime = departureTime;
    }

    /**
     * @return the airfare
     */
    public float getAirfare() {
        return airfare;
    }

    /**
     * @param airfare the airfare to set
     */
    public void setAirfare(float airfare) {
        this.airfare = airfare;
    }

    /**
     * @return the seatAvailability
     */
    public int getSeatAvailability() {
        return seatAvailability;
    }

    /**
     * @param seatAvailability the seatAvailability to set
     */
    public void setSeatAvailability(int seatAvailability) {
        this.seatAvailability = seatAvailability;
    }

    /**
     * displays flight details
     */
    public void print() {
        String s =
                "FlightId: " + this.flightId + "\n" + "Source: " + this.source + "\n" + "Destination: " + this.destination + "\n" + "Departure Time: " + this.departureTime + "\n" + "Airfare: " + this.airfare + "\n" + "Seat Availability: " + this.seatAvailability + "\n";
        System.out.println(s);
    }

    public boolean reserveSeats(int seatsBooking, boolean isAdding) {
        if (isAdding) {
            seatAvailability += seatsBooking;
            return true;
        }
        if (seatAvailability < seatsBooking) return false;
        seatAvailability -= seatsBooking;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Flight) {
            Flight s = (Flight) obj;
            return flightId == s.flightId;
        }
        return false;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * @param destination the destination to set
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    
    @Override
    public String toString() {
        return "Flight ID: " + this.flightId + "\nSource: " + this.source + "\nDestination: " + this.destination + "\nDeparture Time: "
                + this.departureTime.toString() + "\nAirfare: " + this.airfare + "\nSeat Availability: " + this.seatAvailability;
    }
}
