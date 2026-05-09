# Android Zip Gallery Viewer

### ariefbayu's [Android-ZIP-Gallery-Viewer]https://github.com/ariefbayu/Android-ZIP-Gallery-Viewer with some fixes and features

## Added

- Support for encrypted 7z archives (p7zip fallback)

- Image sticking to screen borders
- Immersive mode
- Double-tap zoom at tap position and double-tap unzoom
- Image preloading

## Fixed

- Crashing due to Firebase (it is removed)
- Not full resolution of images

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
- **Persistent exit notification** - Tap notification to quickly initiate app closure and data cleanup
- **Exit confirmation dialog** - Prevents accidental data loss with confirmation before cleanup
- **Randomize image order** - Toggle between alphabetical and randomized image viewing while keeping folders organized

## Recent Implementations

### Privacy & Session Management

**Persistent Notification**
- Ongoing notification displayed after archive extraction
- Tap notification to bring app to foreground and initiate exit flow
- Notification channel created at application startup
- Automatically dismissed on app exit

**Exit Confirmation & Cleanup**
- Exit confirmation dialog triggered by back button at root or notification tap
- Two-button dialog: "Cancel" to dismiss, "Yes, clear and exit" to proceed
- CleanupService clears all extracted content on confirmation
- Notification dismissed before app closure
- Activity programmatically finished after cleanup

### Enhanced Gallery Features

**Randomize Image Order**
- Shuffle button in TopAppBar alongside grid/list toggle
- Folders always displayed first in alphabetical order
- Images randomized when shuffle is active, alphabetical when inactive
- Visual feedback: filled shuffle icon with primary color when active, outlined with default color when inactive
- Randomized order persists across folder navigation within session
- Full-screen image viewer navigation follows randomized sequence
- Toggle functionality implemented with StateFlow state management

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

## License

[Add license information here]
