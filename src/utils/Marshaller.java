package utils;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;

import common.FlightDetailsByIdReply;
import common.FlightsBySourceDestinationReply;
import entity.*;

public class Marshaller {

    public void marshalHeaderPacket(ByteBuffer buffer, int queryLength, int serviceType, int requestId) {
        buffer.putInt(queryLength);
        buffer.putInt(serviceType);
        buffer.putInt(requestId);
    }

    public int[] unmarshallHeaderPacket(byte[] byteArray) {
        /**
         * msgLength: 4 bytes, index 0-3
         * serviceType: 4 bytes, index 4-7
         * requestId: 4 bytes, index 8-11
         */

        byte[] queryLengthBytes = new byte[4], serviceTypeBytes = new byte[4], requestIdBytes = new byte[4];
        for(int i = 0; i < 4; i++) {
            queryLengthBytes[i] = byteArray[i];
            serviceTypeBytes[i] = byteArray[i + 4];
            requestIdBytes[i] = byteArray[i + 8];
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(queryLengthBytes);
        int queryLength = buffer.getInt();

        buffer = ByteBuffer.wrap(serviceTypeBytes);
        int serviceType = buffer.getInt();

        buffer = ByteBuffer.wrap(requestIdBytes);
        int requestId = buffer.getInt();

        return new int[] {queryLength, serviceType, requestId};
    }

    /**
     * Service 1: View Flights
     */
    public byte[] viewFlightsToByteArray(int serviceType, int requestId, String source, String destination) {
        /**
         * HEADER:
         * queryLength: 4 bytes
         * serviceType: 4 bytes
         * requestId: 4 bytes
         * 
         * sourceLength: 4 bytes
         * source: sourceLength bytes
         * destinationLength: 4 bytes
         * destination: destinationLength bytes
         */
        
        int queryLength = 4 + source.getBytes().length + 4 + destination.getBytes().length;
        int totalLength = 12 + queryLength;
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);

        //marshall fields to byte buffer
        this.marshalHeaderPacket(buffer, queryLength, serviceType, requestId);

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

    public String[] byteArrayToSourceAndDestination(ClientMessage clientMessage) {
        /**
         * ClientMessage (Client client, Int requestId, Int serviceType, Int queryLength, byte[] payload)
         * 
         * PAYLOAD:
         * sourceLength: 4 bytes
         * source: sourceLength bytes
         * destinationLength: 4 bytes
         * destination: destinationLength bytes
         */
        
        int queryLength = clientMessage.getQueryLength();
        byte[] payload = clientMessage.getPayload();

        byte[] query = new byte[queryLength];
        for(int i = 0; i < queryLength; i++) {
            query[i] = payload[i+12];
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

    public byte[] marshallFlightIds(int serviceType, int requestId, List<Integer> flightIds) {
        /**
         * 
         * HEADER:
         * queryLength: 4 bytes
         * serviceType: 4 bytes
         * requestId: 4 bytes
         * 
         * listLength: 4 bytes
         * flightIds: 4 bytes * listLength
         */

        int listLength = flightIds.size();
        int queryLength = 4 + 4 * listLength;
        int totalLength = 12 + queryLength;

        ByteBuffer buffer = ByteBuffer.allocate(totalLength);
        this.marshalHeaderPacket(buffer, queryLength, serviceType, requestId);
        
        buffer.putInt(listLength);
        for(int flightId : flightIds) {
            buffer.putInt(flightId);
        }

        return buffer.array();
    }

    public List<Integer> unmarshallFlightIds(int[] header, byte[] returnMessage) {
        /**
         * int[] header - {queryLength, serviceType, requestId}
         * 
         * PAYLOAD:
         * listLength: 4 bytes
         * flightIds: 4 bytes * listLength
         */

        int queryLength = header[0];

        byte[] payload = new byte[queryLength];
        for(int i = 0; i < queryLength; i++) {
            payload[i] = returnMessage[i+12];
        }

        ByteBuffer buffer = ByteBuffer.wrap(payload);
        int listLength = buffer.getInt();

        List<Integer> flightIds = new ArrayList<>();
        for(int i = 0; i < listLength; i++) {
            flightIds.add(buffer.getInt());
        }

        return flightIds;
    }

    /**
     * Service 2: Get Flight Info
     */
    public byte[] getFlightInfoToByteArray(int serviceType, int requestId, int flightId) {
        /**
         * HEADER:
         * queryLength: 4 bytes
         * serviceType: 4 bytes
         * requestId: 4 bytes
         * 
         * flightId: 4 bytes
         */

        int queryLength = 4;
        int totalLength = 12 + queryLength;
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);

        //marshall fields to byte buffer
        this.marshalHeaderPacket(buffer, queryLength, serviceType, requestId);

        buffer.putInt(flightId);

        return buffer.array();
    }

    public int byteArrayToFlightId(ClientMessage clientMessage) {
        /**
         * ClientMessage (Client client, Int requestId, Int serviceType, Int queryLength, byte[] payload)
         * 
         * PAYLOAD:
         * flightId: 4 bytes
         */

        int queryLength = clientMessage.getQueryLength();
        byte[] payload = clientMessage.getPayload();

        byte[] query = new byte[queryLength];
        for(int i = 0; i < queryLength; i++) {
            query[i] = payload[i+12];
        }

        ByteBuffer buffer = ByteBuffer.wrap(query);
        int flightId = buffer.getInt();

        return flightId;
    }

    /**
     * Service 3: Make Reservation
     */
    public byte[] makeReservationToByteArray(int serviceType, int requestId, int flightId, int numSeats) {
        /**
         * HEADER:
         * queryLength: 4 bytes
         * serviceType: 4 bytes
         * requestId: 4 bytes
         * 
         * flightId: 4 bytes
         */

        int queryLength = 4 + 4;
        int totalLength = 12 + queryLength;
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);

        //marshall fields to byte buffer
        this.marshalHeaderPacket(buffer, queryLength, serviceType, requestId);

        buffer.putInt(numSeats);

        return buffer.array();
    }

    public int[] byteArrayToReservationInfo(ClientMessage clientMessage) {
        /**
         * ClientMessage (Client client, Int requestId, Int serviceType, Int queryLength, byte[] payload)
         * 
         * PAYLOAD:
         * flightId: 4 bytes
         * numSeats: 4 bytes
         */

        int queryLength = clientMessage.getQueryLength();
        byte[] payload = clientMessage.getPayload();

        byte[] query = new byte[queryLength];
        for(int i = 0; i < queryLength; i++) {
            query[i] = payload[i+12];
        }

        ByteBuffer buffer = ByteBuffer.wrap(query);
        int flightId = buffer.getInt();
        int numSeats = buffer.getInt();

        return new int[] {flightId, numSeats};
    }

    /*
     * Service 4: Monitor flights
     */
    public byte[] monitoFlightsToByteArray(int serviceType, int requestId, int flightId, int monitorInterval) {
         /**
         * HEADER:
         * queryLength: 4 bytes
         * serviceType: 4 bytes
         * requestId: 4 bytes
         * 
         * flightId: 4 bytes
         * monitorInterval: 4 bytes
         */

        int msgLength = 4 + 4;
        int totalLength = 12 + msgLength;
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);

        //marshall fields to byte buffer
        this.marshalHeaderPacket(buffer, msgLength, serviceType, requestId);

        buffer.putInt(flightId);
        buffer.putInt(monitorInterval);

        return buffer.array();
    }

