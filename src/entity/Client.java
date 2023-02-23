package entity;

import java.net.InetAddress;
import java.util.HashMap;

public class Client {
    private InetAddress address;
    private int port;
    private HashMap<Integer, Integer> personalBookings;

    public Client(InetAddress address, int port) {
        this.address = address;
        this.port = port;
        this.personalBookings = new HashMap<>();
    }

    public InetAddress getAddress() {
        return this.address;
    };

    public String printAddress() {
        return this.address.getHostAddress();
    }

    public int getPort() {
        return this.port;
    };

    public HashMap<Integer, Integer> getPersonalBookings() {
        return personalBookings;
    }

    /**
     * modifies booking for client
     * @param flightId, flight modifying
     * @param seatsBooking, seats booking
     * @param isAdding, whether is adding or removing
     * @return. whether modification was successful
     */
    public boolean modifyBooking(int flightId, int seatsBooking, boolean isAdding) {
        if (!isAdding) {
            if (!personalBookings.containsKey(flightId)) return false;
            int currSeatsBooked = personalBookings.get(flightId);
            if (currSeatsBooked < seatsBooking) return false;
            if (currSeatsBooked == seatsBooking) {
                personalBookings.remove(flightId);
            } else {
                personalBookings.compute(flightId, (k, v) -> v - seatsBooking);
            }
            return true;
        }
        if (!personalBookings.containsKey(flightId)) {
            personalBookings.put(flightId, seatsBooking);
        } else {
            personalBookings.compute(flightId, (k, v) -> v + seatsBooking);
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Client) {
            Client s = (Client) obj;
            return address.equals(s.address) && port == s.port;
        }
        return false;
    }
}
