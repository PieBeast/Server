package cpen221.mp3.entity;

import cpen221.mp3.client.Request;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.server.Deserializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Actuator implements Entity {
    private final int id;
    private int clientId;
    private final String type;
    private boolean state;
    private double eventGenerationFrequency = 0.2; // default value in Hz (1/s)
    private String serverIP = null;
    private int serverPort = 0;
    private final String host = "127.0.0.1";
    private final int port;
    private static final Set<Integer> usedPorts = new HashSet<>();

    /**
     * Creates an actuator instance.
     *
     * @param id         The id assigned to the actuator, must be unique.
     * @param type       The type of actuator, must be Switch.
     * @param init_state The initial state of the actuator.
     */
    public Actuator(int id, String type, boolean init_state) {
        this.id = id;
        this.clientId = -1;         // remains unregistered
        this.type = type;
        this.state = init_state;
        port = id;
    }

    public Actuator(int id, int clientId, String type, boolean init_state) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.state = init_state;
        this.port = id;
    }

    public Actuator(int id, String type, boolean init_state, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = -1;         // remains unregistered
        this.type = type;
        this.state = init_state;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.port = id;
    }

    public Actuator(int id, int clientId, String type, boolean init_state, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.state = init_state;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.port = id;
    }

    private int generatePort() {
        Random r = new Random();
        int port = r.nextInt(65536);
        while (usedPorts.contains(port)) {
            port = r.nextInt(65536);
        }
        return port;
    }

    public int getId() {
        return id;
    }

    public int getClientId() {
        return clientId;
    }

    public String getType() {
        return type;
    }

    public boolean isActuator() {
        return true;
    }

    public boolean getState() {
        return state;
    }

    public String getIP() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void updateState(boolean new_state) {
        this.state = new_state;
    }

    /**
     * Registers the actuator for the given client
     *
     * @return true if the actuator is new (clientID is -1 already) and gets successfully registered
     * or if it is already registered for clientId, else false
     */
    public boolean registerForClient(int clientId) {
        if (getClientId() == -1) {
            this.clientId = clientId;
            return true;
        } else return clientId == this.clientId;
    }

    /**
     * Sets or updates the http endpoint that
     * the actuator should send events to
     *
     * @param serverIP   the IP address of the endpoint
     * @param serverPort the port number of the endpoint
     */
    public void setEndpoint(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    /**
     * Sets the frequency of event generation
     *
     * @param frequency the frequency of event generation in Hz (1/s)
     */
    public void setEventGenerationFrequency(double frequency) {
        this.eventGenerationFrequency = frequency;
    }

    public void sendEvent(Event event) {
        int count = 0;
        boolean sent = false;
        if (!(this.serverIP == null) && !(getClientId() == -1)) {
            while (!sent) {
                try (Socket s = new Socket(this.serverIP, this.serverPort)) {
                    if (count >= 5) {
                        wait(10000);
                        count = 0;
                    }
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    out.println(event.toString());
                    sent = true;
                } catch (IOException e) {
                    System.out.println("Unable to establish connection on IP: " + this.serverIP + ", Port: " + this.serverPort);
                    count++;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void processServerMessage(Request command) {
        System.out.println("Actuator: " + getId() + " received Command: " + command.getRequestCommand().toString());
        System.out.println("Time taken: " + (System.currentTimeMillis() - command.getTimeStamp()));
        switch (command.getRequestCommand()) {
            case CONTROL_SET_ACTUATOR_STATE -> {
                System.out.println("Previous State: " + getState());
                updateState(true);
                System.out.println("Current State: " + getState());
            }
            case CONTROL_TOGGLE_ACTUATOR_STATE -> {
                System.out.println("Previous State: " + getState());
                updateState(!getState());
                System.out.println("Current State: " + getState());
            }
        }
    }

    @Override
    public String toString() {
        return "Actuator{" +
                "getId=" + getId() +
                ",ClientId=" + getClientId() +
                ",EntityType=" + getType() +
                ",IP=" + getIP() +
                ",Port=" + getPort() +
                '}';
    }

    // you will most likely need additional helper methods for this class
    public void start() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::sendAutoEvent, 0, (long) (1000 / eventGenerationFrequency), TimeUnit.MILLISECONDS);

        while (true) {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Waiting for Request...");
                try (Socket incomingSocket = serverSocket.accept()) {
                    System.out.println("Accepted Request");
                    String message = (new BufferedReader(new InputStreamReader(incomingSocket.getInputStream()))).readLine();
                    System.out.println("Message: " + message);
                    if (message.contains("getId=" + getId())) {
                        processServerMessage(Deserializer.deserializeRequest(message));
                    }
                } catch (IOException e) {
                    System.err.println("Error processing request: " + e.getMessage());
                }
            } catch (IOException e) {
                System.err.println("Error creating server socket: " + e.getMessage());
            }
        }
    }

    private void sendAutoEvent() {
        Random r = new Random();

        if (getType().equals("Switch")) {
            boolean temp = r.nextBoolean();
            ActuatorEvent ac = new ActuatorEvent(System.currentTimeMillis(), clientId, id, type, temp);
            this.state = temp;
            sendEvent(ac);
        }
    }

//    public static void main(String[] args) {
//        Actuator a = new Actuator(1, 0, "Switch", false, "127.0.0.1", 1);
//        ActuatorEvent actuatorEvent = new ActuatorEvent(System.currentTimeMillis(), 0, 1, "Switch", false);
//        a.sendEvent(actuatorEvent);
//
//        try (ServerSocket serverSocket = new ServerSocket(a.getPort())) {
//            System.out.println("Waiting for Request...");
//            Socket s = serverSocket.accept();
//            System.out.println("Server Connected: " + s.getInetAddress().getHostAddress());
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
//
//            String input;
//            while ((input = in.readLine()) != null) {
//                a.processServerMessage(Deserializer.deserializeRequest(input));
//            }
//
//        } catch (IOException ignored) {}
//    }
//    public static void main(String[] args) {
//        Actuator a = new Actuator(1, 0, "Switch", false, "127.0.0.1", 1);
//        ActuatorEvent actuatorEvent = new ActuatorEvent(System.currentTimeMillis(), 0, 1, "Switch", false);
//        a.sendEvent(actuatorEvent);
//
//        try (ServerSocket serverSocket = new ServerSocket(a.getPort())) {
//            System.out.println("Waiting for Request...");
//            Socket s = serverSocket.accept();
//            System.out.println("Server Connected: " + s.getInetAddress().getHostAddress());
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
//
//            String input;
//            while ((input = in.readLine()) != null) {
//                a.processServerMessage(Deserializer.deserializeRequest(input));
//            }
//
//        } catch (IOException ignored) {}
//    }
}