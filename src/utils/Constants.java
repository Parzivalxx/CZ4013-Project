package utils;

public class Constants {
    public static final String SERVER_ADDRESS = "155.69.192.63";
    public static final int MAX_PACKET_SIZE = 256;

    public static final int DEFAULT_PORT = 5000;
    public static final boolean ENABLE_LOSS_OF_REQUEST = true;
    public static final double DEFAULT_CLIENT_FAILURE_PROB = 0.1;
    public static final double DEFAULT_SERVER_FAILURE_PROB = 0.1;
    public static final int DEFAULT_TIMEOUT = 1000;
    public static final int DEFAULT_MAX_TRIES = 3;

    public class InvSem {
        public static final int NONE = 0;
        public static final int AT_LEAST_ONCE = 1;
        public static final int AT_MOST_ONCE = 2;
        public static final int DEFAULT = AT_MOST_ONCE;
    }
    public static final String INVALID_INPUT_MSG = "Error: Invalid input!";

    // service 1
    public static final int FLIGHT_FOUND_STATUS = 1;
    public static final int FLIGHT_NOT_FOUND_STATUS = 0;
}
