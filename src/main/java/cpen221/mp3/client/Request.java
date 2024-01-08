package cpen221.mp3.client;

public class Request {
    private double timeStamp;
    private final RequestType requestType;
    private final RequestCommand requestCommand;
    private final String requestData;

    /**
     * Creates a request instance.
     *
     * @param requestType    The type of request being sent.
     * @param requestCommand The command of the request.
     * @param requestData    The data for the request, contains any value necessary for the command of the request.
     */
    public Request(RequestType requestType, RequestCommand requestCommand, String requestData) {
        this.timeStamp = System.currentTimeMillis();
        this.requestType = requestType;
        this.requestCommand = requestCommand;
        this.requestData = requestData;
    }

    public void setTimeStamp(double timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * Gets the timestamp of when the request was created
     *
     * @return the timestamp of the request
     */
    public double getTimeStamp() {
        return timeStamp;
    }

    /**
     * Gets the requestType
     *
     * @return requestType of current instance of request
     */
    public RequestType getRequestType() {
        return requestType;
    }

    /**
     * Gets the request command
     *
     * @return request command of current instance of request
     */
    public RequestCommand getRequestCommand() {
        return requestCommand;
    }

    /**
     * Gets the request data as a string
     * <p>
     * Request data for each request:
     * CONFIG_UPDATE_MAX_WAIT_TIME: clientId,new max wait time
     * CONTROL_SET_ACTUATOR_STATE: clientId,entityId,deserializedFilter,new value
     * CONTROL_TOGGLE_ACTUATOR_STATE: clientId,entityId,deserializedFilter,new value
     * CONTROL_NOTIFY_IF: clientId,deserializedFilter
     * ANALYSIS_GET_ALL_LOGS: clientId
     * ANALYSIS_GET_EVENTS_IN_WINDOW: clientId,deserializedTimeWindow
     * ANALYSIS_GET_ALL_ENTITIES: clientId
     * ANALYSIS_GET_LATEST_EVENTS: clientId,number of timestamps
     * ANALYSIS_GET_MOST_ACTIVE_ENTITY: clientId
     * PREDICT_NEXT_N_TIMESTAMPS: clientId,number of timestamps
     * PREDICT_NEXT_N_VALUES: clientId,number of values
     *
     * @return all the request data of current instance of request as a string
     */
    public String getRequestData() {
        // This implementation prob needs to be changed Task 1b
        return requestData;
    }

    /**
     * Gets the clientID of current request
     *
     * @return the clientID of current request as an integer
     */
    public int getClientID() {
        if (requestData.contains("Actuator{")) {
            String s = requestData.substring(requestData.indexOf(",ClientId=") + ",ClientId=".length());
            return Integer.parseInt(s.substring(0, s.indexOf(",")));
        }
        return Integer.parseInt(requestData.substring(0, requestData.indexOf(",")));
    }

    @Override
    public String toString() {
        return "Request{" +
                "TimeStamp=" + getTimeStamp() +
                ",RequestType=" + getRequestType() +
                ",RequestCommand=" + getRequestCommand() +
                ",RequestData=" + getRequestData() +
                "}.";
    }
}