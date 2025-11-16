package com.sijayyx.todone

import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sijayyx.todone.data.repository.AppThemeOptions
import com.sijayyx.todone.data.repository.UserSettingsRepository
import com.sijayyx.todone.utils.EMPTY_STRING
import com.sijayyx.todone.utils.TAG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GlobalViewModel(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {


    private val _permissionRequestState = MutableStateFlow(PermissionRequestState())
    val permissionRequestState = _permissionRequestState.asStateFlow()

    fun checkCanScheduleAlarm(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun checkPermissionState(permission: String, context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(
        permissionToRequest: String,
        onDeny: (() -> Unit)?,
        onAllow: (() -> Unit)?,
        onClose: (() -> Unit)?
    ) {
        _permissionRequestState.value = _permissionRequestState.value.copy(
            isShowRequestPermissionDialog = true,
            requestingPermission = permissionToRequest,
            onRequestDeny = onDeny,
            onRequestAllow = onAllow,
            onCloseDialog = {
                onClose?.invoke()
            }
        )
    }

    fun requestPermission(permission: String, onCloseDialog: (() -> Unit)?) {
        _permissionRequestState.update {
            it.copy(
                requestingPermission = permission,
                onRequestDeny = null,
                onRequestAllow = null,
                onCloseDialog = {
                    onCloseDialog?.invoke()
                },
                isShowRequestPermissionDialog = true,
            )
        }
        Log.e(
            TAG,
            "permission requested: $permission, isShowDialog = ${permissionRequestState.value.isShowRequestPermissionDialog}"
        )
    }

    fun setRequestCalls(
        onDeny: (() -> Unit)?,
        onAllow: (() -> Unit)?
    ) {
        _permissionRequestState.value = _permissionRequestState.value.copy(
            onRequestDeny = onDeny,
            onRequestAllow = onAllow,
        )
    }

    fun resetPermissionRequestState() {
        _permissionRequestState.value = PermissionRequestState()
    }

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

    /**
     * 应用是否处于深色主题
     *
     * @param context
     * @return
     */
    fun isDarkTheme(context: Context): Boolean {
        val isSystemInDarkTheme =
            when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> true
                Configuration.UI_MODE_NIGHT_NO -> false
                else -> false
            }

        return if (appThemeOption.value == AppThemeOptions.Adaptive.optionName)
            isSystemInDarkTheme
        else
            appThemeOption.value == AppThemeOptions.Dark.optionName
    }

    fun setAllowNotification(isAllow: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setAllowNotificationPreference(isAllow)
        }
    }
}

data class PermissionRequestState(
    var isShowRequestPermissionDialog: Boolean = false,
    var requestingPermission: String = EMPTY_STRING,
    var onRequestDeny: (() -> Unit)? = null,
    var onRequestAllow: (() -> Unit)? = null,
    var onCloseDialog: (() -> Unit)? = null,
)