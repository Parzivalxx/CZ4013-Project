package server;

import controller.FlightManager;
import entity.ClientMessage;
import utils.Constants;
import utils.Marshaller;
import utils.Helpers;
import common.FlightsBySourceDestinationReply;
import java.util.List;

public class FlightsBySourceDestinationHandler {
    public static byte[] handleResponse(int serverID,
                                        int serviceType,
                                        ClientMessage clientMessage,
                                        FlightManager flightManager) {
        String[] srcAndDest = Marshaller.byteArrayToSourceAndDestination(clientMessage);
        String src = srcAndDest[0], dest = srcAndDest[1];
        System.out.println("Source: " + src + ", Destination: " + dest);
        List<Integer> flightIds = flightManager.getFlightsBySourceDestination(src, dest);

        FlightsBySourceDestinationReply reply;
        if (flightIds.size() > 0) {
            int[] intFlightIds = Helpers.convertIntegers(flightIds);
            reply = new FlightsBySourceDestinationReply(serverID, serviceType, Constants.FLIGHT_FOUND_STATUS, src, dest, intFlightIds);
        } else {
            reply = new FlightsBySourceDestinationReply(serverID, serviceType, Constants.FLIGHT_NOT_FOUND_STATUS, src, dest, new int[]{});
        }
        return Marshaller.marshalFlightsBySourceDestinationReply(reply);
    }
}
