package com.sijayyx.todone.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sijayyx.todone.data.repository.AppThemeOptions
import com.sijayyx.todone.data.repository.UserSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsScreenViewModel(private val userSettingsRepository: UserSettingsRepository) :
    ViewModel() {

    val isAllowNotification: StateFlow<Boolean> =
        userSettingsRepository.isAllowNotification.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val appThemeOption: StateFlow<String> = userSettingsRepository.appThemeOption.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppThemeOptions.Adaptive.optionName
    )

    fun setAllowNotification(isAllow: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setAllowNotificationPreference(isAllow)
        }
    }

    fun setAppThemeOption(theme: String) {
        viewModelScope.launch {
            userSettingsRepository.setAppThemeOptionPreference(theme)
//            Log.e(TAG, "Adaptive: app theme now set to $theme")
        }
    }
}