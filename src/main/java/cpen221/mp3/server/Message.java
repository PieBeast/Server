package cpen221.mp3.server;

import cpen221.mp3.client.Request;
import cpen221.mp3.event.Event;

/*
 * A datatype to store either an event or request
 */
public class Message {
    private final Event event;
    private final Request request;
    boolean containsEvent;
    boolean containsRequest;
    // Abstraction function:
    //    Represents either an event, or message where containsEvent == true if
    //    it is an event, and containsRequest == true if it is a request

    // Rep Invariant:
    //    Either request is null, or message is null
    //    containsEvent, containsEvent always true or false

    /**
     * Creates a new instance of message for events
     *
     * @param e the event, not null
     */
    public Message(Event e) {
        containsEvent = true;
        containsRequest = false;
        event = e;
        request = null;
    }

    /**
     * Creates a new instance of message for requests
     *
     * @param r the request, not null
     */
    public Message(Request r) {
        containsEvent = false;
        containsRequest = true;
        request = r;
        event = null;
    }

    /**
     * Checks if this current message is an event
     *
     * @return True if it is a message, false if it is not a message
     */
    public boolean hasEvent() {
        return containsEvent;
    }

    /**
     * Checks if the current message is a request
     *
     * @return True if it is a request, false if it is a message
     */
    public boolean hasRequest() {
        return containsRequest;
    }

    /**
     * Gets the event of the current message
     *
     * @return the current event if this message is an event, and null if it is a request
     */
    public Event getEvent() {
        if (containsRequest) {
            return null;
        }
        return event;
    }

    /**
     * Gets the request of the current message
     *
     * @return the current request if this message is a request, and null if it is an event
     */
    public Request getRequest() {
        if (containsEvent) {
            return null;
        }
        return request;
    }

    /**
     * Gets the timestamp of the current message
     *
     * @return the timestamp of the current message
     */
    public double getTimeStamp() {
        if (containsEvent) {
            return event.getTimeStamp();
        }
        return request.getTimeStamp();
    }

    /**
     * Gets the client ID of the current message.
     *
     * @return the client ID of the current message
     */
    public int getClientID() {
        if (containsEvent) {
            return event.getClientId();
        }
        return request.getClientID();
    }
}
