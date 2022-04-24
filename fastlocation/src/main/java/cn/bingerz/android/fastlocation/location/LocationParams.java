package cn.bingerz.android.fastlocation.location;

import android.location.Location;

/**
 * @author hanson
 */
public class LocationParams {

    public static final LocationParams HIGH_ACCURACY = new Builder()
            .setAccuracy(LocationAccuracy.HIGH)
            .setInterval(500)
            .setDistance(0)
            .setAcceptableTime(0)
            .setAcceptableAccuracy(0)
            .build();

    public static final LocationParams MEDIUM_ACCURACY = new Builder()
            .setAccuracy(LocationAccuracy.MEDIUM)
            .setInterval(2500)
            .setDistance(150)
            .setAcceptableTime(60 * 1000)
            .setAcceptableAccuracy(100)
            .build();

    public static final LocationParams LOW_ACCURACY = new Builder()
            .setAccuracy(LocationAccuracy.MEDIUM)
            .setInterval(5000)
            .setDistance(500)
            .setAcceptableTime(60 * 60 * 1000)
            .setAcceptableAccuracy(1000)
            .build();

    private LocationAccuracy accuracy;
    private long interval;
    private float distance;

    private long acceptableTime;
    private float acceptableAccuracy;

    LocationParams(LocationAccuracy accuracy, long interval, float distance,
                   long acceptableTime, float acceptableAccuracy) {
        this.accuracy = accuracy;
        this.interval = interval;
        this.distance = distance;
        this.acceptableTime = acceptableTime;
        this.acceptableAccuracy = acceptableAccuracy;
    }

    public float getDistance() {
        return distance;
    }

    public long getInterval() {
        return interval;
    }

    public LocationAccuracy getAccuracy() {
        return accuracy;
    }

    public long getAcceptableTime() {
        return acceptableTime;
    }

    public float getAcceptableAccuracy() {
        return acceptableAccuracy;
    }

    public boolean isAcceptableTime(Location location) {
        return location != null && location.getTime() >= System.currentTimeMillis() - acceptableTime;
    }

    public boolean isAcceptableAccuracy(Location location) {
        return location != null && location.hasAccuracy() && location.getAccuracy() <= acceptableAccuracy;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LocationParams)) return false;

        LocationParams that = (LocationParams) obj;

        return Float.compare(that.distance, distance) == 0
                && interval == that.interval
                && accuracy == that.accuracy;
    }

    @Override
    public int hashCode() {
        int result = (int) (interval ^ (interval >>> 32));
        result = 31 * result + (distance != +0.0f ? Float.floatToIntBits(distance) : 0);
        result = 31 * result + accuracy.hashCode();
        return result;
    }

    public static class Builder {
        private LocationAccuracy accuracy;
        private long interval;
        private float distance;

        private long acceptableTime;
        private float acceptableAccuracy;

        public Builder setAccuracy(LocationAccuracy accuracy) {
            this.accuracy = accuracy;
            return this;
        }

        public Builder setInterval(long interval) {
            this.interval = interval;
            return this;
        }

        public Builder setDistance(float distance) {
            this.distance = distance;
            return this;
        }

        public Builder setAcceptableTime(long acceptableTime) {
            this.acceptableTime = acceptableTime;
            return this;
        }

        public Builder setAcceptableAccuracy(float acceptableAccuracy) {
            this.acceptableAccuracy = acceptableAccuracy;
            return this;
        }

        public LocationParams build() {
            return new LocationParams(accuracy, interval, distance, acceptableTime, acceptableAccuracy);
        }
    }
}
