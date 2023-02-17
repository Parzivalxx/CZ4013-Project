package utils;

import java.nio.ByteBuffer;

import entity.*;

public class Marshaller {
    /**
     * Java stores data in big endian format: Most significant byte first
     */

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
