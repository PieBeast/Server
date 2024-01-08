package cpen221.mp3.handler;

import cpen221.mp3.client.Client;
import cpen221.mp3.client.Request;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.event.Event;
import cpen221.mp3.server.Deserializer;
import cpen221.mp3.server.Message;
import cpen221.mp3.server.Server;
import cpen221.mp3.server.SeverCommandToActuator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

class MessageHandlerThread implements Runnable {
    private final Socket incomingSocket;
    private final ConcurrentMap<Integer, Server> serverMap;
    private final BlockingQueue<Message> queue;
    // Abstraction function:
    //    Represents a thread of MessageHandler with incomingSocket being the
    //    server-client connection, serverMap being the map of servers, and all
    //    serverMap.key == a client ID, and queue contains message to be sent,
    //    queue.poll corresponds with the message that was received the latest

    // Rep Invariant:
    //    incomingSocket, serverMap, queue all != null, unique serverMap.key == unique server

    /**
     * @param incomingSocket
     * @param serverMap
     * @param queue
     */
    public MessageHandlerThread(Socket incomingSocket, ConcurrentMap<Integer, Server> serverMap, BlockingQueue queue) {
        this.incomingSocket = incomingSocket;
        this.serverMap = serverMap;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(incomingSocket.getInputStream()));
            String input;
            while ((input = in.readLine()) != null) {
                if (input.contains("Request{")) {
                    System.out.println("Processing: " + input);
                    Request r = Deserializer.deserializeRequest(input);
                    String data = r.getRequestData();
                    queue.add(new Message(r));
                    waitTillReady(new Message(r));
                    queue.poll();
                    if (input.contains(SeverCommandToActuator.SET_STATE.toString()) || input.contains(SeverCommandToActuator.TOGGLE_STATE.toString())) {
                        Actuator a = Deserializer.deserializeActuator(data.substring(0, data.indexOf("}")));
                        System.out.println("Started Socket on IP: " + a.getIP() + " and Port: " + a.getPort());

                        try (Socket s = new Socket(a.getIP(), a.getPort())) {
                            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                            System.out.println(input);
                            System.out.println("Sending Request to Actuator...");
                            out.println(input);
                        } catch (IOException e) {
                            System.out.println("Unable to establish connection on IP: " + a.getIP() + ", Port: " + a.getPort());
                        }
                    } else {
                        int clientId = Integer.parseInt(data.substring(0, data.indexOf(",")));
                        System.out.println("Client ID: " + clientId);

                        if (serverMap.containsKey(clientId)) {
                            System.out.println("Existing Server on ID: " + clientId);
                            serverMap.get(clientId).processIncomingRequest(r);
                        } else {
                            Client client = new Client(
                                    clientId, "temp@temp.ca",
                                    incomingSocket.getInetAddress().toString(),
                                    incomingSocket.getLocalPort()
                            );

                            System.out.println("Creating new Server on ID: " + clientId);

                            Server server = new Server(client);

                            server.processIncomingRequest(r);
                            serverMap.put(clientId, server);
                        }
                    }
                } else {
                    Event e = Deserializer.deserializeEvent(input);
                    queue.add(new Message(e));
                    int clientId = e.getClientId();
                    waitTillReady(new Message(e));
                    queue.poll();
                    System.out.println("Processing: " + input);

                    if (serverMap.containsKey(clientId)) {
//                        System.out.println("Existing Server on ID: " + clientId);
                        serverMap.get(clientId).processIncomingEvent(e);
                    } else {
                        Client client = new Client(
                                clientId, "temp@temp.ca",
                                incomingSocket.getInetAddress().toString(),
                                incomingSocket.getLocalPort()
                        );

//                        System.out.println("Creating new Server on ID: " + clientId);
                        Server server = new Server(client);
                        server.processIncomingEvent(e);
                        serverMap.put(clientId, server);
                    }
                }
            }
            incomingSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void waitTillReady(Message message) {
        boolean ready = false;
        while (!ready) {
            if (!queue.isEmpty()) {
                Message m = queue.peek();
                String s1, s2;
                boolean same = false;
                if (m.hasEvent() && message.hasEvent()) {
                    s1 = m.getEvent().toString();
                    s2 = message.getEvent().toString();
                    same = s1.equals(s2);
                } else if (m.hasRequest() && message.hasRequest()) {
                    s1 = m.getRequest().toString();
                    s2 = message.getRequest().toString();
                    same = s1.equals(s2);
                }
                if (same) {
                    double time;
                    if (serverMap.containsKey(m.getClientID())) {
                        time = serverMap.get(m.getClientID()).getMaxWaitTime() * 1000 + m.getTimeStamp() - System.currentTimeMillis();
                    } else {
                        time = 2000 + m.getTimeStamp() - System.currentTimeMillis();
                    }
                    if (time <= 100) {
                        ready = true;
                    }
                }
            }
        }

    }

}