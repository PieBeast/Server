package cpen221.mp3.server;

import cpen221.mp3.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

enum DoubleOperator {
    EQUALS,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_OR_EQUALS,
    LESS_THAN_OR_EQUALS
}

enum BooleanOperator {
    EQUALS,
    NOT_EQUALS
}

public class Filter {

    private BooleanOperator operatorB;
    private DoubleOperator operatorD;
    private boolean valueB;
    private double valueD;
    private String field;
    private final List<Filter> filters;
    private final FilterType filterType;

    /**
     * Constructs a filter that compares the boolean (actuator) event value
     * to the given boolean value using the given BooleanOperator.
     * (X (BooleanOperator) value), where X is the event's value passed by satisfies or sift methods.
     * A BooleanOperator can be one of the following:
     * <p>
     * BooleanOperator.EQUALS
     * BooleanOperator.NOT_EQUALS
     *
     * @param operator the BooleanOperator to use to compare the event value with the given value
     * @param value    the boolean value to match
     */
    public Filter(BooleanOperator operator, boolean value) {
        this.operatorB = operator;
        this.valueB = value;
        this.filterType = FilterType.BOOL;
        this.filters = new ArrayList<>();
    }

    /**
     * Constructs a filter that compares a double field in events
     * with the given double value using the given DoubleOperator.
     * (X (DoubleOperator) value), where X is the event's value passed by satisfies or sift methods.
     * A DoubleOperator can be one of the following:
     * <p>
     * DoubleOperator.EQUALS
     * DoubleOperator.GREATER_THAN
     * DoubleOperator.LESS_THAN
     * DoubleOperator.GREATER_THAN_OR_EQUALS
     * DoubleOperator.LESS_THAN_OR_EQUALS
     * <p>
     * For non-double (boolean) value events, the satisfies method should return false.
     *
     * @param field    the field to match (event "value" or event "timestamp")
     * @param operator the DoubleOperator to use to compare the event value with the given value
     * @param value    the double value to match
     * @throws IllegalArgumentException if the given field is not "value" or "timestamp"
     */
    public Filter(String field, DoubleOperator operator, double value) {
        if (!field.equals("value") && !field.equals("timestamp")) {
            throw new IllegalArgumentException();
        }
        this.field = field;
        this.operatorD = operator;
        this.valueD = value;
        this.filters = new ArrayList<>();
        this.filterType = FilterType.DOUBLE;
    }

    /**
     * A filter can be composed of other filters.
     * in this case, the filter should satisfy all the filters in the list.
     * Constructs a complex filter composed of other filters.
     *
     * @param filters the list of filters to use in the composition
     */
    public Filter(List<Filter> filters) {
        this.filters = filters;
        this.filterType = FilterType.MULTI;
    }

    /**
     * Returns true if the given event satisfies the filter criteria.
     *
     * @param event the event to check
     * @return true if the event satisfies the filter criteria, false otherwise
     */
    public boolean satisfies(Event event) {
        switch (this.filterType) {
            case BOOL -> {
                return satisfiesBoolOperator(this.operatorB, event);
            }
            case DOUBLE -> {
                return satisfiesDoubleOperator(this.operatorD, this.field, event);
            }
            case MULTI -> {
                return satisfiesMultipleFilters(filters, event);
            }
        }
        return false;
    }

