package cpen221.mp3.server;

import cpen221.mp3.client.Request;
import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDeserializer {
    @Test
    public void testEvent() {
        Event e = new ActuatorEvent(100.3, 20, 30, "Switch", true);
        assertEquals(Deserializer.deserializeEvent(e.toString()).toString(), e.toString());

        Event e2 = new SensorEvent(100.3, 20, 30, "Switch", 14.67);
        assertEquals(Deserializer.deserializeEvent(e2.toString()).toString(), e2.toString());
    }

    @Test
    public void testRequest() {
        Request r = new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "10");
        assertEquals(Deserializer.deserializeRequest(r.toString()).getRequestType(), r.getRequestType());
        assertEquals(Deserializer.deserializeRequest(r.toString()).getRequestCommand(), r.getRequestCommand());
        assertEquals(Deserializer.deserializeRequest(r.toString()).getRequestData(), r.getRequestData());
    }

    @Test
    public void testFilter() {
        Filter f1 = new Filter(BooleanOperator.EQUALS, true);
        Filter f2 = new Filter("timestamp", DoubleOperator.GREATER_THAN, 10.0);
        ArrayList<Filter> filters = new ArrayList<>();
        ArrayList<Filter> filters2 = new ArrayList<>();
        filters2.add(f2);
        Filter f5 = new Filter(filters2);
        filters.add(f1);
        filters.add(f5);
        Filter f3 = new Filter(filters);
        String s = f3.toString();
        Filter f4 = Deserializer.deserializeFilter(s);
        Request r = new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "10");
        assertEquals(Deserializer.deserializeRequest(r.toString()).getRequestType(), r.getRequestType());
        assertEquals(Deserializer.deserializeRequest(r.toString()).getRequestCommand(), r.getRequestCommand());
        assertEquals(Deserializer.deserializeRequest(r.toString()).getRequestData(), r.getRequestData());
    }
}
