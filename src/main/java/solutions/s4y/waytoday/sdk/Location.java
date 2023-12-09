package solutions.s4y.waytoday.sdk;

import solutions.s4y.waytoday.grpc.LocationOuterClass;

/**
 * A class to represent a location to be uploaded to the WayToday service
 */
public class Location {
    public final String id;
    public final String tid;
    public final long lat;
    public final long lon;
    public final long alt;
    public final long bear;
    public final long ts;
    public final long batp;
    public final boolean bats;
    public final String provider;
    public final long speed;
    public final long acc;
    public final String sid;
    public Location(String tid, long lat, long lon, long alt, long bear, long ts, long batp, boolean bats, String provider, long speed, long acc) {
        this.id = "";
        this.tid = tid;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.bear = bear;
        this.ts = ts;
        this.batp = batp;
        this.bats = bats;
        this.provider = provider;
        this.speed = speed;
        this.acc = acc;
        // Server side assigned ID
        this.sid = "";
    }

    public Location(String id, String tid, long lat, long lon, long alt, long bear, long ts, long batp, boolean bats, String provider, long speed, long acc) {
        this.id = id;
        this.tid = tid;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.bear = bear;
        this.ts = ts;
        this.batp = batp;
        this.bats = bats;
        this.provider = provider;
        this.speed = speed;
        this.acc = acc;
        // Server side assigned ID
        this.sid = "";
    }

    public Location(String tid, long lat, long lon, long alt, long bear, long ts, long batp, boolean bats, long speed, long acc) {
        this.id = "";
        this.tid = tid;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.bear = bear;
        this.ts = ts;
        this.batp = batp;
        this.bats = bats;
        // will be set by GrpcClient
        this.provider = "";
        this.speed = speed;
        this.acc = acc;
        // Server side assigned ID
        this.sid = "";
    }
    public Location(String id, String tid, long lat, long lon, long alt, long bear, long ts, long batp, boolean bats, long speed, long acc) {
        this.id = id;
        this.tid = tid;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.bear = bear;
        this.ts = ts;
        this.batp = batp;
        this.bats = bats;
        // will be set by GrpcClient
        this.provider = "";
        this.speed = speed;
        this.acc = acc;
        // Server side assigned ID
        this.sid = "";
    }

    /**
     * Used internally by the SDK when decoding from the protobuf format
     */
    Location(String id, String tid, long lat, long lon, long alt, long bear, long ts, long batp, boolean bats, String provider, long speed, long acc, String sid) {
        this.id = id;
        this.tid = tid;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.bear = bear;
        this.ts = ts;
        this.batp = batp;
        this.bats = bats;
        this.provider = provider;
        this.speed = speed;
        this.acc = acc;
        this.sid = sid;
    }

    /**
     * Used in tests only
     */
    Location(String id, String tid, long lat, long lon, long alt, long bear, long ts, long batp, boolean bats, long speed, long acc, String sid) {
        this.id = id;
        this.tid = tid;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.bear = bear;
        this.ts = ts;
        this.batp = batp;
        this.bats = bats;
        this.provider = "";
        this.speed = speed;
        this.acc = acc;
        this.sid = sid;
    }

}
