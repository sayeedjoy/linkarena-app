package com.sayeedjoy.linkarena.data.remote.api

import com.sayeedjoy.linkarena.data.remote.dto.AuthResponse
import com.sayeedjoy.linkarena.data.remote.dto.BookmarkResponse
import com.sayeedjoy.linkarena.data.remote.dto.CreateBookmarkRequest
import com.sayeedjoy.linkarena.data.remote.dto.CreateGroupRequest
import com.sayeedjoy.linkarena.data.remote.dto.ForgotPasswordRequest
import com.sayeedjoy.linkarena.data.remote.dto.MessageResponse
import com.sayeedjoy.linkarena.data.remote.dto.ResetPasswordRequest
import com.sayeedjoy.linkarena.data.remote.dto.SessionResponse
import com.sayeedjoy.linkarena.data.remote.dto.SettingsResponse
import com.sayeedjoy.linkarena.data.remote.dto.SignInRequest
import com.sayeedjoy.linkarena.data.remote.dto.SignUpRequest
import com.sayeedjoy.linkarena.data.remote.dto.SyncResponseDto
import com.sayeedjoy.linkarena.data.remote.dto.UpdateBookmarkCategoryRequest
import com.sayeedjoy.linkarena.data.remote.dto.UpdateBookmarkRequest
import com.sayeedjoy.linkarena.data.remote.dto.UpdateGroupRequest
import com.sayeedjoy.linkarena.data.remote.dto.UpdateSettingsRequest
import kotlinx.serialization.json.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface LinkArenaApi {

    // Auth
    @POST("api/mobile/auth/login")
    suspend fun signIn(@Body request: SignInRequest): Response<AuthResponse>

    @POST("api/mobile/auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<AuthResponse>

    @GET("api/auth/session")
    suspend fun getSession(): Response<SessionResponse>

    @POST("api/mobile/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>

    @POST("api/mobile/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<MessageResponse>

    // Sync
    @GET("api/sync")
    suspend fun sync(
        @Query("mode") mode: String? = null,
        @Query("cursor") cursor: String? = null
    ): Response<SyncResponseDto>

    // Bookmarks
    @POST("api/bookmarks")
    suspend fun createBookmark(@Body request: CreateBookmarkRequest): Response<BookmarkResponse>

    @PUT("api/bookmarks")
    suspend fun updateBookmark(@Body request: UpdateBookmarkRequest): Response<BookmarkResponse>

    @DELETE("api/bookmarks")
    suspend fun deleteBookmark(@Query("url") url: String): Response<MessageResponse>

    @DELETE("api/bookmarks/{id}")
    suspend fun deleteBookmarkById(@Path("id") id: String): Response<MessageResponse>

    @POST("api/bookmarks/{id}/refetch")
    suspend fun refetchBookmark(@Path("id") id: String): Response<BookmarkResponse>

    @PUT("api/bookmarks/{id}/category")
    suspend fun updateBookmarkCategory(
        @Path("id") id: String,
        @Body request: UpdateBookmarkCategoryRequest
    ): Response<BookmarkResponse>

    // Groups
    @GET("api/groups")
    suspend fun getGroups(): Response<JsonElement>

    @POST("api/groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<JsonElement>

    @PATCH("api/groups/{id}")
    suspend fun updateGroup(
        @Path("id") id: String,
        @Body request: UpdateGroupRequest
    ): Response<JsonElement>

    @DELETE("api/groups/{id}")
    suspend fun deleteGroup(@Path("id") id: String): Response<MessageResponse>

    // Settings
    @GET("api/settings")
    suspend fun getSettings(): Response<SettingsResponse>

    @PATCH("api/settings")
    suspend fun updateSettings(@Body request: UpdateSettingsRequest): Response<SettingsResponse>

    // Export
    @GET("api/export")
    suspend fun exportBookmarks(): Response<SyncResponseDto>
}
