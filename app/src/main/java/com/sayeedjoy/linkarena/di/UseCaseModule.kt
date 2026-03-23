package com.sayeedjoy.linkarena.di

import com.sayeedjoy.linkarena.domain.repository.AuthRepository
import com.sayeedjoy.linkarena.domain.repository.BookmarkRepository
import com.sayeedjoy.linkarena.domain.repository.GroupRepository
import com.sayeedjoy.linkarena.domain.usecase.auth.LoginUseCase
import com.sayeedjoy.linkarena.domain.usecase.auth.LogoutUseCase
import com.sayeedjoy.linkarena.domain.usecase.auth.SignupUseCase
import com.sayeedjoy.linkarena.domain.usecase.bookmarks.*
import com.sayeedjoy.linkarena.domain.usecase.groups.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @ViewModelScoped
    fun provideLoginUseCase(authRepository: AuthRepository): LoginUseCase {
        return LoginUseCase(authRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideSignupUseCase(authRepository: AuthRepository): SignupUseCase {
        return SignupUseCase(authRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideLogoutUseCase(authRepository: AuthRepository): LogoutUseCase {
        return LogoutUseCase(authRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetBookmarksUseCase(bookmarkRepository: BookmarkRepository): GetBookmarksUseCase {
        return GetBookmarksUseCase(bookmarkRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideCreateBookmarkUseCase(bookmarkRepository: BookmarkRepository): CreateBookmarkUseCase {
        return CreateBookmarkUseCase(bookmarkRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideUpdateBookmarkUseCase(bookmarkRepository: BookmarkRepository): UpdateBookmarkUseCase {
        return UpdateBookmarkUseCase(bookmarkRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideDeleteBookmarkUseCase(bookmarkRepository: BookmarkRepository): DeleteBookmarkUseCase {
        return DeleteBookmarkUseCase(bookmarkRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideSyncBookmarksUseCase(bookmarkRepository: BookmarkRepository): SyncBookmarksUseCase {
        return SyncBookmarksUseCase(bookmarkRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetGroupsUseCase(groupRepository: GroupRepository): GetGroupsUseCase {
        return GetGroupsUseCase(groupRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideCreateGroupUseCase(groupRepository: GroupRepository): CreateGroupUseCase {
        return CreateGroupUseCase(groupRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideUpdateGroupUseCase(groupRepository: GroupRepository): UpdateGroupUseCase {
        return UpdateGroupUseCase(groupRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideDeleteGroupUseCase(groupRepository: GroupRepository): DeleteGroupUseCase {
        return DeleteGroupUseCase(groupRepository)
    }
}
