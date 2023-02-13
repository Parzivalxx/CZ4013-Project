package entity;

public class Flight {
    private int flightId;
    private String source;
    private String destination;
    private DateTime departureTime;
    private float airfare;
    private int seatAvailability;

    /**
     * Constructor
     * @param flightId
     * @param source
     * @param destination
     * @param departureTime
     * @param airfare
     * @param seatAvailability
     */
    public Flight(int flightId, String source, String destination, DateTime departureTime, float airfare, int seatAvailability) {
        this.flightId = flightId;
        this.source = source;
        this.destination = destination;
        this.departureTime = departureTime;
        this.airfare = airfare;
        this.seatAvailability = seatAvailability;
    }

    /**
     * 
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

}
