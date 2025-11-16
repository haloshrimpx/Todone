package com.sijayyx.todone

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.sijayyx.todone.ui.composables.PermissionRequestDialog
import com.sijayyx.todone.ui.navigation.AppNavigationHost
import com.sijayyx.todone.ui.navigation.ScreenNavDestination
import com.sijayyx.todone.utils.TAG

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TodoListApp(
    navController: NavHostController,
    permissionViewModel: GlobalViewModel,
    deeplinkViewModel: DeeplinkIntentViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val permissionRequestState = permissionViewModel.permissionRequestState.collectAsState()
    val isShowPermissionRequestDialog by remember(permissionRequestState) {
        derivedStateOf { permissionRequestState.value.isShowRequestPermissionDialog }
    }

    val deepLinkState = deeplinkViewModel.deeplinkIntentState.collectAsState().value

    // 接受Intent的导航参数并进行导航
    LaunchedEffect(deepLinkState) {
        if (deepLinkState.deeplinkUri != Uri.EMPTY) {
            val route = deepLinkState.deeplinkUri.getQueryParameter("screen_route")
                ?: ScreenNavDestination.Todo.route
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                    inclusive = false
                }
                launchSingleTop = true
            }
        }

        deeplinkViewModel.clearDeeplinkState()
    }

    val alarmPermissionState =
        rememberPermissionState(Manifest.permission.SCHEDULE_EXACT_ALARM)


    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            Log.e(
                TAG,
                "permission request: isGranted: $isGranted, alarm:${alarmPermissionState.status.isGranted}"
            )
            permissionViewModel.setAllowNotification(
                isGranted && permissionViewModel.checkCanScheduleAlarm(
                    context
                )
            )
        }
    )
    val scheduleAlarmPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        onResult = {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    )

    AppNavigationHost(
        navController = navController,
        globalViewModel = permissionViewModel
    )

    if (isShowPermissionRequestDialog) {
        if (permissionRequestState.value.requestingPermission == Manifest.permission.SCHEDULE_EXACT_ALARM) {
            permissionViewModel.setRequestCalls(
                onAllow = {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = "package:${context.packageName}".toUri()
                    }
                    scheduleAlarmPermissionLauncher.launch(intent)
                },
                onDeny = {
                    permissionViewModel.setAllowNotification(false)
                }
            )
        }

        PermissionRequestDialog(
            permission = permissionRequestState.value.requestingPermission,
            onAllow = permissionRequestState.value.onRequestAllow,
            onDeny = permissionRequestState.value.onRequestDeny,
            onDismiss = {
                permissionRequestState.value.onCloseDialog?.invoke()
                permissionViewModel.resetPermissionRequestState()
            }
        )
    }
}
