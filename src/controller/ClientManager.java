package controller;

import java.util.ArrayList;
import entity.Client;

public class ClientManager {
    private ArrayList<Client> clients;

    public ClientManager() {
        this.clients = new ArrayList<>();
    }

    public void addClient(Client client) {
        clients.add(client);
    }

    public void removeClient(Client client) {
        clients.remove(client);
    }

}
