package com.sijayyx.todone.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sijayyx.todone.utils.TAG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class AppThemeOptions(val optionName: String) {
    Dark("Dark"),
    Light("Light"),
    Adaptive("Adaptive")
}

class UserSettingsRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        val IS_ALLOW_NOTIFICATION = booleanPreferencesKey("is_allow_notification")
        val APP_THEME_OPTION = stringPreferencesKey("app_theme_option")

        //true = grid, false = line
        val IS_NOTES_VIEW_MODE_GRID = booleanPreferencesKey("is_notes_view_mode_grid")
    }

    val isAllowNotification: Flow<Boolean> =
        dataStore.data.catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[IS_ALLOW_NOTIFICATION] ?: true
        }

    val appThemeOption: Flow<String> =
        dataStore.data.catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[APP_THEME_OPTION] ?: AppThemeOptions.Adaptive.optionName
        }

    val isNotesViewGrid: Flow<Boolean> =
        dataStore.data.catch {
            if (it is IOException) {
                emit(emptyPreferences())
                Log.e(TAG, "Error reading preferences.", it)
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[IS_NOTES_VIEW_MODE_GRID] ?: true
        }

    suspend fun setAllowNotificationPreference(isAllowNotification: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_ALLOW_NOTIFICATION] = isAllowNotification
        }
        Log.e(TAG, "DataStore: set notification preference $isAllowNotification")
    }

    suspend fun setAppThemeOptionPreference(appThemeOptions: String) {
        dataStore.edit { preferences ->
            preferences[APP_THEME_OPTION] = appThemeOptions
        }
        Log.e(TAG, "DataStore: set app theme preference $appThemeOptions")
    }

    suspend fun setNoteViewMode(isGridView: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_NOTES_VIEW_MODE_GRID] = isGridView
        }
    }
}