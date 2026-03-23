package com.sayeedjoy.linkarena.data.remote.api

import com.sayeedjoy.linkarena.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface LinkArenaApi {

    // Auth
    @POST("api/mobile/auth/login")
    suspend fun signIn(@Body request: SignInRequest): Response<AuthResponse>

    @POST("api/auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<AuthResponse>

    @GET("api/auth/session")
    suspend fun getSession(): Response<SessionResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>

    @POST("api/auth/reset-password")
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

    @PUT("api/bookmarks/{id}/category")
    suspend fun updateBookmarkCategory(
        @Path("id") id: String,
        @Body request: UpdateBookmarkCategoryRequest
    ): Response<BookmarkResponse>

    // Groups
    @GET("api/groups")
    suspend fun getGroups(): Response<GroupListResponse>

    @POST("api/groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<GroupResponse>

    @PATCH("api/categories/{id}")
    suspend fun updateGroup(
        @Path("id") id: String,
        @Body request: UpdateGroupRequest
    ): Response<GroupResponse>

    @DELETE("api/categories/{id}")
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
