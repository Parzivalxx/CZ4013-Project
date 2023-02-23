package entity;

import entity.Client;

public class ClientRecord {
    private Client client;
    private int id;

    public ClientRecord(Client client, int id) {
        this.client = client;
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClientRecord) {
            ClientRecord s = (ClientRecord) obj;
            return client.equals(s.client) && id == s.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) id * client.getAddress().hashCode();
    }
}
