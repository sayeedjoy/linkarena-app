# LinkArena Android

[![Kotlin](https://img.shields.io/badge/Kotlin-1E1E1E?style=for-the-badge&logo=kotlin&logoColor=7F52FF)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-1E1E1E?style=for-the-badge&logo=android&logoColor=3DDC84)](https://developer.android.com/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1E1E1E?style=for-the-badge&logo=jetpackcompose&logoColor=4285F4)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-1E1E1E?style=for-the-badge&logo=materialdesign&logoColor=00C4B3)](https://m3.material.io/)
[![Hilt](https://img.shields.io/badge/Hilt-1E1E1E?style=for-the-badge&logo=dagger&logoColor=F7DF1E)](https://developer.android.com/training/dependency-injection/hilt-android)
[![Room](https://img.shields.io/badge/Room-1E1E1E?style=for-the-badge&logo=sqlite&logoColor=003B57)](https://developer.android.com/training/data-storage/room)
[![DataStore](https://img.shields.io/badge/DataStore-1E1E1E?style=for-the-badge&logo=android&logoColor=3DDC84)](https://developer.android.com/topic/libraries/architecture/datastore)

LinkArena is a Jetpack Compose Android app for saving, organizing, and managing bookmarks with groups, search, metadata, and sync support.

## Features

- Email/password authentication
- Bookmark CRUD (create, edit, delete)
- Group-based bookmark organization
- Search bookmarks
- External share support:
  - Share a link from browser/other apps to LinkArena
  - Opens Add Bookmark with prefilled URL
  - Preserves shared URL through login if user is logged out
- Auto metadata fetch in Add Bookmark:
  - Automatically fetches title/description/favicon from URL input
- Theme settings:
  - System / Light / Dark mode
- Material 3 UI with dynamic theming

## Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- Hilt (DI)
- Retrofit + OkHttp + Kotlinx Serialization
- Room (local storage)
- DataStore (preferences)
- Navigation Compose
- Coil (images/favicons)

## Requirements

- Android Studio (latest stable)
- JDK 11+
- `JAVA_HOME` set to your JDK path
- Android SDK with API 36

## Local Setup

1. Clone the repository.
2. Open the project in Android Studio.
3. Ensure `local.properties` points to your Android SDK.
4. Ensure environment variable `JAVA_HOME` is set.
5. Sync Gradle and run:

```bash
./gradlew :app:assembleDebug
```

Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

## Run

- Run the `app` module from Android Studio on emulator or device.

## Share-to-Save Flow

1. Open any app (browser, social, etc.) and tap Share on a link.
2. Choose **LinkArena**.
3. App opens Add Bookmark with URL prefilled.
4. Metadata is fetched automatically.
5. Tap **Add Bookmark** to save.

If logged out, LinkArena keeps the shared URL pending and continues after login.

## Project Structure

- `app/src/main/java/com/sayeedjoy/linkarena/ui` - Compose screens/components/navigation
- `app/src/main/java/com/sayeedjoy/linkarena/domain` - models/use cases/repository contracts
- `app/src/main/java/com/sayeedjoy/linkarena/data` - api, db, repository implementations
- `app/src/main/java/com/sayeedjoy/linkarena/di` - Hilt modules

## Notes

- Base API URL is configured in `NetworkModule`.
- Metadata extraction currently runs client-side from the provided URL in Add Bookmark.
