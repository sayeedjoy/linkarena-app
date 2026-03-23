# LinkArena Android App Plan

## Overview

Building an Android app using Kotlin and Jetpack Compose that connects to the LinkArena bookmarking platform API. The app allows users to manage bookmarks, organize them into groups, and sync across devices.

---

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 1.9+ |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| Networking | Retrofit + OkHttp + Kotlin Serialization |
| Dependency Injection | Hilt |
| Local Storage | Room |
| Async | Kotlin Coroutines + Flow |
| Navigation | Jetpack Navigation Compose |

---

## API Reference

### Base URL
```
https://your-linkarena-domain.com/api
```

### Authentication Headers
```bash
Authorization: Bearer <api-token>
Content-Type: application/json
```

---

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/signin` | Sign in with email/password |
| POST | `/api/auth/callback` | NextAuth callback |
| GET | `/api/auth/session` | Get current session |

**Auth Flow**: The app uses NextAuth credentials provider. On successful login, store the session cookie or convert to API token for persistent auth.

### Bookmarks

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/sync?mode=initial` | Initial sync - get all bookmarks & groups |
| GET | `/api/sync?cursor=<bookmarkId>` | Paginated sync (for initial loads >150 items) |
| POST | `/api/bookmarks` | Create bookmark (upsert by URL) |
| PUT | `/api/bookmarks` | Update bookmark by URL |
| DELETE | `/api/bookmarks` | Delete by URL |
| DELETE | `/api/bookmarks/{id}` | Delete by ID |
| PUT | `/api/bookmarks/{id}/category` | Move bookmark to group |

**POST /api/bookmarks Request:**
```json
{
  "url": "https://github.com",
  "title": "GitHub",
  "description": "Where the world builds software",
  "groupId": "cuid-xxx"
}
```

**PUT /api/bookmarks/{id}/category Request:**
```json
{ "categoryId": "cuid-xxx" }
```
Pass `null` to remove from group.

### Groups (Categories)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/groups` | List all groups |
| POST | `/api/groups` | Create group |
| PATCH | `/api/categories/{id}` | Update group (name, color) |
| DELETE | `/api/categories/{id}` | Delete group |

**POST /api/groups Request:**
```json
{ "name": "Work", "color": "#ff0000" }
```

**PATCH /api/categories/{id} Request:**
```json
{ "name": "Personal", "color": "#00ff00", "order": 2 }
```

### Settings

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/settings` | Get user settings |
| PATCH | `/api/settings` | Update settings |

**GET /api/settings Response:**
```json
{ "autoGroupEnabled": true }
```

### Export

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/export` | Export all bookmarks as JSON |

### Realtime (SSE)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/realtime/bookmarks` | SSE stream for live updates |

---

## Data Models

### Bookmark
```kotlin
data class Bookmark(
    val id: String,
    val url: String?,
    val title: String?,
    val description: String?,
    val faviconUrl: String?,
    val previewImageUrl: String?,
    val groupId: String?,
    val group: Group?,
    val createdAt: String,  // ISO 8601
    val updatedAt: String   // ISO 8601
)
```

### Group
```kotlin
data class Group(
    val id: String,
    val name: String,
    val color: String?,    // Hex color like "#ff0000"
    val order: Int,
    val bookmarkCount: Int = 0
)
```

### SyncResponse
```kotlin
data class SyncResponse(
    val bookmarks: List<Bookmark>,
    val groups: List<Group>,
    val partial: Boolean,      // true if more bookmarks remain
    val hasMore: Boolean,
    val nextCursor: String?    // use as cursor param for next page
)
```

### UserSettings
```kotlin
data class UserSettings(
    val autoGroupEnabled: Boolean
)
```

---

## Screens

### Auth Screens
1. **LoginScreen** - Email/password sign in
2. **SignupScreen** - New user registration
3. **ForgotPasswordScreen** - Request password reset email
4. **ResetPasswordScreen** - Set new password with token from email

