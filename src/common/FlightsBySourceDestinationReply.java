package common;

import utils.Constants;
import java.util.Arrays;

public class FlightsBySourceDestinationReply {
    private int serverId;
    private int serviceType;
    private String source;
    private String destination;
    private int status;
    private int[] flights;

    public FlightsBySourceDestinationReply() {
        this(0, 0, 0, "", "", new int[0]);
    }

    public FlightsBySourceDestinationReply(int serverId, int serviceType, int status, String source, String destination, int[] flights) {
        this.serverId = serverId;
        this.serviceType = serviceType;
        this.source = source;
        this.destination = destination;
        this.status = status;
        this.flights = flights;
    }

    public int getId() {
        return serverId;
    }

    public void setId(int serverId) {
        this.serverId = serverId;
    }

    public int getServiceType() {
        return serviceType;
    }

    public void setServiceType(int serviceType) {
        this.serviceType = serviceType;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int[] getFlights() {
        return flights;
    }

    public void setFlights(int[] flights) {
        this.flights = flights;
    }

    public String generateOutputMessage() {
        if (this.status == Constants.FLIGHT_FOUND_STATUS) {
            return "Flights found: " + Arrays.toString(this.flights);
        } else if (this.status == Constants.FLIGHT_NOT_FOUND_STATUS) {
            return "No flights found";
        } else {
            return "Something went wrong. Status was invalid.";
        }
    }
}
