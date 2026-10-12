# Status Saver Project

An Android status-media utility organized into UI, ViewModels, data, models, and utility packages.

## Overview

This README documents the checked-in Android project and provides a practical guide to reviewing, configuring, building, and validating it. Implementation and dependency details come from the repository source and Gradle configuration.

## Feature map

- Status, saved-status, media, and settings fragments.
- Separate activity and adapter packages for presentation.
- Media3 playback, Glide image loading, DocumentFile/storage helper dependencies, and permission utilities.

## Technology stack

- **Language:** Kotlin / Android.
- XML layouts / View Binding
- Hilt dependency injection
- Kotlin Coroutines
- AndroidX Lifecycle / ViewModel
- Retrofit / Gson
- OkHttp
- Room
- Media3 / ExoPlayer
- Firebase integrations
- Glide / Coil image loading

| Build setting | Value |
| --- | --- |
| Application ID | `com.example.statussaver` |
| Minimum Android API | 24 |
| Compile SDK | 36 (minor API level 1) |
| Target SDK | 36 |
| Default branch at documentation time | `master` |

The Java/Kotlin bytecode target in the app build is separate from the JDK used to run Gradle. Use a Gradle JDK compatible with the checked-in Android Gradle Plugin and wrapper.

## Project structure

| Location | Purpose |
| --- | --- |
| `app/` | Android application module |
| `app/src/main/` | Main source, resources, and manifest |
| `app/src/test/` | Local JVM test source set, when populated |
| `app/src/androidTest/` | Device/emulator test source set, when populated |
| `gradle/` | Gradle wrapper and dependency configuration |
| Root Gradle files | Plugin, repository, and module configuration |

Main source areas:

- `data`
- `models`
- `ui`
- `utils`
- `viewmodels`

## Getting started

1. Clone this repository using an account with access.
2. Open the repository root in Android Studio.
3. Install the SDK platform requested by the checked-in app build; accept dependency sync using a compatible Gradle JDK.
4. Configure the services required by the feature you intend to run, then select the `app` configuration and a device meeting the minimum API requirement.

```powershell
git clone https://github.com/Muhammad-muaz-dev/Status_Saver_Project.git
cd Status_Saver_Project
.\gradlew.bat :app:assembleDebug
```

On macOS/Linux, use `./gradlew :app:assembleDebug` (set executable permission on the wrapper if required). The APK is normally under `app/build/outputs/apk/debug/`. Building the debug variant avoids the release-distribution workflow, but Gradle configuration must still resolve correctly.

## Configuration

- Review `app/build.gradle.kts`, the root build, and `settings.gradle.kts` before changing SDK, plugins, or dependencies.
- Use the Firebase project configuration that matches this application ID. Obtain the appropriate `google-services.json` from the project maintainer when a different environment is needed.
- Check the data/API or network implementation for service base URLs and required configuration. Use your authorized development environment for API-dependent flows.
- Advertising, billing, and Firebase-backed behavior may depend on service configuration, consent, a suitable test account, and network availability. Use service test modes for development.
- Release signing is a separate setup step. Use authorized signing configuration; credentials and keystore contents are intentionally not reproduced here.

## Validation

The build declares test dependencies. Check the existing test source sets before assuming they cover product behavior. These commands are a development guide; a successful build or test run is not asserted by this documentation update.

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

Connected tests require a running emulator or connected device. If a task is unavailable in this checkout, inspect `:app:tasks` and use the matching variant task.

**Manual verification:** Grant media access, inspect status media, save an item, and reopen it from the saved list. Test denied/revoked access, missing files, refresh, image preview, and video playback.

## Troubleshooting

- **Gradle sync fails:** check the first reported error, wrapper/AGP/JDK compatibility, SDK installation, and access to configured dependency repositories.
- **Service-backed feature fails:** confirm configuration and connectivity, then inspect Logcat at the point of failure.
- **Media/storage feature behaves differently on another Android version:** verify runtime permissions and the relevant storage-access behavior on that device.
- **Release build fails:** check signing and release-only configuration independently of debug behavior.

## Development workflow

Keep UI, data, and supporting logic in the existing source organization. For each change, document the trigger and expected result, run relevant checks, and add focused regression coverage where the behavior warrants it. Include screenshots or a short recording for UI changes, using test data.

## Ownership and reuse

Maintained in Muhammad Muaz’s GitHub workspace. See [the developer profile](https://github.com/Muhammad-muaz-dev) for public Android work. Repository visibility does not grant reuse rights; refer to an existing license or obtain permission from the rights holder before redistributing code or assets.
