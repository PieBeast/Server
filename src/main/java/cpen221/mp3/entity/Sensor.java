package cpen221.mp3.entity;

import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sensor implements Entity {
    private final int id;
    private int clientId;
    private final String type;
    private String serverIP = null;
    private int serverPort = 0;
    private double eventGenerationFrequency = 0.2; // default value in Hz (1/s)
    private ServerSocket socket;

    /**
     * Creates a sensor instance.
     *
     * @param id   The id assigned to the sensor, must be unique.
     * @param type The type of the sensor being created, must be one of TempSensor, PressureSensor, CO2Sensor
     *             Switch.
     */
    public Sensor(int id, String type) {
        this.id = id;
        this.clientId = -1;         // remains unregistered
        this.type = type;
        try {
            socket = new ServerSocket(serverPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Sensor(int id, int clientId, String type) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        try {
            socket = new ServerSocket(serverPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Sensor(int id, String type, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = -1;   // remains unregistered
        this.type = type;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        try {
            socket = new ServerSocket(serverPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Sensor(int id, int clientId, String type, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        try {
            socket = new ServerSocket(serverPort);
        } catch (IOException ignored) {
        }
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
        return false;
    }

    public boolean registerForClient(int clientId) {
        if (getClientId() == -1) {
            this.clientId = clientId;
            return true;
        } else return clientId == this.clientId;
    }

    /**
     * Sets or updates the http endpoint that
     * the sensor should send events to
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
        eventGenerationFrequency = frequency;
    }

    /**
     * Sends an event to the server that the sensor is connected to.
     *
     * @param event The event being sent.
     */
    public void sendEvent(Event event) {
        int count = 0;
        boolean sent = false;
        if (!(this.serverIP == null)) {
            while (!sent) {
                try {
                    if (count >= 5) {
                        wait(10000);
                        count = 0;
                    }

                    Socket s = new Socket(this.serverIP, this.serverPort);
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

    public void start() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::sendAutoEvent, 0, (long) (1000 / eventGenerationFrequency), TimeUnit.MILLISECONDS);
    }

    private void sendAutoEvent() {
        switch (getType()) {
            case "TempSensor" -> {
                double state = 20 + (new Random()).nextDouble() * 4;
                SensorEvent se = new SensorEvent(System.currentTimeMillis(), clientId, id, type, state);
                sendEvent(se);
            }
            case "PressureSensor" -> {
                double state = 1020 + (new Random()).nextDouble() * 4;
                SensorEvent se = new SensorEvent(System.currentTimeMillis(), clientId, id, type, state);
                sendEvent(se);
            }
            case "CO2Sensor" -> {
                double state = 400 + (new Random()).nextDouble() * 50;
                SensorEvent se = new SensorEvent(System.currentTimeMillis(), clientId, id, type, state);
                sendEvent(se);
            }
            default -> {
            }
        }

    }
}