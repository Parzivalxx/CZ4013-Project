package entity;

import entity.Client;
public class ClientMessage {
    private Client client;
    int responseId;
    byte[] payload;
    int serviceType;
    int msgLength;

    public ClientMessage(Client client, int responseId, byte[] payload, int serviceType, int msgLength) {
        this.client = client;
        this.responseId = responseId;
        this.payload = payload;
        this.serviceType = serviceType;
        this.msgLength = msgLength;
    }

}
