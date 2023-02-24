package common;

import entity.DateTime;
import utils.Constants;

public class FlightDetailsByIdReply {
    private int serverId;
    private int serviceType;
    private int status;
    private int flightId;
    private DateTime departureTime;
    private int seatAvailability;
    private float airfare;
    private String source;
    private String destination;

    public FlightDetailsByIdReply() {
        this(0, 0, 0,  0, new DateTime(0, 0, 0, 0, 0), 0, 0, "", "");
    }

    public FlightDetailsByIdReply(int serverId, int serviceType, int status, int flightId, DateTime departureTime, int seatAvailability,
                              float airfare, String source, String destination) {
        this.serverId = serverId;
        this.serviceType = serviceType;
        this.status = status;
        this.flightId = flightId;
        this.departureTime = departureTime;
        this.seatAvailability = seatAvailability;
        this.airfare = airfare;
        this.source = source;
        this.destination = destination;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getServiceType() {
        return serviceType;
    }

    public void setServiceType(int serviceType) {
        this.serviceType = serviceType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    public DateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(DateTime departureTime) {
        this.departureTime = departureTime;
    }

    public int getSeatAvailability() {
        return seatAvailability;
    }

    public void setSeatAvailability(int availability) {
        this.seatAvailability = seatAvailability;
    }

    public float getAirfare() {
        return airfare;
    }

    public void setAirfare(float airfare) {
        this.airfare = airfare;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String generateOutputMessage() {
        if (this.status == Constants.FLIGHT_FOUND_STATUS) {
            String s = "Flight details: " + flightId + "\n";
            s += String.format("Departure time: Year %d, Month %d, Day %d, Hour %d, Minute %d\n",
                    departureTime.getYear(),
                    departureTime.getMonth(),
                    departureTime.getDay(),
                    departureTime.getHour(),
                    departureTime.getMinutes()
            );
            s += String.format("Airfare: %.2f, Seat availability: %d, Source: %s, Destination: %s\n",
                    airfare,
                    seatAvailability,
                    source,
                    destination
            );
            return s;
    } else if (this.status == Constants.FLIGHT_NOT_FOUND_STATUS) {
            return "Flight id: " + flightId + " not found\n";
        } else {
            return "Something went wrong. Status was invalid.";
        }
    }
}
