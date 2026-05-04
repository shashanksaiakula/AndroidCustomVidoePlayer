# Android Custom Video Player Library

A powerful and customizable Android library for listing and playing local videos using Jetpack Compose and Media3 (ExoPlayer).

## Features

- 📱 **Local Media Discovery**: Automatically fetch and list videos from the device storage.
- 🎬 **Modern Video Playback**: Built on top of AndroidX Media3 (ExoPlayer) for reliable playback.
- 📝 **Video Notes**: Add and view time-stamped notes on specific videos.
- 🎨 **Jetpack Compose**: Fully built with Compose for modern UI development.
- 💉 **Hilt Support**: Ready for Dependency Injection out of the box.

## Installation

### 1. Add the JitPack repository
Add this to your `settings.gradle.kts` (or project-level `build.gradle`):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Add the dependency
Add the following to your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.YOUR_GITHUB_USERNAME:YOUR_REPOSITORY_NAME:v1.0.0")
}
```
*Note: Replace `YOUR_GITHUB_USERNAME` and `YOUR_REPOSITORY_NAME` with your actual GitHub details.*

## Quick Start

To use the media library screen in your app:

```kotlin
@Composable
fun MyScreen() {
    MediaLibraryScreen(
        onVideoSelected = { video ->
            // Handle video selection
        }
    )
}
```

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
