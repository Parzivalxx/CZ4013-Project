package utils;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import entity.*;

public class Marshaller {

    public void headerToByteArray(ByteBuffer buffer, int queryLength, int serviceType, int requestId) {
        buffer.putInt(queryLength);
        buffer.putInt(serviceType);
        buffer.putInt(requestId);
    }

    public int[] byteArrayToHeader(byte[] byteArray) {
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
        this.headerToByteArray(buffer, queryLength, serviceType, requestId);

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

    public byte[] flightIdsToByteArray(int serviceType, int requestId, List<Integer> flightIds) {
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
        this.headerToByteArray(buffer, queryLength, serviceType, requestId);
        
        buffer.putInt(listLength);
        for(int flightId : flightIds) {
            buffer.putInt(flightId);
        }

        return buffer.array();
    }

    public List<Integer> byteArrayToFlightIds(int[] header, byte[] data) {
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
            payload[i] = data[i+12];
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
    public byte[] flightIdToByteArray(int serviceType, int requestId, int flightId) {
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
        this.headerToByteArray(buffer, queryLength, serviceType, requestId);

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

    public byte[] flightToByteArray(int serviceType, int requestId, String resultString) {
        /**
         * HEADER:
         * queryLength: 4 bytes
         * serviceType: 4 bytes
         * requestId: 4 bytes
         * 
         * resultStringLength: 4 bytes
         * resultString: resultStringLength bytes
         */

        int queryLength = 4 + resultString.getBytes().length;
        int totalLength = 12 + queryLength;

        ByteBuffer buffer = ByteBuffer.allocate(totalLength);
        this.headerToByteArray(buffer, queryLength, serviceType, requestId);

        buffer.putInt(resultString.getBytes().length);
        buffer.put(resultString.getBytes());

        return buffer.array();
    }

    public String byteArrayToFlight(int[] header, byte[] data) {
        /**
         * int[] header - {queryLength, serviceType, requestId}
         * 
         * PAYLOAD:
         * resultStringLength: 4 bytes
         * resultString: resultStringLength bytes
         */

        int queryLength = header[0];

        byte[] payload = new byte[queryLength];
        for(int i = 0; i < queryLength; i++) {
            payload[i] = data[i+12];
        }

        ByteBuffer buffer = ByteBuffer.wrap(payload);

        int acknowledgmentLength = buffer.getInt();
        byte[] acknowledgmentBytes = new byte[acknowledgmentLength];
        buffer.get(acknowledgmentBytes);
        
        return new String(acknowledgmentBytes);
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
         * numSeats: 4 bytes
         */
        int queryLength = 4 + 4;
        int totalLength = 12 + queryLength;
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);

        //marshall fields to byte buffer
        this.headerToByteArray(buffer, queryLength, serviceType, requestId);
        buffer.putInt(flightId);
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

    public byte[] reservationResultToByteArray(int serviceType, int requestId, String acknowledgment) {
        /**
         * HEADER:
         * queryLength: 4 bytes
         * serviceType: 4 bytes
         * requestId: 4 bytes
         * 
         * acknowledgment length: 4 bytes
         * acknowledgment: acknowledgment.length bytes
         */

        int queryLength = 4 + acknowledgment.getBytes().length;
        int totalLength = 12 + queryLength;
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);

        //marshall fields to byte buffer
        this.headerToByteArray(buffer, queryLength, serviceType, requestId);

        buffer.putInt(acknowledgment.getBytes().length);
        buffer.put(acknowledgment.getBytes());

        return buffer.array();
    }

    public String byteArrayToReservationResult(int[] header, byte[] data) {
        /**
         * int[] header - {queryLength, serviceType, requestId}
         * 
         * PAYLOAD:
         * acknowledgment length: 4 bytes
         * acknowledgment: acknowledgment.length bytes
         */

        int queryLength = header[0];

        byte[] payload = new byte[queryLength];
        for(int i = 0; i < queryLength; i++) {
            payload[i] = data[i+12];
        }

        ByteBuffer buffer = ByteBuffer.wrap(payload);

        int acknowledgmentLength = buffer.getInt();
        byte[] acknowledgmentBytes = new byte[acknowledgmentLength];
        buffer.get(acknowledgmentBytes);
        
        return new String(acknowledgmentBytes);
    }

    /*
     * Service 4: Monitor flights
     */
    public byte[] monitorFlightsToByteArray(int serviceType, int requestId, int flightId, int monitorInterval) {
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
        this.headerToByteArray(buffer, msgLength, serviceType, requestId);

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

    public byte[] callbackResultToByteArray(int serviceType, int requestId, String resultString) {
        /**
         * HEADER:
         * queryLength: 4 bytes
         * serviceType: 4 bytes
         * requestId: 4 bytes
         * 
         * resultString length: 4 bytes
         * resultString: resultString.length bytes
         */

        int queryLength = 4 + resultString.getBytes().length;
        int totalLength = 12 + queryLength;

        ByteBuffer buffer = ByteBuffer.allocate(totalLength);

        //marshall fields to byte buffer
        this.headerToByteArray(buffer, queryLength, serviceType, requestId);

        buffer.putInt(resultString.getBytes().length);
        buffer.put(resultString.getBytes());

        return buffer.array();
    }

    
    public String byteArrayToCallbackResult(int[] header, byte[] data) {
        /**
         * int[] header - {queryLength, serviceType, requestId}
         * 
         * PAYLOAD:
         * resultString length: 4 bytes
         * resultString: resultString.length bytes
         */

        int queryLength = header[0];

        byte[] payload = new byte[queryLength];
        for(int i = 0; i < queryLength; i++) {
            payload[i] = data[i+12];
        }

        ByteBuffer buffer = ByteBuffer.wrap(payload);

        int resultStringLength = buffer.getInt();
        byte[] resultStringBytes = new byte[resultStringLength];
        buffer.get(resultStringBytes);
        
        return new String(resultStringBytes);
    }

    
    public byte[] callbackUpdateToByteArray(int serviceType, int requestId, int flightId, int numSeatsLeft) {
        /**
         * HEADER:
         * queryLength: 4 bytes
         * serviceType: 4 bytes
         * requestId: 4 bytes
         * 
         * flightId: 4 bytes
         * numSeatsLeft: 4 bytes
         */

        int queryLength = 4 + 4;
        int totalLength = 12 + queryLength;

        ByteBuffer buffer = ByteBuffer.allocate(totalLength);

        //marshall fields to byte buffer
        this.headerToByteArray(buffer, queryLength, serviceType, requestId);

        buffer.putInt(flightId);
        buffer.putInt(numSeatsLeft);

        return buffer.array();
    }

    
    public String byteArrayToCallbackUpdate(int[] header, byte[] data) {
        /**
         * int[] header - {queryLength, serviceType, requestId}
         * 
         * PAYLOAD:
         * flightId: 4 bytes
         * numSeatsLeft: 4 bytes
         */

        int queryLength = header[0];

        byte[] payload = new byte[queryLength];
        for(int i = 0; i < queryLength; i++) {
            payload[i] = data[i+12];
        }

        ByteBuffer buffer = ByteBuffer.wrap(payload);

        int flightId = buffer.getInt();
        int numSeatsLeft = buffer.getInt();

        return "Flight " + flightId + " has " + numSeatsLeft + " seats left.";
    }

    /*
     * Idempotent service
     * Service 5: Check user's booking history
     */
    public byte[] checkReservationHistoryToByteArray(int serviceType, int requestId) {
        /**
         * HEADER:
         * queryLength: 4 bytes
         * serviceType: 4 bytes
         * requestId: 4 bytes
         */

        int totalLength = 12;
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);

        //marshall fields to byte buffer
        this.headerToByteArray(buffer, 0, serviceType, requestId);

        return buffer.array();
    }

    public byte[] reservationHistoryToByteArray(int serviceType, int requestId, Map<Integer, Integer> reservationHistory) {
        /**
         * HEADER:
         * queryLength: 4 bytes
         * serviceType: 4 bytes
         * requestId: 4 bytes
         * 
         * PAYLOAD:
         * flightId: 4 bytes
         * numSeats: 4 bytes
         * ...
         */

        int queryLength = reservationHistory.size() * 8;
        int totalLength = 12 + queryLength;
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);

        //marshall fields to byte buffer
        this.headerToByteArray(buffer, queryLength, serviceType, requestId);

        for(Map.Entry<Integer, Integer> entry : reservationHistory.entrySet()) {
            buffer.putInt(entry.getKey());
            buffer.putInt(entry.getValue());
        }

        return buffer.array();
    }

    public Map<Integer, Integer> byteArrayToReservationHistory(int[] header, byte[] data) {
        /**
         * int[] header - {queryLength, serviceType, requestId}
         * 
         * PAYLOAD:
         * flightId: 4 bytes
         * numSeats: 4 bytes
         * ...
         */

        int queryLength = header[0];

        byte[] payload = new byte[queryLength];
        for(int i = 0; i < queryLength; i++) {
            payload[i] = data[i+12];
        }

        ByteBuffer buffer = ByteBuffer.wrap(payload);
        
        Map<Integer, Integer> reservationHistory = new HashMap<Integer, Integer>();
        while(buffer.hasRemaining()) {
            int flightId = buffer.getInt();
            int numSeats = buffer.getInt();
            reservationHistory.put(flightId, numSeats);
        }

        return reservationHistory;
    }

    /*
     * Non-idempotent service
     * Service 6: Cancel booking
     */
    public byte[] cancelReservationsToByteArray(int serviceType, int requestId, int flightId, int numSeats) {
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
        this.headerToByteArray(buffer, queryLength, serviceType, requestId);

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
}   
