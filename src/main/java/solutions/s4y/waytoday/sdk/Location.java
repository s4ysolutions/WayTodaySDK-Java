package solutions.s4y.waytoday.sdk;

/**
 * A class to represent a location to be uploaded to the WayToday service
 */
public class Location {
    private static final int FLOAT_MULT = 10000000;

    private static int i(double f) {
        return ((int) Math.round(f * FLOAT_MULT));
    }

    private static float f(int i) {
        return (float) i / FLOAT_MULT;
    }

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

    public Location(String tid, double lat, double lon, double alt, long bear, long ts, long batp, boolean bats, String provider, double speed, double acc) {
        this("", tid, i(lat), i(lon), i(alt), bear, ts, batp, bats, provider, i(speed), i(acc), "");
    }
}
