package cpen221.mp3.server;

import cpen221.mp3.client.Client;
import cpen221.mp3.client.Request;
import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.entity.Entity;
import cpen221.mp3.entity.Sensor;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

public class Server {
    private final Client client;
    private double maxWaitTime = 2; // in seconds
    private List<Event> logs;
    private final BlockingQueue<Event> bufferEvent;
    private final BlockingQueue<Request> bufferRequest;
    private final HashSet<Event> events = new HashSet<>();
    private final ConcurrentHashMap<Integer, Entity> entities;
    private final BlockingQueue<Event> processed;
    private double logTimeStamp;
    private boolean isReprocessing = false;

    private boolean isLogging;
    private Filter logFilter;

    // Abstraction function:
    //    Represents a server where bufferRequest[0] == first request .... bufferRequest[i] == i - 1 request
    //    entities.key == entityID, entities.value == entity,logs.get(0) == first logged event...
    //    logs.get(i) == i-1 logged event

    /**
     * Representation Invariants:
     * Events, Requests, Entities, Clients, cannot be null
     *
     */

    /**
     * Creates a new instance of server for a specified client
     *
     * @param client, client assigned to this instance of server, not null
     */
    public Server(Client client) {
        this.client = client;
        this.bufferEvent = new LinkedBlockingQueue<>();
        this.bufferRequest = new LinkedBlockingQueue<>();
        this.logs = new ArrayList<>();
        this.entities = new ConcurrentHashMap<>();
        this.isLogging = false;
        processed = new PriorityBlockingQueue<>(1, new Comparator<Event>() {
            @Override
            public int compare(Event o1, Event o2) {
                double diff = o1.getTimeStamp() - o2.getTimeStamp();
                if (diff > 0) {
                    return -1;
                }
                if (diff < 0) {
                    return 1;
                }
                return 0;
            }
        });
    }

