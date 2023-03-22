package controller;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import entity.Callback;

public class CallbackManager {
    private Map<Integer, List<Callback>> callbacks;

    public CallbackManager() {
        this.callbacks = new HashMap<>();
    } // 1679506106465

    public void registerCallback(InetAddress clientAddress, int clientPort, int flightId, int interval) {
        long expiry = System.currentTimeMillis() + (interval * 1000 * 60);
        System.out.println("Expiry: " + expiry);
        Callback cb = new Callback(clientAddress, clientPort, flightId, expiry);
        if (!callbacks.containsKey(flightId)) {
            callbacks.put(flightId, new ArrayList<>());
        }
        List<Callback> currentCallbacks = callbacks.get(flightId);
        currentCallbacks.add(cb);
    }

    public List<Callback> getCallbacksToUpdate(int flightId) {
        if (!callbacks.containsKey(flightId)) return new ArrayList<>();
        List<Callback> updating = new ArrayList<>();
        List<Callback> currCallbacks = callbacks.get(flightId);
        long currTime = System.currentTimeMillis();
        for (int i = currCallbacks.size() - 1; i > -1; i--) {
            Callback cb = currCallbacks.get(i);
            if (currTime > cb.getExpiry()) {
                currCallbacks.remove(i);
                continue;
            }
            updating.add(cb);
        }
        return updating;
    }
}
