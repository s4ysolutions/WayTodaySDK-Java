# WayTodaySDK-Java — Agent Instructions

## Versions

- This SDK: `3.1.0-alpha1` (pom.xml)
- Compatible Android SDK: [WayTodaySDK-Android](https://github.com/s4ysolutions/WayTodaySDK-Android) `4.3.0+`
- Proto definitions: git submodule at `src/main/proto` (repo: WayTodayProtobuffers)

## Compatibility rules

When bumping this SDK's version:
1. Update `<version>` in `pom.xml`
2. Update version in `README.md` dependency snippets
3. Update the Android SDK: bump `WayTodaySDK-Java` dependency version in `WayTodaySDK-Android/waytoday-sdk/build.gradle` and bump Android SDK minor version
4. Update the compatible Android SDK version referenced in this repo's `README.md` Android section

## Submodule

Proto files live in `src/main/proto` as a git submodule.
After cloning: `git submodule init && git submodule update`

## Build

```bash
mvn verify
```

Integration tests (`GrpcClientIntegrationTest`) require a live gRPC server on `:9001` — expected to fail in CI without it.

## Git

Artifacts published via JitPack on tag push. Tag must match pom.xml version exactly.
