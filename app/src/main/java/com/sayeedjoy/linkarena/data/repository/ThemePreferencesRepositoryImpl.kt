package com.sayeedjoy.linkarena.data.repository

import com.sayeedjoy.linkarena.data.local.datastore.PreferencesManager
import com.sayeedjoy.linkarena.domain.model.ThemeMode
import com.sayeedjoy.linkarena.domain.repository.ThemePreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ThemePreferencesRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ThemePreferencesRepository {

    override val themeMode: Flow<ThemeMode> = preferencesManager.themeMode

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        preferencesManager.saveThemeMode(themeMode)
    }
}
