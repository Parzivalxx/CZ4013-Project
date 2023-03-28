package controller;

import java.util.ArrayList;
import java.net.InetAddress;

import entity.Client;

public class ClientManager {
    private ArrayList<Client> clients;

    public ClientManager() {
        this.clients = new ArrayList<>();
    }

    public ArrayList<Client> getClients() {return clients;}

    public void addClient(Client client) {
        clients.add(client);
    }

    public void removeClient(Client client) {
        clients.remove(client);
    }

    public Client getClientByAddressAndPort(InetAddress clientAddress, int clientPort) {
        for (Client c : clients) {
            if (c.getAddress().equals(clientAddress) && c.getPort() == clientPort) return c;
        }
        return null;
    }

}
