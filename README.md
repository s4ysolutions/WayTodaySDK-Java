# WayToday Java SDK

Java client library for the [way.today](https://github.com/s4ysolutions/way-today) real-time GPS tracking service. Trackers push locations via gRPC; subscribers receive live updates via WebSocket.

## Requirements

- Java 8+
- Maven 3.6+

## Add Dependency

[![](https://jitpack.io/v/s4ysolutions/WayTodaySDK-Java.svg)](https://jitpack.io/#s4ysolutions/WayTodaySDK-Java)

**Maven** — add JitPack repository and dependency:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.s4ysolutions</groupId>
    <artifactId>WayTodaySDK-Java</artifactId>
    <version>3.1.0-alpha1</version>
</dependency>
```

**Gradle:**

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.s4ysolutions:WayTodaySDK-Java:3.1.0-alpha1'
}
```

## Quick Start

```java
// 1. Implement IPersistedState to store/retrieve the tracker ID
IPersistedState state = new IPersistedState() {
    private String trackerId = "";
    @Override public String getTrackerId() { return trackerId; }
    @Override public void setTrackerId(String id) { trackerId = id; }
    @Override public boolean hasTrackerId() { return !trackerId.isEmpty(); }
};

WayTodayClient client = new WayTodayClient(state);

// 2. Request a tracker ID (call once; persist the returned ID)
String tid = client.requestNewTrackerId(null);

// 3. Enqueue locations and upload
Location loc = new Location(lat, lon, alt, bearing, speed, accuracy, timestamp);
client.enqueueLocationToUpload(loc);
client.uploadLocations();

// 4. Listen to status changes (optional)
client.addTrackIdChangeListener(id -> System.out.println("New tracker ID: " + id));
client.addUploadingLocationsStatusChangeListener(status -> System.out.println("Status: " + status));
client.addErrorsListener(err -> System.err.println("Error: " + err.getMessage()));
```

For async usage see [`WayTodayClientAsync`](src/main/java/solutions/s4y/waytoday/sdk/WayTodayClientAsync.java).

## API

| Method | Description |
|---|---|
| `requestNewTrackerId(prevId)` | Allocate a tracker ID from the server (100–9999). Pass `null` on first call. |
| `enqueueLocationToUpload(location)` | Add a location to the upload queue (max 500 in memory). |
| `uploadLocations()` | Upload queued locations in batches of 16. Blocks until done. |
| `getCurrentTrackerId()` | Return current tracker ID from persisted state. |
| `getUploadingLocationsStatus()` | `EMPTY` / `QUEUED` / `UPLOADING` / `ERROR`. |

Full API: [`WayTodayClient.java`](src/main/java/solutions/s4y/waytoday/sdk/WayTodayClient.java)

## Android

For Android use [WayTodaySDK-Android](https://github.com/s4ysolutions/WayTodaySDK-Android), which wraps this SDK with WorkManager-based background uploads.

## Build & Test

Proto definitions live in a submodule. After cloning, initialize it:

```bash
git submodule init
git submodule update
```

Then build:

```bash
mvn verify
```

Artifact: `target/waytoday-sdk-java-[version].jar`

## License

[Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)
