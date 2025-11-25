# Android Zip Gallery Viewer

## Overview

The Android Zip Gallery Viewer addresses a user need for a private method of viewing image archives. Currently, users often have to extract zip files to their device's public storage, making the images visible to other applications and potentially leaving sensitive data behind. This application solves that problem by treating image archives as temporary, self-contained sessions. All content is loaded into the application's private, sandboxed storage and is automatically and irrevocably deleted upon closing the app.

The core value proposition is **privacy and security through ephemerality**.

## Core Features

- **View images from `.zip` and `.7z` archives** - Support for multiple archive formats with efficient extraction
- **Password-protected archive support** - Secure handling of encrypted archives with password input
- **Automatic content cleanup** - All extracted content is deleted on exit, leaving no trace
- **Simple, intuitive gallery** - Easy navigation through folders and images within archives
- **Private sandboxed storage** - Content never exposed to public storage or other apps
- **Offline-first** - No network connectivity required, fully local operation

## Development Setup

### Requirements

- **Android Studio** - Latest stable version
- **Android SDK** - API level 35 or higher
- **Kotlin** - 1.9.22 or higher (bundled with Android Studio)

### Environment Configuration

1. Install Android Studio from [https://developer.android.com/studio](https://developer.android.com/studio)
2. Open Android Studio and install the following via SDK Manager:
   - Android SDK Platform 35
   - Android SDK Build-Tools
   - Android Emulator (for testing)

## Building and Running

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Android-ZIP-Gallery-Viewer
   ```

2. **Open the project in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned repository directory and select it

3. **Let Gradle sync dependencies**
   - Android Studio will automatically detect the Gradle configuration
   - Wait for the sync process to complete (this may take a few minutes on first run)
   - Resolve any SDK version prompts if they appear

4. **Run the application**
   - Connect an Android device via USB with debugging enabled, OR
   - Create/start an Android emulator (API 35+)
   - Click the "Run" button (green play icon) or press `Shift+F10`
   - Select your target device from the deployment dialog
   - Wait for the build to complete and the app to launch

## Testing

Run all tests using Android Studio's test runner:
- Right-click on the `app` module
- Select "Run 'All Tests'"

Alternatively, use Gradle from the terminal:
```bash
./gradlew build
./gradlew test           # Unit tests
./gradlew connectedTest  # Instrumentation tests (requires device/emulator)
```

## Architecture

This application follows MVVM architecture with:
- **Jetpack Compose** for declarative UI
- **Hilt** for dependency injection
- **StateFlow** for reactive state management
- **7-Zip-JBinding-4Android** for archive extraction

For detailed architecture documentation, see `docs/architecture.md`.

## License

[Add license information here]
