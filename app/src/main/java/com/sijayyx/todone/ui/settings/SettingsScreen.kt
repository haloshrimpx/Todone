package com.sijayyx.todone.ui.settings

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.work.WorkManager
import com.sijayyx.todone.AppViewModelProvider
import com.sijayyx.todone.GlobalViewModel
import com.sijayyx.todone.R
import com.sijayyx.todone.background.alarm.AlarmScheduler
import com.sijayyx.todone.background.alarm.AlarmUtils
import com.sijayyx.todone.data.AppDatabase
import com.sijayyx.todone.data.repository.AppThemeOptions
import com.sijayyx.todone.ui.theme.TodoListTheme
import com.sijayyx.todone.utils.TAG
import com.sijayyx.todone.utils.TAG_CHECKLIST_ALARM_WORKER
import com.sijayyx.todone.utils.TAG_TODO_ALARM_WORKER
import com.sijayyx.todone.utils.getTimesLaterTimestampFromNow
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    setGestureEnable: (Boolean) -> Unit,
    navController: NavController,
    permissionViewModel: GlobalViewModel,
    modifier: Modifier = Modifier,
    viewModel: SettingsScreenViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val isAllowNotification = viewModel.isAllowNotification.collectAsState().value
    val appThemeOption = viewModel.appThemeOption.collectAsState().value

    LaunchedEffect(navController) {
        setGestureEnable(false)
    }

    SettingsScreenContent(
        isAllowNotification = isAllowNotification,
        onAllowNotificationChanged = { isOn ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && isOn
                && !permissionViewModel.checkCanScheduleAlarm(context)
            ) {
                permissionViewModel.requestPermission(
                    Manifest.permission.SCHEDULE_EXACT_ALARM,
                    onCloseDialog = null
                )
            }
            viewModel.setAllowNotification(isOn)
        },
        appThemeOption = appThemeOption,
        onDarkModeChanged = {
            val theme = (if (it) AppThemeOptions.Dark else AppThemeOptions.Light).optionName
            viewModel.setAppThemeOption(theme)
        },
        onAdaptiveThemeChanged = {
            //关闭时根据系统主题设置深浅
            if (it) {
                viewModel.setAppThemeOption(AppThemeOptions.Adaptive.optionName)
            } else {
                val theme =
                    (if (isSystemInDarkTheme) AppThemeOptions.Dark else AppThemeOptions.Light).optionName
                viewModel.setAppThemeOption(theme)
            }
        },
        onPrintBackgroundWorks = {
            coroutineScope.launch {
                val checklistWorks = WorkManager.getInstance(context).getWorkInfosByTag(
                    TAG_CHECKLIST_ALARM_WORKER
                ).get()
                var workInfos =
                    "current background works of tag \"$TAG_CHECKLIST_ALARM_WORKER\": \n"

                checklistWorks.forEach {
                    workInfos =
                        workInfos + "   Work: ${it.id},outputData = ${it.outputData}, state = ${it.state} \n"
                }

                val todoWorks = WorkManager.getInstance(context).getWorkInfosByTag(
                    TAG_TODO_ALARM_WORKER
                ).get()

                workInfos = workInfos +
                        "current background works of tag \"$TAG_TODO_ALARM_WORKER\": \n"

                todoWorks.forEach {
                    workInfos =
                        workInfos + "   Work: ${it.id},outputData = ${it.outputData}, state = ${it.state} \n"
                }

                val alarmDao = AppDatabase.getDatabase(context).alarmMessageDao()
                val alarms = alarmDao.getAllAlarmMessageData()

                workInfos = workInfos + "current alarms in database:\n"
                alarms.forEach {
                    workInfos =
                        workInfos + "   Alarm ${it.hashcode}, message = ${it.message}, alarmTime = ${it.alarmTimestamp}\n"
                }
                Log.i(TAG, workInfos)
            }

        },
        onNavigateUp = { navController.navigateUp() },
        onFireNotification = {
            coroutineScope.launch {
                val alarmScheduler = AlarmScheduler(context)
                alarmScheduler.scheduleOrUpdateAlarm(
                    AlarmUtils.createAlarmMessage(
                        1, AlarmUtils.AlarmType.TodoItem,
                        alarmTimestamp = getTimesLaterTimestampFromNow(seconds = 1),
                        message = "Test Message: Todo"
                    )
                )
            }

        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    isAllowNotification: Boolean,
    appThemeOption: String,

    onAllowNotificationChanged: (Boolean) -> Unit,
    onDarkModeChanged: (Boolean) -> Unit,
    onAdaptiveThemeChanged: (Boolean) -> Unit,
    onPrintBackgroundWorks: () -> Unit,
    onFireNotification: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLanguagePickerExpanded by rememberSaveable { mutableStateOf(false) }
    val languageOptions = mapOf(
        R.string.language_chinese to "zh-CN",
        R.string.language_english to "en-US"
    ).mapKeys { stringResource(it.key) }

    var selectedOption by rememberSaveable { mutableStateOf("Chinese") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_settings)) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateUp
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .verticalScroll(
                        rememberScrollState()
                    )
            ) {
                Spacer(Modifier.padding(vertical = 8.dp))
                SettingsElement(
                    title = stringResource(R.string.settings_subtitle_notifications),
                    options = {
                        SettingOption(
                            description = stringResource(R.string.settings_options_allow_notification),
                            action = {
                                Switch(
                                    checked = isAllowNotification,
                                    onCheckedChange = { onAllowNotificationChanged(it) },
                                    modifier = Modifier.scale(0.85f)
                                )
                            },
                            subDescription = stringResource(R.string.settings_notification_switch_description)
                        )
                    }
                )
                HorizontalDivider(Modifier.padding(vertical = 24.dp), thickness = 2.dp)
                SettingsElement(
                    title = stringResource(R.string.settings_options_language),
                    options = {
                        ExposedDropdownMenuBox(
                            expanded = isLanguagePickerExpanded,
                            onExpandedChange = {
                                isLanguagePickerExpanded = it
                            },
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.menuAnchor(
                                    MenuAnchorType.PrimaryEditable,
                                    true
                                ),
                                value = stringResource(R.string.app_language),
                                onValueChange = { },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLanguagePickerExpanded) },
                                readOnly = true // 设置为只读，只能通过下拉菜单选择
                            )
                            ExposedDropdownMenu(
                                expanded = isLanguagePickerExpanded,
                                onDismissRequest = { isLanguagePickerExpanded = false },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.background(Color.Transparent)
                            ) {
                                languageOptions.keys.forEach { option ->
                                    key(option) {
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                isLanguagePickerExpanded = false
                                                AppCompatDelegate.setApplicationLocales(
                                                    LocaleListCompat.forLanguageTags(
                                                        languageOptions[option]
                                                    )
                                                )
                                                Log.e(TAG, "App language has set to $option")
                                            }
                                        )
                                    }

                                }
                            }
                        }
                        Text(
                            text = stringResource(R.string.settings_language_dropdown_description),
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                HorizontalDivider(Modifier.padding(vertical = 24.dp), thickness = 2.dp)
                SettingsElement(
                    title = stringResource(R.string.settings_options_theme),
                    options = {
                        SettingOption(
                            description = stringResource(R.string.settings_options_follow_system_theme),
                            action = {
                                Switch(
                                    checked = appThemeOption == AppThemeOptions.Adaptive.optionName,
                                    onCheckedChange = onAdaptiveThemeChanged,
                                    modifier = Modifier.scale(0.85f)
                                )
                            },
                        )
                        SettingOption(
                            description = stringResource(R.string.settings_options_use_dark_mode),
                            subDescription = stringResource(R.string.settings_use_dark_mode_description),
                            action = {
                                Switch(
                                    enabled = appThemeOption != AppThemeOptions.Adaptive.optionName,
                                    checked = appThemeOption == AppThemeOptions.Dark.optionName,
                                    onCheckedChange = onDarkModeChanged,
                                    modifier = Modifier.scale(0.85f)
                                )
                            },
                        )
                    }
                )
                HorizontalDivider(Modifier.padding(vertical = 24.dp), thickness = 2.dp)
                SettingsElement(
                    title = stringResource(R.string.settings_subtitle_debug),
                    options = {
                        SettingOption(
                            description = stringResource(R.string.settings_options_print_background_works),
                            action = {
                                TextButton(
                                    onClick = onPrintBackgroundWorks
                                ) {
                                    Text(
                                        stringResource(R.string.settings_action_button_print),
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        )
                        SettingOption(
                            description = stringResource(R.string.settings_options_fire_a_notification),
                            action = {
                                TextButton(
                                    onClick = onFireNotification
                                ) {
                                    Text(
                                        stringResource(R.string.settings_action_button_go),
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        )
                    }
                )
            }
        }

    }
}

@Composable
fun SettingsElement(
    title: String,
    options: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Column {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            options()
        }
    }

}

@Composable
fun SettingOption(
    description: String,
    modifier: Modifier = Modifier,
    action: (@Composable RowScope.() -> Unit)? = null,
    subDescription: String? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.widthIn(20.dp, 240.dp)) {
            Text(text = description, fontSize = 22.sp, lineHeight = 22.sp)
            if (subDescription != null && subDescription.isNotEmpty())
                Text(
                    text = subDescription,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
        }
        Spacer(Modifier.weight(1f))
        if (action != null)
            action()
    }
}

@Preview(
    showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, locale = "zh-rCN"
)
@Composable
fun SettingsScreenContentPreview() {
    TodoListTheme {
        SettingsScreenContent(
            onNavigateUp = {},
            isAllowNotification = false,
            onAllowNotificationChanged = {},
            appThemeOption = "",
            onDarkModeChanged = {},
            onAdaptiveThemeChanged = {},
            onPrintBackgroundWorks = {},
            onFireNotification = {}
        )
    }
}