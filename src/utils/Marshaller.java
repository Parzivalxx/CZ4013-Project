package utils;

import java.nio.ByteBuffer;

import entity.*;

public class Marshaller {

    public int[] unmarshallHeaderPacket(byte[] byteArray) {
        /**
         * msgLength: 4 bytes, index 0-3
         * serviceType: 4 bytes, index 4-7
         */

        byte[] msgLengthBytes = new byte[4], serviceTypeBytes = new byte[4];
        for(int i = 0; i < 4; i++) {
            msgLengthBytes[i] = byteArray[i];
            serviceTypeBytes[i] = byteArray[i + 4];
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(msgLengthBytes);
        int msgLength = buffer.getInt();

        buffer = ByteBuffer.wrap(serviceTypeBytes);
        int serviceType = buffer.getInt();

        return new int[] {msgLength, serviceType};
    }

    /**
     * Service 1: View Flights
     */
    public byte[] viewFlightsToByteArray(int serviceType, String source, String destination) {
        /**
         * msgLength: 4 bytes
         * serviceType: 4 bytes
         * sourceLength: 4 bytes
         * source: sourceLength bytes
         * destinationLength: 4 bytes
         * destination: destinationLength bytes
         */
        
        int msgLength = 4 + source.getBytes().length + 4 + destination.getBytes().length;
        int totalLength = 8 + msgLength;
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);

        //marshall fields to byte buffer
        buffer.putInt(msgLength);

        buffer.putInt(serviceType);

        buffer.putInt(source.getBytes().length);
        for(byte b : source.getBytes()) {
            buffer.put(b);
        }

        buffer.putInt(destination.getBytes().length);
        for(byte b : destination.getBytes()) {
            buffer.put(b);
        }

        return buffer.array();
    }

    public String[] byteArrayToSourceAndDestination(byte[] byteArr, int msgLength) {
        /**
         * sourceLength: 4 bytes
         * source: sourceLength bytes
         * destinationLength: 4 bytes
         * destination: destinationLength bytes
         */

        byte[] query = new byte[msgLength];
        for(int i = 0; i < msgLength; i++) {
            query[i] = byteArr[i+8];
        }

        ByteBuffer buffer = ByteBuffer.wrap(query);

        int sourceLength = buffer.getInt();
        byte[] sourceBytes = new byte[sourceLength];
        for(int i = 0; i < sourceLength; i++) {
            sourceBytes[i] = buffer.get();
        }
        String source = new String(sourceBytes);

        int destinationLength = buffer.getInt();
        byte[] destinationBytes = new byte[destinationLength];
        for(int i = 0; i < destinationLength; i++) {
            destinationBytes[i] = buffer.get();
        }
        String destination = new String(destinationBytes);

        return new String[] {source, destination};
    }

    /**
     * Service 2: Get Flight Info
     */
    public byte[] getFlightInfoToByteArray(int serviceType, int flightId) {
        /**
         * msgLength: 4 bytes
         * serviceType: 4 bytes
         * flightId: 4 bytes
         */

        int msgLength = 4;
        int totalLength = 8 + msgLength;
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);

        //marshall fields to byte buffer
        buffer.putInt(msgLength);
        buffer.putInt(serviceType);
        buffer.putInt(flightId);

        return buffer.array();
    }

    public int byteArrayToFlightId(byte[] data, int msgLength) {
        /**
         * flightId: 4 bytes
         */

        byte[] query = new byte[msgLength];
        for(int i = 0; i < msgLength; i++) {
            query[i] = data[i+8];
        }

        ByteBuffer buffer = ByteBuffer.wrap(query);
        int flightId = buffer.getInt();

        return flightId;
    }

    /**
     * Service 3: Make Reservation
     */
    public byte[] makeReservationToByteArray(int serviceType, int flightId, int numSeats) {
        /**
         * msgLength: 4 bytes
         * serviceType: 4 bytes
         * flightId: 4 bytes
         */

        int msgLength = 4 + 4;
        int totalLength = 8 + msgLength;
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);

        //marshall fields to byte buffer
        buffer.putInt(msgLength);
        buffer.putInt(serviceType);
        buffer.putInt(flightId);
        buffer.putInt(numSeats);

        return buffer.array();
    }

    public int[] byteArrayToReservationInfo(byte[] data, int msgLength) {
        /**
         * flightId: 4 bytes
         * numSeats: 4 bytes
         */

        byte[] query = new byte[msgLength];
        for(int i = 0; i < msgLength; i++) {
            query[i] = data[i+8];
        }

        ByteBuffer buffer = ByteBuffer.wrap(query);
        int flightId = buffer.getInt();
        int numSeats = buffer.getInt();

        return new int[] {flightId, numSeats};
    }

    /**
     * Convert flight to byte array
     * @param flight
     * @return
     */
    public byte[] flightToByteArray(Flight flight) {
        /**
         * flightId: 4 bytes
         * departureTime: 20 bytes
         * airfare: 4 bytes
         * seatAvailability: 4 bytes
         * source: 4 bytes + source.length bytes
         * destination: 4 bytes + destination.length bytes
         */

        int totalLength = 4 + 20 + 4 + 4 + 4 + flight.getSource().length() + 4 + flight.getDestination().length();
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);

        //marshall fields to byte buffer
        buffer.putInt(flight.getFlightId());
        buffer.putInt(flight.getDepartureTime().getYear());
        buffer.putInt(flight.getDepartureTime().getMonth());
        buffer.putInt(flight.getDepartureTime().getDay());
        buffer.putInt(flight.getDepartureTime().getHour());
        buffer.putInt(flight.getDepartureTime().getMinutes());
        buffer.putFloat(flight.getAirfare());
        buffer.putInt(flight.getSeatAvailability());

        byte[] sourceBytes = flight.getSource().getBytes();
        buffer.putInt(sourceBytes.length);
        for(byte b : sourceBytes) {
            buffer.put(b);
        }

        byte[] destBytes = flight.getDestination().getBytes();
        buffer.putInt(destBytes.length);
        for(byte b : destBytes) {
            buffer.put(b);
        }

        return buffer.array();
    }

    /**
     * Convert byte array to flight
     * @param bytes
     * @return
     */
    public Flight byteArrayToFlight(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        int flightId = buffer.getInt();
        int year = buffer.getInt();
        int month = buffer.getInt();
        int day = buffer.getInt();
        int hour = buffer.getInt();
        int minutes = buffer.getInt();
        float airfare = buffer.getFloat();
        int seatAvailability = buffer.getInt();

        int sourceLength = buffer.getInt();
        byte[] sourceBytes = new byte[sourceLength];
        for(int i = 0; i < sourceLength; i++) {
            sourceBytes[i] = buffer.get();
        }
        String source = new String(sourceBytes);

        int destLength = buffer.getInt();
        byte[] destBytes = new byte[destLength];
        for(int i = 0; i < destLength; i++) {
            destBytes[i] = buffer.get();
        }
        String destination = new String(destBytes);

        return new Flight(flightId, new DateTime(year, month, day, hour, minutes), airfare, seatAvailability, source, destination);
    }
}   
