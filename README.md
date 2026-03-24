# LinkArena Android

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