    /**
     * Returns true if the given list of events satisfies the filter criteria.
     *
     * @param events the list of events to check
     * @return true if every event in the list satisfies the filter criteria, false otherwise
     */
    public boolean satisfies(List<Event> events) {
        for (Event e : events) {
            if (!(satisfies(e))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a new event if it satisfies the filter criteria.
     * If the given event does not satisfy the filter criteria, then this method should return null.
     *
     * @param event the event to sift
     * @return a new event if it satisfies the filter criteria, null otherwise
     */
    public Event sift(Event event) {
        return (satisfies(event)) ? event : null;
    }

    /**
     * Returns a list of events that contains only the events in the given list that satisfy the filter criteria.
     * If no events in the given list satisfy the filter criteria, then this method should return an empty list.
     *
     * @param events the list of events to sift
     * @return a list of events that contains only the events in the given list that satisfy the filter criteria
     * or an empty list if no events in the given list satisfy the filter criteria
     */
    public List<Event> sift(List<Event> events) {
        // idk if parallelStream breaks stuff so plz check
        return events.parallelStream().filter(this::satisfies).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        switch (this.filterType) {
            case BOOL -> {
                return "Filter{" +
                        "BooleanOperator=" + this.operatorB +
                        ",Value=" + this.valueB +
                        '}';
            }
            case DOUBLE -> {
                return "Filter{" +
                        "DoubleOperator=" + this.operatorD +
                        ",Field=" + this.field +
                        ",Value=" + this.valueD +
                        '}';
            }
            case MULTI -> {
                ArrayList<Filter> extracted = extract(this);
                StringBuilder s = new StringBuilder("Filter{" + "Filters=");
                for (Filter f : extracted) {
                    s.append(f.toString());
                    s.append(",,");
                }
                //s.delete(s.lastIndexOf(",,"),s.lastIndexOf(",,") + 1);
                s.append("}");
                return new String(s);
            }
        }
        return null;
    }

    private ArrayList<Filter> extract(Filter filter) {
        ArrayList<Filter> basic = new ArrayList<>();
        if (filter.filterType.equals(FilterType.MULTI)) {
            for (Filter f : filter.filters) {
                basic.addAll(extract(f));
            }
        } else {
            basic.add(filter);
        }
        return basic;
    }

    /**
     * Checks if a given event satisfies a certain boolean operator of this filter (equals, not equals)
     * Compares the value of the event with this instance of filters boolean value
     *
     * @param operator The boolean operator to be applied
     * @param event    An event to compare, not null, and an ActuatorEvent.
     * @return true if the event satisfies the BooleanOperator, false otherwise
     */
    private boolean satisfiesBoolOperator(BooleanOperator operator, Event event) {
        switch (operator) {
            case EQUALS -> {
                return event.getValueBoolean() == this.valueB;
            }
            case NOT_EQUALS -> {
                return event.getValueBoolean() != this.valueB;
            }
        }
        return false;
    }

    /**
     * Checks if a given event satisfies a certain double operator of this filter
     * Compares either the timestamp or double value of the event with this
     * instance of filters values
     *
     * @param operator the double operator to be applied
     * @param field    which value to compare
     * @param event    An event to compare, not null, and a SensorEvent.
     * @return true if the event satisfies the double operator, false otherwise
     */
    private boolean satisfiesDoubleOperator(DoubleOperator operator, String field, Event event) {
        double matchValue;
        if (field.equals("timestamp")) {
            matchValue = event.getTimeStamp();
        } else {
            matchValue = event.getValueDouble();
        }
        switch (operator) {
            case EQUALS -> {
                return valueD == matchValue;
            }
            case LESS_THAN -> {
                return valueD > matchValue;
            }
            case GREATER_THAN -> {
                return valueD < matchValue;
            }
            case LESS_THAN_OR_EQUALS -> {
                return valueD >= matchValue;
            }
            case GREATER_THAN_OR_EQUALS -> {
                return valueD <= matchValue;
            }
        }
        return false;
    }

    /**
     * Checks if a list of filters satisfies a given event
     *
     * @param filters list of filters to check if each satisfies an event, not null
     * @param event   event to see if it satisfies the list of filters
     * @return true if the event satisfies all filters, false otherwise
     */

    private boolean satisfiesMultipleFilters(List<Filter> filters, Event event) {
        for (Filter filter : filters) {
            if (filter.filters.isEmpty()) {
                if (!filter.satisfies(event)) {
                    return false;
                }
            }
            if (!satisfiesMultipleFilters(filter.filters, event)) return false;
        }
        return true;
    }

    public enum FilterType {
        BOOL,
        DOUBLE,
        MULTI
    }
}
