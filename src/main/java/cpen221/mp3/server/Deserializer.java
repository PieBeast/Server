package cpen221.mp3.server;

import cpen221.mp3.client.Request;
import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;

import java.util.ArrayList;

public class Deserializer {
    /**
     * @param in
     * @return
     */
    public static Event deserializeEvent(String in) {
        StringBuilder serializedRequest = new StringBuilder(in);
        boolean acc;
        acc = in.contains("ActuatorEvent");

        double timestamp;
        int clientID;
        int entityID;
        String EntityType;

        serializedRequest.delete(0, ("TimeStamp=").length() + serializedRequest.indexOf("TimeStamp="));
        timestamp = Double.parseDouble(serializedRequest.substring(0, serializedRequest.indexOf(",ClientId=")));
        serializedRequest.delete(0, ",ClientId=".length() + serializedRequest.indexOf(",ClientId="));
        clientID = Integer.parseInt(serializedRequest.substring(0, serializedRequest.indexOf(",EntityId=")));
        serializedRequest.delete(0, ",EntityId=".length() + serializedRequest.indexOf(",EntityId="));
        entityID = Integer.parseInt(serializedRequest.substring(0, serializedRequest.indexOf(",EntityType=")));
        serializedRequest.delete(0, ",EntityType=".length() + serializedRequest.indexOf(",EntityType="));
        EntityType = serializedRequest.substring(0, serializedRequest.indexOf(",Value="));
        serializedRequest.delete(0, ",Value=".length() + serializedRequest.indexOf(",Value="));
        if (acc) {
            boolean value = Boolean.parseBoolean(serializedRequest.substring(0, serializedRequest.indexOf("}")));
            return new ActuatorEvent(timestamp, clientID, entityID, EntityType, value);
        }
        double value = Double.parseDouble(serializedRequest.substring(0, serializedRequest.indexOf("}")));
        return new SensorEvent(timestamp, clientID, entityID, EntityType, value);
    }

    /**
     * @param in
     * @return
     */
    public static Request deserializeRequest(String in) {
        StringBuilder serializedRequest = new StringBuilder(in);

        serializedRequest.delete(0, ("TimeStamp=").length() + serializedRequest.indexOf("TimeStamp="));
        double timeStamp = Double.parseDouble(serializedRequest.substring(0, serializedRequest.indexOf(",RequestType=")));

        serializedRequest.delete(0, (",RequestType=").length() + serializedRequest.indexOf(",RequestType="));
        RequestType requestType = RequestType.valueOf(serializedRequest.substring(0, serializedRequest.indexOf(",RequestCommand=")));

        serializedRequest.delete(0, (",RequestCommand=").length() + serializedRequest.indexOf(",RequestCommand="));
        RequestCommand requestCommand = RequestCommand.valueOf(serializedRequest.substring(0, serializedRequest.indexOf(",RequestData=")));

        serializedRequest.delete(0, (",RequestData=").length() + serializedRequest.indexOf(",RequestData="));
        String requestData = serializedRequest.substring(0, serializedRequest.indexOf("}."));
        Request r = new Request(requestType, requestCommand, requestData);
        r.setTimeStamp(timeStamp);
        return r;
    }

    /**
     * @param in
     * @return
     */
    public static Filter deserializeFilter(String in) {
        Filter.FilterType filterType;
        if (in.contains(new StringBuffer("Filters="))) {
            filterType = Filter.FilterType.MULTI;
        } else if (in.contains(new StringBuffer("DoubleOperator="))) {
            filterType = Filter.FilterType.DOUBLE;
        } else {
            filterType = Filter.FilterType.BOOL;
        }
        switch (filterType) {
            case BOOL -> {
                StringBuilder sr = new StringBuilder(in);

                sr.delete(0, ("BooleanOperator=").length() + sr.indexOf("BooleanOperator="));
                //System.out.println(sr);
                BooleanOperator operator = BooleanOperator.valueOf((sr.substring(0, sr.indexOf(",Value="))));

                sr.delete(0, (",Value=").length() + sr.indexOf(",Value="));
                //System.out.println(sr);
                boolean value = Boolean.parseBoolean(sr.substring(0, sr.indexOf("}")));

                return new Filter(operator, value);
            }
            case DOUBLE -> {
                StringBuilder sr = new StringBuilder(in);

                sr.delete(0, ("DoubleOperator=").length() + sr.indexOf("DoubleOperator="));
                //System.out.println(sr);
                DoubleOperator operator = DoubleOperator.valueOf((sr.substring(0, sr.indexOf(",Field="))));

                sr.delete(0, (",Field=").length() + sr.indexOf(",Field="));
                String field = sr.substring(0, sr.indexOf(",Value="));

                sr.delete(0, (",Value=").length() + sr.indexOf(",Value="));
                //System.out.println(sr);
                double value = Double.parseDouble(sr.substring(0, sr.indexOf("}")));
                return new Filter(field, operator, value);
            }
            case MULTI -> {
                StringBuilder sr = new StringBuilder(in);
                ArrayList<Filter> filters = new ArrayList<>();
                sr.delete(0, ("Filters=").length() + sr.indexOf("Filters="));
                while (sr.charAt(0) != '}') {
                    filters.add(deserializeFilter(sr.substring(0, sr.indexOf(",,"))));
                    sr.delete(0, sr.indexOf(",,") + 2);
                }
                return new Filter(filters);
            }
        }
        return null;
    }

    /**
     * @param in
     * @return
     */
    public static TimeWindow deserializeTimeWindow(String in) {
        StringBuilder serializedRequest = new StringBuilder(in);

        serializedRequest.delete(0, (",RequestType=").length() + serializedRequest.indexOf(",RequestType="));
        RequestType requestType = RequestType.valueOf(serializedRequest.substring(0, serializedRequest.indexOf(",RequestCommand=")));

        serializedRequest.delete(0, (",RequestCommand=").length() + serializedRequest.indexOf(",RequestCommand="));
        RequestCommand requestCommand = RequestCommand.valueOf(serializedRequest.substring(0, serializedRequest.indexOf(",RequestData=")));

        serializedRequest.delete(0, (",RequestData=").length() + serializedRequest.indexOf(",RequestData="));
        String requestData = serializedRequest.substring(0, serializedRequest.indexOf("}"));
        return null;
    }

    /**
     * @param in
     * @return
     */
    public static Actuator deserializeActuator(String in) {
        StringBuilder serialA = new StringBuilder(in);

        serialA.delete(0, ("Actuator{" + "getId=").length() + serialA.indexOf("Actuator{" + "getId="));
        int entityID = Integer.parseInt(serialA.substring(0, serialA.indexOf(",ClientId=")));

        serialA.delete(0, (",ClientId=").length() + serialA.indexOf(",ClientId="));
        int clientID = Integer.parseInt(serialA.substring(0, serialA.indexOf(",EntityType=")));

        serialA.delete(0, (",EntityType=").length() + serialA.indexOf(",EntityType="));
        String type = serialA.substring(0, serialA.indexOf(",IP="));

        serialA.delete(0, (",IP=").length() + serialA.indexOf(",IP="));
        String host = serialA.substring(0, serialA.indexOf(",Port="));

        serialA.delete(0, (",Port=").length() + serialA.indexOf(",Port="));
        System.out.println(serialA);
        int port = Integer.parseInt(serialA.toString());

        return new Actuator(entityID, clientID, type, false, host, port);
    }
}
