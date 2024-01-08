package cpen221.mp3.server;

import cpen221.mp3.client.Client;
import cpen221.mp3.client.Request;
import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.entity.Sensor;
import cpen221.mp3.handler.MessageHandler;

class MessageHandlerInstance {
    public static void main(String[] args) {
        MessageHandler mh = new MessageHandler(1);
        mh.start();
    }
}

class Actuator1Instance {
    public static void main(String[] args) {
        Actuator actuator1 = new Actuator(97, 0, "Switch", false, "127.0.0.1", 1);
        actuator1.start();
    }
}

class Actuator2Instance {
    public static void main(String[] args) {
        Actuator actuator2 = new Actuator(98, 1, "Switch", false, "127.0.0.1", 1);
        actuator2.start();
    }
}

class Sensor1Instance {
    public static void main(String[] args) {
        Sensor sensor1 = new Sensor(99, 0, "TempSensor", "127.0.0.1", 1);
        sensor1.start();
    }
}

class Sensor2Instance {
    public static void main(String[] args) {
        Sensor sensor2 = new Sensor(100, 0, "CO2Sensor", "127.0.0.1", 1);
        sensor2.start();
    }
}


class ClientInstance {
    static Client client0 = new Client(0, "test@test.com", "127.0.0.1", 1);
    static Client client1 = new Client(1, "test@test.com", "127.0.0.1", 1);
    static Filter f = new Filter(BooleanOperator.EQUALS, false);

    public static void main(String[] args) {
        Request r1 = new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "0,5");
        Request r2 = new Request(RequestType.CONTROL, RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE, "0,97," + f + ",true");
        Request r3 = new Request(RequestType.CONTROL, RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE, "1,98," + f + ",true");
//        client1.addEntity(actuator1);
//        client2.addEntity(actuator2);
//        client1.addEntity(sensor1);
//        client2.addEntity(sensor2);
        try {
            client0.sendRequest(r1);
            client0.sendRequest(r2);
            client1.sendRequest(r3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