    public int[] byteArrayToMonitorInfo(ClientMessage clientMessage) {
        /**
         * ClientMessage (Client client, Int requestId, Int serviceType, Int queryLength, byte[] payload)
         * 
         * PAYLOAD:
         * flightId: 4 bytes
         * monitorInterval: 4 bytes
         */

        int queryLength = clientMessage.getQueryLength();
        byte[] payload = clientMessage.getPayload();

        byte[] query = new byte[queryLength];
        for(int i = 0; i < queryLength; i++) {
            query[i] = payload[i+12];
        }

        ByteBuffer buffer = ByteBuffer.wrap(query);
        int flightId = buffer.getInt();
        int monitorInterval = buffer.getInt();

        return new int[] {flightId, monitorInterval};
    }

    /*
     * Idempotent service
     * Service 5: Check user's booking history
     */
    public byte[] bookingHistoryToByteArray(int serviceType, int requestId) {
        /**
         * HEADER:
         * queryLength: 4 bytes
         * serviceType: 4 bytes
         * requestId: 4 bytes
         */

        int totalLength = 12;
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);

        //marshall fields to byte buffer
        this.marshalHeaderPacket(buffer, 0, serviceType, requestId);

        return buffer.array();
    }

    /*
     * Non-idempotent service
     * Service 6: Cancel booking
     */
    public byte[] cancelBookingToByteArray(int serviceType, int requestId, int flightId, int numSeats) {
        /**
         * HEADER:
         * queryLength: 4 bytes
         * serviceType: 4 bytes
         * requestId: 4 bytes
         * 
         * flightId: 4 bytes
         * numSeats: 4 bytes
         */

        int queryLength = 4 + 4;
        int totalLength = 12 + queryLength;
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);

        //marshall fields to byte buffer
        this.marshalHeaderPacket(buffer, queryLength, serviceType, requestId);

        buffer.putInt(flightId);
        buffer.putInt(numSeats);

        return buffer.array();
    }

    public int[] byteArrayToCancelInfo(ClientMessage clientMessage) {
        /**
         * ClientMessage (Client client, Int requestId, Int serviceType, Int queryLength, byte[] payload)
         * 
         * PAYLOAD:
         * flightId: 4 bytes
         * numSeats: 4 bytes
         */

        int queryLength = clientMessage.getQueryLength();
        byte[] payload = clientMessage.getPayload();

        byte[] query = new byte[queryLength];
        for(int i = 0; i < queryLength; i++) {
            query[i] = payload[i+12];
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
    public static byte[] flightToByteArray(Flight flight) {
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
    public static Flight byteArrayToFlight(byte[] bytes) {
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

    // marshal server reply for service 1, request flights by source and destination
    public static byte[] marshalFlightsBySourceDestinationReply(FlightsBySourceDestinationReply reply) {
        return new byte[]{};
    }

    // marshal server reply for service 2, request flights details by id
    public static byte[] marshalFlightDetailsByIdReply(FlightDetailsByIdReply reply) {
        return new byte[]{};
    }
}   
