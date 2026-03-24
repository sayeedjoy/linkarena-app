# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

LinkArena is a native Android app for saving, organizing, and managing bookmarks with groups, search, metadata fetching, and sync support. Built with Jetpack Compose and Material 3.

## Build Commands

```bash
# Build debug APK
./gradlew :app:assembleDebug

# Run unit tests
./gradlew :app:testDebugUnitTest

# Run instrumented tests
./gradlew :app:connectedDebugAndroidTest

# Clean and rebuild
./gradlew :app:clean :app:assembleDebug
```

Windows: Replace `./gradlew` with `.\gradlew.bat`

## Architecture

Clean Architecture with MVVM pattern:

```
app/src/main/java/com/sayeedjoy/linkarena/
├── data/                    # Data layer
│   ├── local/
│   │   ├── db/            # Room database, DAOs, entities
│   │   └── datastore/     # PreferencesManager (DataStore)
│   ├── remote/
│   │   ├── api/           # Retrofit API interface
│   │   ├── auth/          # AuthInterceptor
│   │   └── dto/           # Data transfer objects
│   └── repository/        # Repository implementations
├── di/                     # Hilt modules (NetworkModule, RepositoryModule, etc.)
├── domain/                 # Domain layer
│   ├── model/             # Domain models (Bookmark, Group, UserSettings, ThemeMode)
│   ├── repository/        # Repository contracts (interfaces)
│   └── usecase/          # Business logic use cases
├── ui/                     # UI layer
│   ├── auth/              # Login, Signup, ForgotPassword, ResetPassword screens
│   ├── bookmark/           # AddBookmark, BookmarkDetail screens + ViewModels
│   ├── groups/            # GroupsScreen, CreateGroupDialog, EditGroupDialog
│   ├── home/              # HomeScreen + HomeViewModel
│   ├── settings/          # SettingsScreen + SettingsViewModel
│   ├── components/        # Reusable UI components (BookmarkCard, GroupChip, etc.)
│   ├── navigation/        # Screen routes, NavGraphs (AuthNavGraph, MainNavGraph)
│   └── theme/             # Theme configuration (Color, Type, Theme)
├── util/                   # Utilities (Constants, Extensions, NetworkResult)
├── LinkArenaApplication.kt
└── MainActivity.kt
```

## Key Patterns

### NetworkResult
API responses are wrapped in a sealed class:
```kotlin
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
}
```

### Use Cases
Use cases follow the pattern of injecting a repository and providing a single `invoke` operator:
```kotlin
class CreateBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(url: String, title: String?, ...): NetworkResult<Bookmark> { ... }
}
```

### Navigation
Routes are defined as sealed class objects in `ui/navigation/Screen.kt`. The `AddBookmark` screen supports an optional `sharedUrl` parameter for the share-to-save flow.

### Dependency Injection
Hilt modules in `di/`:
- `NetworkModule` - OkHttpClient, Retrofit, Json, CookieJar (in-memory)
- `RepositoryModule` - Binds repository implementations to contracts
- `DatabaseModule` - Room database and DAOs
- `UseCaseModule` - Provides use case instances

## Tech Stack

- **Kotlin 2.2.10** with Compose compiler plugin
- **Jetpack Compose** BOM 2024.09.00 + Material 3
- **Hilt 2.59.2** for dependency injection
- **Retrofit 2.10.0** + **OkHttp 4.12.0** + **Kotlinx Serialization 1.6.0**
- **Room 2.7.2** for local database
- **DataStore** for preferences
- **Navigation Compose 2.7.6**
- **Coil 2.5.0** for image loading
- **Material Kolor 4.1.1** for dynamic theming

## Configuration Notes

- **API Base URL**: Configured in `NetworkModule.kt` (currently `https://linkarena.app/`)
- **Database Name**: `linkarena_database` (defined in `Constants.kt`)
- **Min SDK**: 24, **Target SDK**: 36
- **Java Version**: 11

## Share-to-Save Flow

External apps can share URLs to LinkArena via Android's share intent. The `AddBookmark` screen receives the shared URL via navigation argument and auto-fetches metadata.

## Testing

- Unit tests: `app/src/test/java/com/sayeedjoy/linkarena/`
- Instrumented tests: `app/src/androidTest/java/com/sayeedjoy/linkarena/`