### Main Screens
5. **HomeScreen** - Bookmark list with search, group filter, timeline toggle
6. **BookmarkDetailScreen** - View/edit bookmark with metadata preview
7. **AddBookmarkScreen** - Add new bookmark (URL entry or paste)
8. **GroupsScreen** - Manage groups (create, edit, delete, reorder)
9. **SettingsScreen** - App settings, account management

---

## Navigation Structure

```
AppNavHost
├── AuthNavGraph
│   ├── LoginScreen
│   ├── SignupScreen
│   ├── ForgotPasswordScreen
│   └── ResetPasswordScreen
│
└── MainNavGraph (after auth)
    ├── HomeScreen (startDestination)
    ├── AddBookmarkScreen
    ├── BookmarkDetailScreen/{bookmarkId}
    ├── GroupsScreen
    └── SettingsScreen
```

---

## Package Structure

```
com.linkarena.app/
├── data/
│   ├── remote/
│   │   ├── api/
│   │   │   └── LinkArenaApi.kt        # Retrofit interface
│   │   ├── dto/
│   │   │   ├── BookmarkDto.kt
│   │   │   ├── GroupDto.kt
│   │   │   ├── SyncResponseDto.kt
│   │   │   └── SettingsDto.kt
│   │   └── auth/
│   │       └── AuthInterceptor.kt
│   ├── local/
│   │   ├── db/
│   │   │   ├── LinkArenaDatabase.kt
│   │   │   ├── BookmarkDao.kt
│   │   │   └── GroupDao.kt
│   │   └── datastore/
│   │       └── PreferencesManager.kt
│   └── repository/
│       ├── BookmarkRepositoryImpl.kt
│       ├── GroupRepositoryImpl.kt
│       └── AuthRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   ├── Bookmark.kt
│   │   ├── Group.kt
│   │   └── UserSettings.kt
│   ├── repository/
│   │   ├── BookmarkRepository.kt
│   │   ├── GroupRepository.kt
│   │   └── AuthRepository.kt
│   └── usecase/
│       ├── bookmarks/
│       │   ├── GetBookmarksUseCase.kt
│       │   ├── CreateBookmarkUseCase.kt
│       │   ├── UpdateBookmarkUseCase.kt
│       │   ├── DeleteBookmarkUseCase.kt
│       │   └── SyncBookmarksUseCase.kt
│       ├── groups/
│       │   ├── GetGroupsUseCase.kt
│       │   ├── CreateGroupUseCase.kt
│       │   ├── UpdateGroupUseCase.kt
│       │   └── DeleteGroupUseCase.kt
│       └── auth/
│           ├── LoginUseCase.kt
│           ├── SignupUseCase.kt
│           └── LogoutUseCase.kt
│
├── di/
│   ├── NetworkModule.kt
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   └── UseCaseModule.kt
│
├── ui/
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   └── Type.kt
│   ├── navigation/
│   │   ├── AppNavHost.kt
│   │   ├── AuthNavGraph.kt
│   │   └── MainNavGraph.kt
│   ├── auth/
│   │   ├── LoginScreen.kt
│   │   ├── LoginViewModel.kt
│   │   ├── SignupScreen.kt
│   │   ├── SignupViewModel.kt
│   │   ├── ForgotPasswordScreen.kt
│   │   └── ResetPasswordScreen.kt
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   ├── bookmark/
│   │   ├── BookmarkDetailScreen.kt
│   │   ├── BookmarkDetailViewModel.kt
│   │   ├── AddBookmarkScreen.kt
│   │   └── AddBookmarkViewModel.kt
│   ├── groups/
│   │   ├── GroupsScreen.kt
│   │   ├── GroupsViewModel.kt
│   │   ├── CreateGroupDialog.kt
│   │   └── EditGroupDialog.kt
│   ├── settings/
│   │   ├── SettingsScreen.kt
│   │   └── SettingsViewModel.kt
│   └── components/
│       ├── BookmarkCard.kt
│       ├── GroupChip.kt
│       ├── LoadingIndicator.kt
│       └── ErrorMessage.kt
│
└── util/
    ├── NetworkResult.kt
    ├── Constants.kt
    └── Extensions.kt
```

