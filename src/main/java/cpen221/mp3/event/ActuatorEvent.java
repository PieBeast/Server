package cpen221.mp3.event;

public class ActuatorEvent implements Event {
    private final double TimeStamp;
    private final int ClientId;
    private final int EntityId;
    private final String EntityType;
    private final boolean Value;

    // Abstraction function:
    //    Represents an event from an actuator, where Value is the
    //    value contained in the actuator, timeStamp is the time the event is sent,
    //    ClientID is the ID of the client, and EntityID is the ID of this actuator.

    // Rep Invariant:
    //    EntityType != null

    public ActuatorEvent(double TimeStamp,
                         int ClientId,
                         int EntityId,
                         String EntityType,
                         boolean Value) {
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

    public boolean getValueBoolean() {
        return this.Value;
    }

    public double getValueDouble() {
        return -1;
    }

    @Override
    public String toString() {
        return "ActuatorEvent{" +
                "TimeStamp=" + getTimeStamp() +
                ",ClientId=" + getClientId() +
                ",EntityId=" + getEntityId() +
                ",EntityType=" + getEntityType() +
                ",Value=" + getValueBoolean() +
                '}';
    }
}
