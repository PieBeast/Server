package cpen221.mp3.event;

public class SensorEvent implements Event {
    private final double TimeStamp;
    private final int ClientId;
    private final int EntityId;
    private final String EntityType;
    private final double Value;
    // Abstraction function:
    //    Represents an event from a sensor, where Value is the
    //    value contained in the sensor, timeStamp is the time the event is sent,
    //    ClientID is the ID of the client, and EntityID is the ID of this actuator.

    // Rep Invariant:
    //    EntityType != null

    public SensorEvent(double TimeStamp,
                       int ClientId,
                       int EntityId,
                       String EntityType,
                       double Value) {
        this.TimeStamp = TimeStamp;
        this.ClientId = ClientId;
        this.EntityId = EntityId;
        this.EntityType = EntityType;
        this.Value = Value;
    }

    public double getTimeStamp() {
        return this.TimeStamp;
    }

    public int getClientId() {
        return this.ClientId;
    }

    public int getEntityId() {
        return this.EntityId;
    }

    public String getEntityType() {
        return this.EntityType;
    }

    public double getValueDouble() {
        return this.Value;
    }

    public boolean getValueBoolean() {
        return false;
    }

    @Override
    public String toString() {
        return "SensorEvent{" +
                "TimeStamp=" + getTimeStamp() +
                ",ClientId=" + getClientId() +
                ",EntityId=" + getEntityId() +
                ",EntityType=" + getEntityType() +
                ",Value=" + getValueDouble() +
                '}';
    }
}
