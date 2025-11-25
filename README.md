# Android ZIP Gallery Viewer

A privacy-focused Android application for viewing images inside ZIP archives without extracting them to shared storage.

## Features

- View images from ZIP archives
- Password-protected archive support
- Ephemeral data - all extracted content is deleted on app exit
- Material Design 3 UI

## Requirements

- Android API 35+
- Kotlin 1.9.22+

## Build

This project uses Gradle. To build:

```bash
./gradlew build
```

## Architecture

The project follows MVVM architecture with:
- Jetpack Compose for UI
- Hilt for dependency injection
- Kotlin Coroutines for async operations

## License

TBD