---

## Implementation Order

### Phase 1: Project Setup
1. Create `build.gradle.kts` with all dependencies
2. Set up Hilt application class
3. Configure Retrofit with OkHttp
4. Set up Room database
5. Create navigation structure

### Phase 2: Data Layer
1. Create DTOs for API responses
2. Define Retrofit API interface
3. Create Room entities and DAOs
4. Implement repository classes

### Phase 3: Auth (Start Here)
1. **LoginScreen** - Email/password form with validation
2. **SignupScreen** - Registration form
3. **ForgotPasswordScreen** - Password reset request
4. **AuthRepository** - Handle auth state
5. **Session management** - Store token securely (DataStore Encrypted)

### Phase 4: Bookmarks
1. **HomeScreen** - Display bookmark list with pull-to-refresh
2. **AddBookmarkScreen** - Create new bookmark
3. **BookmarkDetailScreen** - View/edit/delete bookmark
4. **Sync logic** - Paginated sync on app launch

### Phase 5: Groups
1. **GroupsScreen** - List all groups with bookmark counts
2. **Create/Edit Group dialogs**
3. **Group filter** on HomeScreen
4. **Move bookmark to group** functionality

### Phase 6: Settings & Polish
1. **SettingsScreen** - User preferences
2. **Account deletion** option
3. **Offline support** - Room caching
4. **Error handling** UI

---

## Key Implementation Notes

### Auth Interceptor
```kotlin
class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {
    override fun intercept(chain: Chain): Response {
        val request = chain.request().newBuilder()
        tokenProvider()?.let { token ->
            request.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(request.build())
    }
}
```

### Sync Flow
```kotlin
suspend fun syncAll(): SyncResult {
    // 1. Initial sync
    val initial = api.sync(mode = "initial")
    db.bookmarkDao().insertAll(initial.bookmarks)
    db.groupDao().insertAll(initial.groups)

    // 2. If more pages, fetch all remaining
    var cursor = initial.nextCursor
    while (initial.hasMore && cursor != null) {
        val page = api.sync(cursor = cursor)
        db.bookmarkDao().insertAll(page.bookmarks)
        cursor = page.nextCursor
    }

    return SyncResult.Success
}
```

### SSE Realtime (Optional for MVP)
```kotlin
// Use OkHttp SSE for real-time updates
val request = Request.Builder()
    .url("${BASE_URL}/api/realtime/bookmarks")
    .addHeader("Authorization", "Bearer $token")
    .build()

client.newCall(request).execute().use { response ->
    val source = response.body!!.source()
    while (true) {
        val line = source.readUtf8Line() ?: continue
        if (line.startsWith("data: ")) {
            val event = Gson().fromJson(line.substring(6), RealtimeEvent::class.java)
            // Handle event - update local DB, emit Flow
        }
    }
}
```

---

## Verification Checklist

- [ ] Login with email/password works
- [ ] Signup creates new account
- [ ] Bookmarks sync on app launch
- [ ] Create bookmark appears in list
- [ ] Edit bookmark updates correctly
- [ ] Delete bookmark removes from list
- [ ] Create/edit/delete groups works
- [ ] Filter bookmarks by group works
- [ ] Settings persist correctly
- [ ] Logout clears session
- [ ] App handles offline gracefully
- [ ] Error messages display correctly

---

## Reference Files

**Backend API Implementation:**
- `app/api/bookmarks/route.ts`
- `app/api/groups/route.ts`
- `app/api/sync/route.ts`
- `app/api/realtime/bookmarks/route.ts`
- `lib/api-auth.ts`
- `prisma/schema.prisma`
