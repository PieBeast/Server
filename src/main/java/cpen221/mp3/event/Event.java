package cpen221.mp3.event;

public interface Event {

    /**
     * Gets the timestamp of the current event
     *
     * @return the timestamp of the current event
     */
    double getTimeStamp();

    /**
     * Gets the clientID of the current event
     *
     * @return the clientID of the current event
     */
    int getClientId();

    /**
     * Gets the entityID of the current event
     *
     * @return the entityID of the current event
     */
    int getEntityId();

    /**
     * Gets the entity type of the current event
     *
     * @return the entity type
     */
    String getEntityType();

    /**
     * Gets the value of the event if available
     *
     * @return the double value of the current event, and -1 if there is no double value
     */
    double getValueDouble();

    /**
     * Returns the boolean value of the current event
     *
     * @return the boolean value of the current event, and false if there is no boolean value.
     */
    // returns the boolean value of the event if available
    // returns false if the event does not have a boolean value
    boolean getValueBoolean();
}
