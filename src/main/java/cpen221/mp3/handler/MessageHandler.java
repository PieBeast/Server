package cpen221.mp3.handler;

import cpen221.mp3.server.Message;
import cpen221.mp3.server.Server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class MessageHandler {
    private ServerSocket serverSocket;
    private final int port;
    private final BlockingQueue<Message> queue;
    private final ConcurrentHashMap<Integer, Server> serverMap;
    // Abstraction function:
    //    Represents a message handler, where queue contains message to be sent,
    //    queue.poll corresponds with the message that was received the latest,
    //    serverMap contains the server instance, and a client ID to represent the current servers,
    //    port is the port of connection, and serverSocket listens for connections with port

    // Rep Invariant:
    //    0 <= port <= 65535, serverSocket, queue, serverMap all != null,
    //    if unique client id, unique server in serverMap

    /**
     * Creates a new instance of MessageHandler
     *
     * @param port the port of connection
     */
    public MessageHandler(int port) {
        this.port = port;
        serverMap = new ConcurrentHashMap<>();
        queue = new PriorityBlockingQueue<>(1, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                double o1time, o2time;
                double current = System.currentTimeMillis();
                if (serverMap.containsKey(o1.getClientID())) {
                    o1time = serverMap.get(o1.getClientID()).getMaxWaitTime() * 1000 + o1.getTimeStamp();
                } else {
                    o1time = 2000 + o1.getTimeStamp() - current;
                }
                if (serverMap.containsKey(o2.getClientID())) {
                    o2time = serverMap.get(o2.getClientID()).getMaxWaitTime() * 1000 + o2.getTimeStamp();
                } else {
                    o2time = 2000 + o2.getTimeStamp() - current;
                }
                double diff = o1time - o2time;
                if (diff > 0) {
                    return 1;
                }
                if (diff < 0) {
                    return -1;
                }
                return 0;
            }
        });
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
            while (true) {
                System.out.println("Waiting for Request or Event");
                Socket incomingSocket = serverSocket.accept();
                System.out.println("Client/Entity connected: " + incomingSocket.getInetAddress().getHostAddress() + " " + incomingSocket.getPort());
                // create a new thread to handle the client request or entity event
                Thread handlerThread = new Thread(new MessageHandlerThread(incomingSocket, serverMap, queue));
//                System.out.println("Started Thread");
                handlerThread.start();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // testing only
    public ConcurrentHashMap<Integer, Server> getServerMap() {
        return this.serverMap;
    }

//    public static void main(String[] args) {
//        // you would need to initialize the RequestHandler with the port number
//        // and then start it here
//        // Server s = new Server();
//
//        Client client = new Client(0, "test@test.com", "127.0.0.1", 1);
//        Server server = new Server(client);
//        MessageHandler mh = new MessageHandler(1, server);
//        mh.start();
//    }
}
