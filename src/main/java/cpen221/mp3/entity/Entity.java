package cpen221.mp3.entity;

import cpen221.mp3.event.Event;

public interface Entity {
    /**
     * Gets the ID of the entity
     *
     * @return the entity ID
     */
    int getId();

    /**
     * Gets the client ID
     *
     * @return the client ID
     */
    int getClientId();

    /**
     * Gets the type of entity
     *
     * @return the type of entity
     */
    String getType();

    /**
     * Checks if the current entity is an actuator
     *
     * @return true if it is an actuator, false if it is a sensor
     */
    boolean isActuator();

    /**
     * Registers the entity for the given client
     *
     * @param clientId the ID to register, not -1
     * @return True if the actuator is new (clientID is -1 already) and gets successfully registered
     * or if it is already registered for clientId, else false
     */
    boolean registerForClient(int clientId);

    // sets or updates the http endpoint of the entity
    void setEndpoint(String serverIP, int serverPort);

    void setEventGenerationFrequency(double frequency);

    /**
     * Sends an event to the endpoint of a connection
     *
     * @param event, event to be sent, not null
     */
    void sendEvent(Event event);
}