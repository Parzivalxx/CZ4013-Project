package entity;

import java.net.InetAddress;

public class Callback {
    private InetAddress clientAddress;
    private int clientPort;
    private int flightId;
    private long expiry;

    public Callback(InetAddress clientAddress, int clientPort, int flightId, long expiry) {
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.flightId = flightId;
        this.expiry = expiry;
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }

    public int getClientPort() {
        return clientPort;
    }

    public int getFlightId() {
        return flightId;
    }

    public long getExpiry() {
        return expiry;
    }
}
