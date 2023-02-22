package entity;

import entity.Client;

public class ClientMessage {
    private Client client;
    private int queryLength;
    private int serviceType;
    private int requestId;
    private byte[] payload;

    public ClientMessage(Client client, int queryLength, int serviceType, int requestId, byte[] payload) {
        this.client = client;
        this.queryLength = queryLength;
        this.serviceType = serviceType;
        this.requestId = requestId;
        this.payload = payload;
    }

    public Client getClient() {
        return this.client;
    }

    public int getQueryLength() {
        return this.queryLength;
    }

    public int getServiceType() {
        return this.serviceType;
    }

    public int getRequestId() {
        return this.requestId;
    }

    public byte[] getPayload() {
        return this.payload;
    }

}