    /**
     * Update the max wait time for the client.
     * The max wait time is the maximum amount of time
     * that the server can wait for before starting to process each event of the client:
     * It is the difference between the time the message was received on the server
     * (not the event timeStamp from above) and the time it started to be processed.
     *
     * @param maxWaitTime the new max wait time
     */
    public void updateMaxWaitTime(double maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    /**
     * Gets the current max wait time of this instance of server
     *
     * @return the current max wait time
     */
    public double getMaxWaitTime() {
        return maxWaitTime;
    }

    /**
     * Set the actuator state if the given filter is satisfied by the latest event.
     * Here the latest event is the event with the latest timestamp not the event
     * that was received by the server the latest.
     * <p>
     * If the actuator is not registered for the client, then this method should do nothing.
     *
     * @param filter   the filter to check
     * @param actuator the actuator to set the state of as true
     */
    public void setActuatorStateIf(Filter filter, Actuator actuator) {
        if (actuator.getClientId() == client.getClientId()) {
            Event event = processed.peek();
            if (filter.satisfies(event)) {
                String r = (new Request(RequestType.CONTROL,
                        RequestCommand.CONTROL_SET_ACTUATOR_STATE,
                        actuator + "," + (SeverCommandToActuator.SET_STATE)) + "}");

                try (Socket s = new Socket(actuator.getIP(), client.getServerPort())) {
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    out.println(r);
                    System.out.println("Sent Request to MessageHandler: " + actuator.getId());
                } catch (IOException e) {
                    System.out.println("Unable to establish connection on IP: " + actuator.getIP() + ", Port: " + client.getServerPort());
                }
            } else {
                System.out.println("Request does not pass filter");
            }
        } else {
            System.out.println("Client ID's not equal - Actuator: " + actuator.getClientId() + "Client ID: " + client.getClientId());
        }
    }

    /**
     * Toggle the actuator state if the given filter is satisfied by the latest event.
     * Here the latest event is the event with the latest timestamp not the event
     * that was received by the server the latest.
     * <p>
     * If the actuator has never sent an event to the server, then this method should do nothing.
     * If the actuator is not registered for the client, then this method should do nothing.
     *
     * @param filter   the filter to check
     * @param actuator the actuator to toggle the state of (true -> false, false -> true)
     */
    public void toggleActuatorStateIf(Filter filter, Actuator actuator) {
        if (actuator.getClientId() == client.getClientId()) {
            Event event = processed.peek();
            if (filter.satisfies(event)) {
                String r = (new Request(RequestType.CONTROL,
                        RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE,
                        actuator + "," + (SeverCommandToActuator.TOGGLE_STATE)) + "}");

                try (Socket s = new Socket(actuator.getIP(), client.getServerPort())) {
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    out.println(r);
                    System.out.println("Sent Request to MessageHandler: " + actuator.getId());

                } catch (IOException e) {
                    System.out.println("Unable to establish connection on IP: " + actuator.getIP() + ", Client Server Port: " + client.getServerPort());
                }
            } else {
                System.out.println("Request does not pass filter");
            }
        } else {
            System.out.println("Client ID's not equal - Actuator: " + actuator.getClientId() + "Client ID: " + client.getClientId());
        }
    }

    /**
     * Log the event ID for which a given filter was satisfied.
     * This method is checked for every event received by the server.
     *
     * @param filter the filter to check
     */
    public void logIf(Filter filter) {
        for (Event e : events) {
            if (filter.satisfies(e) && e.getTimeStamp() >= logTimeStamp) {
                if (!logs.contains(e)) {
                    logs.add(e);
                    System.out.println("Added");
                }
            }
        }
    }

    /**
     * Return all the logs made by the "logIf" method so far.
     * If no logs have been made, then this method should return an empty list.
     * The list should be sorted in the order of event timestamps.
     * After the logs are read, they should be cleared from the server.
     *
     * @return list of event IDs
     */
    public List<Integer> readLogs() {
        List<Event> sortedList = sortedEventTimeStamp(logs);
        List<Integer> IDList = new ArrayList<>();
        for (Event event : sortedList) {
            IDList.add(event.getEntityId());
        }
        logs = new ArrayList<>();
        return IDList;
    }

    /**
     * List all the events of the client that occurred in the given time window.
     * Here the timestamp of an event is the time at which the event occurred, not
     * the time at which the event was received by the server.
     * If no events occurred in the given time window, then this method should return an empty list.
     *
     * @param timeWindow the time window of events, inclusive of the start and end times
     * @return list of the events for the client in the given time window
     */
    public List<Event> eventsInTimeWindow(TimeWindow timeWindow) {
        List<Event> inWindow = new ArrayList<>();
        for (Event e : events) {
            if (e.getTimeStamp() >= timeWindow.startTime && e.getTimeStamp() <= timeWindow.endTime) {
                inWindow.add(e);
            }
        }
        return inWindow;
    }

    /**
     * Returns a set of IDs for all the entities of the client for which
     * we have received events so far.
     * Returns an empty list if no events have been received for the client.
     *
     * @return list of all the entities of the client for which we have received events so far
     */
    public List<Integer> getAllEntities() {
        return new ArrayList<>(this.entities.keySet());
    }

    /**
     * List the latest n events of the client.
     * Here the order is based on the original timestamp of the events, not the time at which the events were received by the server.
     * If the client has fewer than n events, then this method should return all the events of the client.
     * If no events exist for the client, then this method should return an empty list.
     * If there are multiple events with the same timestamp in the boundary,
     * the ones with largest EntityId should be included in the list.
     *
     * @param n the max number of events to list
     * @return list of the latest n events of the client
     */
    public List<Event> lastNEvents(int n) {
        if (events.size() <= n) {
            return new ArrayList<>(events);
        }
        List<Event> sorted = sortedEventTimeStamp(events.stream().toList());
        return sorted.subList(sorted.size() - n, sorted.size());
    }

    /**
     * Sorts a given list by increasing timestamps. If timestamps are equal, sorts by
     * increasing entityID.
     *
     * @param unsorted List of events to be sorted, list is not null
     * @return new list of sorted events
     */
    private List<Event> sortedEventTimeStamp(List<Event> unsorted) {
        return unsorted.stream().sorted((o1, o2) -> {
            double diff = o1.getTimeStamp() - o2.getTimeStamp();
            if (diff == 0) {
                if (o1.getEntityId() > o2.getEntityId()) {
                    return -1;
                }
                return 1;
            } else if (diff > 0) {
                return 1;
            }
            return -1;
        }).toList();
    }

    /**
     * returns the ID corresponding to the most active entity of the client
     * in terms of the number of events it has generated.
     * <p>
     * If there was a tie, then this method should return the largest ID.
     *
     * @return the most active entity ID of the client
     */
    public int mostActiveEntity() {
        int mostActiveID = 0;
        int largestCount = Integer.MIN_VALUE;
        Map<Integer, Integer> eventMap = new HashMap<>();
        for (Event e : events) {
            if (!eventMap.containsKey(e.getEntityId())) {
                eventMap.put(e.getEntityId(), 1);
            } else {
                eventMap.put(e.getEntityId(), eventMap.get(e.getEntityId()) + 1);
            }
        }

        for (int i : eventMap.keySet()) {
            if (eventMap.get(i) >= largestCount) {
                if (eventMap.get(i) == largestCount) {
                    mostActiveID = Math.max(i, mostActiveID);
                } else {
                    mostActiveID = i;
                    largestCount = eventMap.get(i);
                }
            }
        }
        return mostActiveID;
    }

    /**
     * the client can ask the server to predict what will be
     * the next n timestamps for the next n events
     * of the given entity of the client (the entity is identified by its ID).
     * <p>
     * If the server has not received any events for an entity with that ID,
     * or if that Entity is not registered for the client, then this method should return an empty list.
     *
     * @param entityId the ID of the entity
     * @param n        the number of timestamps to predict
     * @return list of the predicted timestamps
     */
    public List<Double> predictNextNTimeStamps(int entityId, int n) {
        List<Object> eventValues;
        List<Double> timestamps;

        if (!entities.containsKey(entityId)) {
            return new ArrayList<>();
        } else {
            if (entities.get(entityId) instanceof Actuator) {
                eventValues = processed
                        .stream().toList()
                        .parallelStream()
                        .filter(e -> e instanceof ActuatorEvent)
                        .map(Event::getValueBoolean)
                        .collect(Collectors.toList());
                timestamps = processed
                        .stream().toList()
                        .parallelStream()
                        .filter(e -> e instanceof ActuatorEvent)
                        .map(Event::getTimeStamp)
                        .collect(Collectors.toList());
            } else {
                eventValues = processed
                        .stream().toList()
                        .parallelStream()
                        .filter(e -> e instanceof SensorEvent)
                        .map(e -> e.getValueDouble())
                        .collect(Collectors.toList());
                timestamps = processed
                        .stream().toList()
                        .parallelStream()
                        .filter(e -> e instanceof SensorEvent)
                        .map(e -> e.getTimeStamp())
                        .collect(Collectors.toList());
            }
        }

        return null;
    }

    /**
     * the client can ask the server to predict what will be
     * the next n values of the timestamps for the next n events
     * of the given entity of the client (the entity is identified by its ID).
     * The values correspond to Event.getValueDouble() or Event.getValueBoolean()
     * based on the type of the entity. That is why the return type is List<Object>.
     * <p>
     * If the server has not received any events for an entity with that ID,
     * or if that Entity is not registered for the client, then this method should return an empty list.
     *
     * @param entityId the ID of the entity
     * @param n        the number of double value to predict
     * @return list of the predicted timestamps
     */
    public List<Object> predictNextNValues(int entityId, int n) {
        if (!entities.containsKey(entityId)) {
            return new ArrayList<>();
        }

        return null;
    }

    /**
     * Processes the current event
     *
     * @param event
     */
    public void processIncomingEvent(Event event) {
        System.out.println("Processing Incoming Event");
        events.add(event);
        if (!entities.containsKey(event.getEntityId())) {
            if (event.getValueDouble() == -1) {
                entities.put(event.getEntityId(), new Actuator(
                        event.getEntityId(),
                        event.getClientId(),
                        event.getEntityType(),
                        event.getValueBoolean()
                ));
            } else {
                entities.put(event.getEntityId(), new Sensor(
                        event.getEntityId(),
                        event.getClientId(),
                        event.getEntityType())
                );
            }
        } else {
            if (event.getValueDouble() == -1) {
                entities.replace(event.getEntityId(), new Actuator(
                        event.getEntityId(),
                        event.getClientId(),
                        event.getEntityType(),
                        event.getValueBoolean()
                ));
            } else {
                entities.replace(event.getEntityId(), new Sensor(
                        event.getEntityId(),
                        event.getClientId(),
                        event.getEntityType())
                );
            }
        }
        if (isLogging) {
            logIf(logFilter);
        }
        processed.add(event);
//        System.out.println(entities.get(event.getEntityId()).toString());
    }

    /**
     * Parses the entityId from a string from the request
     *
     * @param request String in the format of a request
     * @return the entityID
     */
    private int parseEntityID(String request) {
        String substring = request.substring(request.indexOf(",") + 1);
        System.out.println(Integer.parseInt(substring.substring(0, substring.indexOf(","))));
        return Integer.parseInt(substring.substring(0, substring.indexOf(",")));
    }

    /**
     * Processes the incoming request, then does what the request wants.
     * If a request comes in that is out of order based on increasing timestamp,
     * reprocess all requests that have a timestamp less than the current request
     *
     * @param request the request to be processed, not null
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void processIncomingRequest(Request request) throws IOException, ClassNotFoundException {
        System.out.println("Processing Incoming Request...");
        String requestData = request.getRequestData();
        while (bufferRequest.size() > 11) {
            bufferRequest.poll();
        }
        if (!bufferRequest.isEmpty() && !isReprocessing) {
            BlockingQueue<Request> tempQueue = new LinkedBlockingQueue<>();
            for (Request oldReq : bufferRequest) {
                if (oldReq.getTimeStamp() > request.getTimeStamp()) {
                    tempQueue.add(oldReq);
                }
            }
            if (!tempQueue.isEmpty()) {
                tempQueue.add(request);
                isReprocessing = true;
                for (Request tempReq : tempQueue) {
                    processIncomingRequest(tempReq);
                }
                isReprocessing = false;
            }
            bufferRequest.add(request);
        } else if (!isReprocessing) {
            bufferRequest.add(request);
        }
        final int id = Integer.parseInt(requestData.substring(0, requestData.indexOf(",")));

        switch (request.getRequestCommand()) {
            case CONFIG_UPDATE_MAX_WAIT_TIME ->
                    updateMaxWaitTime(Double.parseDouble(requestData.substring(requestData.indexOf(",") + 1)));
            case CONTROL_SET_ACTUATOR_STATE -> {
                String filterString = requestData.substring(requestData.indexOf(","));
//              System.out.println(((Actuator) entities.get(entityID)).toString());
                Filter f = Deserializer.deserializeFilter(filterString);

                if (entities.containsKey(parseEntityID(requestData))) {
                    System.out.println("Running CONTROL_SET_ACTUATOR_STATE...");
                    setActuatorStateIf(f, (Actuator) entities.get(parseEntityID(requestData)));
                } else {
                    System.out.println("Err: Actuator does not exist for Server");
                }
            }
            case CONTROL_TOGGLE_ACTUATOR_STATE -> {
                String filterString = requestData.substring(requestData.indexOf(","));
                Filter f = Deserializer.deserializeFilter(filterString);

                if (entities.containsKey(parseEntityID(requestData))) {
                    System.out.println("Running TOGGLE_SET_ACTUATOR_STATE...");
                    toggleActuatorStateIf(f, (Actuator) entities.get(parseEntityID(requestData)));
                } else {
                    System.out.println("Err: Actuator does not exist for Server");
                }
            }
            case CONTROL_NOTIFY_IF -> {
                logTimeStamp = request.getTimeStamp();
                isLogging = true;
                if (requestData.contains("Boolean")) {
                    logFilter = (Deserializer.deserializeFilter(requestData));
                } else if (requestData.contains("Double")) {
                    logFilter = (Deserializer.deserializeFilter(requestData));
                } else if (requestData.contains("Filters")) {
                    logFilter = (Deserializer.deserializeFilter(requestData));
                }
            }
            case ANALYSIS_GET_ALL_LOGS -> {
                readLogs();
            }
            case ANALYSIS_GET_EVENTS_IN_WINDOW -> {
                eventsInTimeWindow(Deserializer.deserializeTimeWindow(requestData));
            }
            case ANALYSIS_GET_ALL_ENTITIES -> {
                getAllEntities();
                if (getAllEntities().isEmpty()) {
                    System.out.println("Empty List");
                }
            }
            case ANALYSIS_GET_LATEST_EVENTS -> {
                lastNEvents(Integer.parseInt(requestData));
            }
            case ANALYSIS_GET_MOST_ACTIVE_ENTITY -> {
                mostActiveEntity();
            }
            case PREDICT_NEXT_N_TIMESTAMPS -> {
                int n = Integer.parseInt(requestData.substring(requestData.indexOf(",") + 1));
                predictNextNTimeStamps(id, n);
            }
            case PREDICT_NEXT_N_VALUES -> {
                int n = Integer.parseInt(requestData.substring(requestData.indexOf(",") + 1));
                predictNextNValues(id, n);
            }
        }
    }
}
