package server;

import common.FlightDetailsByIdReply;
import controller.FlightManager;
import entity.ClientMessage;
import entity.Flight;
import utils.Constants;
import utils.Marshaller;
import entity.DateTime;

public class FlightDetailsByIdHandler {
    public static byte[] handleResponse(int serverID, int serviceType, ClientMessage clientMessage, FlightManager flightManager) {
        
        int flightId = Marshaller.byteArrayToFlightId(clientMessage);
        System.out.println("Flight ID: " + flightId);

        FlightDetailsByIdReply reply;
        Flight f = flightManager.getFlightById(flightId);
        if (f != null) {
            reply = new FlightDetailsByIdReply(
                    serverID,
                    serviceType,
                    Constants.FLIGHT_FOUND_STATUS,
                    f.getFlightId(),
                    f.getDepartureTime(),
                    f.getSeatAvailability(),
                    f.getAirfare(),
                    f.getSource(),
                    f.getDestination()
            );
        } else {
            reply = new FlightDetailsByIdReply(
                    serverID,
                    serviceType,
                    Constants.FLIGHT_NOT_FOUND_STATUS,
                    -1,
                    new DateTime(0, 0, 0, 0, 0),
                    -1,
                    0,
                    "",
                    ""
            );
        }
        return Marshaller.marshalFlightDetailsByIdReply(reply);
    }
}
