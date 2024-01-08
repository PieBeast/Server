package cpen221.mp3.server;

import java.util.List;

public class Predictor {

    private final List<Double> timestamps;
    private final List<Double> values;
    private double sumTimestamp1;
    private double sumValues1;
    private double sumTimestamp2;
    private double sumValues2;

//    public Predictor (List<Boolean> values, List<Double> timestamps) {
//        this.sumTimestamp = 0;
//        this.sumValues = 0;
//    }

    // y = mx + b
    public Predictor(List<Double> timestamps, List<Double> values) {
        this.values = values;
        this.timestamps = timestamps;
        this.sumTimestamp1 = 0;
        this.sumValues1 = 0;
        this.sumTimestamp2 = 0;
        this.sumValues2 = 0;
    }

    public double linearRegression() {
        this.sumTimestamp1 = timestamps.parallelStream().reduce(0.0, Double::sum);
        this.sumTimestamp2 = timestamps.parallelStream().map(t -> t * t).reduce(0.0, Double::sum);

        this.sumValues1 = values.parallelStream().reduce(0.0, Double::sum);
        this.sumValues2 = values.parallelStream().map(t -> t * t).reduce(0.0, Double::sum);

//        double m = ()
        return 0.0;
    }
}
