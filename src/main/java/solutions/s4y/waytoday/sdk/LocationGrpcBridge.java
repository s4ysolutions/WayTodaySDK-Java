package solutions.s4y.waytoday.sdk;

import solutions.s4y.waytoday.grpc.LocationOuterClass;

class LocationGrpcBridge {
    static Location fromProto(LocationOuterClass.Location location) {
        return new Location(
                location.getID(),
                location.getTid(),
                location.getLat(),
                location.getLon(),
                location.getAlt(),
                location.getBear(),
                location.getTs(),
                location.getBatp(),
                location.getBats(),
                location.getProvider(),
                location.getSpeed(),
                location.getAcc(),
                location.getSid()
        );
    }
    static LocationOuterClass.Location toProto(Location location) {
        return LocationOuterClass.Location.newBuilder()
                .setID(location.id)
                .setTid(location.tid)
                .setLat(location.lat)
                .setLon(location.lon)
                .setAlt(location.alt)
                .setBear(location.bear)
                .setTs(location.ts)
                .setBatp(location.batp)
                .setBats(location.bats)
                .setProvider(location.provider)
                .setSpeed(location.speed)
                .setAcc(location.acc)
                .setSid(location.sid)
                .build();
    }
}
