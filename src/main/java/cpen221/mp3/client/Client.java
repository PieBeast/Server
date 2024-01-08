package cpen221.mp3.client;

import cpen221.mp3.entity.Entity;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Client {

    private final int clientId;
    private final String email;
    private final String serverIP;
    private final int serverPort;
    private final Map<Integer, Entity> entities;

    /**
     * Creates a client instance
     *
     * @param clientId   The id value assigned to the client.
     * @param email      An email that is attached to the client.
     * @param serverIP   The IP that this client is connected to.
     * @param serverPort The port that this client is connected to.
     */
    public Client(int clientId, String email, String serverIP, int serverPort) {
        this.clientId = clientId;
        this.email = email;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.entities = new HashMap<>();
    }

    public int getClientId() {
        return clientId;
    }

    public int getServerPort() {
        return this.serverPort;
    }

    /**
     * Registers an entity for the client
     *
     * @return true if the entity is new and gets successfully registered, false if the Entity is already registered
     */
    public boolean addEntity(Entity entity) {
        if (entities.containsKey(entity.getId()) || entity == null || !entity.registerForClient(this.clientId)) {
            System.out.println("Err: Could not add Entity: " + entity);
            return false;
        }
        entities.put(entity.getId(), entity);
        System.out.println("Successfully Added Entity: " + entity);
        return true;
    }

    public Map<Integer, Entity> getEntities() {
        return new HashMap<>(this.entities);
    }

    /**
     * Sends a request to the server
     *
     * @param request request to be sent, not null
     * @throws IOException
     */
    public void sendRequest(Request request) throws IOException {
        String data = request.getRequestData();
        if (data.contains("ACTUATOR")) {
            int entityId = Integer.parseInt(data.substring(data.indexOf(",") + 1, data.indexOf(",") + 2));
            if (entityId != clientId) {
                System.out.println("Err: Actuator " + entityId + "is not registered or does not exist for Client " + clientId);
                return;
            }
        }
        try (Socket s = new Socket(this.serverIP, this.serverPort)) {
            System.out.println("Creating Socket on Port: " + this.serverPort + ", IP: " + this.serverIP);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            out.println(request + "}");

            System.out.println("Serialized Request: " + request);
            System.out.println("Succesfully written to output stream");
        } catch (IOException e) {
            System.out.println("Unable to establish connection on IP: " + this.serverIP + ", Port: " + this.serverPort);
        }
        // note that Request is a complex object that you need to serialize before sending
    }
}