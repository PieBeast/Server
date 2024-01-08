package cpen221.mp3.server;

import cpen221.mp3.client.Client;
import cpen221.mp3.client.Request;
import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.entity.Sensor;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import cpen221.mp3.handler.MessageHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServerTests {
    // Run MessageHandlerInstance before running
    static String IP = "127.0.0.1";
    static int SERVER_PORT = 1;
    static MessageHandler mh = new MessageHandler(1);
    // filters
    static Filter f = new Filter(BooleanOperator.EQUALS, false);

    //clients
    static Client client1 = new Client(0, "test@test.com", IP, SERVER_PORT);
    Client client2 = new Client(1, "test@test.com", IP, SERVER_PORT);

    // entities
    static Actuator actuator1 = new Actuator(97, 0, "Switch", false, IP, SERVER_PORT);
    //    Actuator actuator2 = new Actuator(98, 0, "Switch", false, IP, SERVER_PORT);
    static Sensor sensor1 = new Sensor(99, 0, "CO2Sensor", IP, SERVER_PORT);

    // requests

    static Request r1 = new Request(RequestType.CONTROL, RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE, "0,97," + f.toString() + ",true");

    static ByteArrayOutputStream out = new ByteArrayOutputStream();

    @BeforeAll
    public static void start() throws InterruptedException {
//        System.setOut(new PrintStream(out));

        new Thread(() -> mh.start()).start();
        new Thread(() -> actuator1.start()).start();
//        new Thread(() -> sensor1.start()).start();

        Thread.sleep(1000);
    }

    //    @AfterEach
//    public void end() {
//        System.setOut(System.out);
//    }
    @Test
    public void CONFIG_UPDATE_MAX_WAIT_TIME() throws IOException, ClassNotFoundException {
        Server server = new Server(client1);
        Request r = new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "0,4.3");

        server.processIncomingRequest(r);

        assertEquals(4.3, server.getMaxWaitTime());
    }

    @Test
    public void CONTROL_NOTIFY_IF() throws IOException, ClassNotFoundException {
        Client client = new Client(0, "test@test.com", IP, SERVER_PORT);
        Server server = new Server(client);
        Filter filter = new Filter("value", DoubleOperator.GREATER_THAN_OR_EQUALS, 0.0);
        Request r = new Request(RequestType.CONTROL, RequestCommand.CONTROL_NOTIFY_IF, "0," + filter);

        server.processIncomingRequest(r);

        Event event1 = new SensorEvent(System.currentTimeMillis(), 0, 1, "TempSensor", 1.0);
        Event event2 = new SensorEvent(System.currentTimeMillis(), 0, 2, "TempSensor", 11.0);

        server.processIncomingEvent(event1);
        server.processIncomingEvent(event2);

        List<Integer> solution = new ArrayList<>();
        solution.add(2);
        solution.add(1);

        assertEquals(solution, server.readLogs());
    }

    @Test
    public void ANALYSIS_GET_EVENTS_IN_WINDOW() {
        Client client = new Client(0, "test@test.com", IP, SERVER_PORT);
        Server server = new Server(client);
        Event event1 = new SensorEvent(System.currentTimeMillis(), 0, 1, "TempSensor", 1.0);
        Event event2 = new SensorEvent(System.currentTimeMillis(), 0, 2, "TempSensor", 11.0);
        server.processIncomingEvent(event1);
        server.processIncomingEvent(event2);
        List<Event> answer = new ArrayList<>();
        TimeWindow time = new TimeWindow(0, System.currentTimeMillis());
        answer.add(event2);
        answer.add(event1);
        assertEquals(answer, server.eventsInTimeWindow(time));
    }

    @Test
    public void ANALYSIS_GET_ALL_ENTITIES() throws InterruptedException, IOException {
        Client client = new Client(0, "test@test.com", IP, SERVER_PORT);
        Server server = new Server(client);
        Event event1 = new SensorEvent(System.currentTimeMillis(), 0, 1, "TempSensor", 1.0);
        Event event2 = new SensorEvent(System.currentTimeMillis(), 0, 2, "TempSensor", 11.0);
        server.processIncomingEvent(event1);
        server.processIncomingEvent(event2);
        List<Integer> answer = new ArrayList<>();
        answer.add(1);
        answer.add(2);
        assertEquals(answer, server.getAllEntities());
    }

    @Test
    public void ANALYSIS_GET_LATEST_EVENTS() {
        Client client = new Client(0, "test@test.com", IP, SERVER_PORT);
        Server server = new Server(client);
        Event event1 = new SensorEvent(System.currentTimeMillis(), 0, 1, "TempSensor", 1.0);
        Event event2 = new SensorEvent(System.currentTimeMillis(), 0, 2, "TempSensor", 11.0);
        server.processIncomingEvent(event1);
        server.processIncomingEvent(event2);
        List<Event> a = new ArrayList<>();
        a.add(event1);
        assertEquals(a, server.lastNEvents(1));
    }

    @Test
    public void ANALYSIS_GET_MOST_ACTIVE_ENTITY() {
        Client client = new Client(0, "test@test.com", IP, SERVER_PORT);
        Server server = new Server(client);
        Event event1 = new SensorEvent(System.currentTimeMillis(), 0, 1, "TempSensor", 1.0);
        Event event2 = new SensorEvent(System.currentTimeMillis(), 0, 2, "TempSensor", 11.0);
        server.processIncomingEvent(event1);
        server.processIncomingEvent(event2);
        assertEquals(2, server.mostActiveEntity());
    }
}

