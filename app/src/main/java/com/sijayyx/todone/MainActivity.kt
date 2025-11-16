package com.sijayyx.todone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.core.util.Consumer
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.sijayyx.todone.data.repository.AppThemeOptions
import com.sijayyx.todone.ui.theme.TodoListTheme
import com.sijayyx.todone.utils.TAG

class MainActivity : AppCompatActivity() {

    val deeplinkIntentViewModel: DeeplinkIntentViewModel by viewModels()

    //冷启动
    override fun onStart() {
        super.onStart()
        intent.data?.let {
            if (it.scheme == "todoapp" && it.host == "notification") {
                val screenRoute = it.getQueryParameter("screen_route")
                deeplinkIntentViewModel.setDeeplink("todoapp://notification/screen_route=$screenRoute".toUri())
                Log.e(TAG, "MainActivity: Intent invoked with deeplink $screenRoute")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val globalViewModel = viewModel<GlobalViewModel>(factory = AppViewModelProvider.factory)
            val navController = rememberNavController()

            TodoListTheme(
                darkTheme = when (globalViewModel.appThemeOption.collectAsState().value) {
                    AppThemeOptions.Dark.optionName -> true
                    AppThemeOptions.Light.optionName -> false
                    else -> isSystemInDarkTheme()
                },
                dynamicColor = (globalViewModel.appThemeOption.collectAsState().value == AppThemeOptions.Adaptive.optionName)
            ) {

                //热启动
                DisposableEffect(Unit) {
                    val listener = Consumer<Intent> { intent ->
                        intent.data?.let {
                            if (it.scheme == "todoapp" && it.host == "notification") {
                                deeplinkIntentViewModel.setDeeplink(it)
                                Log.e(
                                    TAG,
                                    "MainActivity: Intent invoked with deeplink $it"
                                )
                            }
                        }
                    }
                    addOnNewIntentListener(listener)
                    onDispose { removeOnNewIntentListener(listener) }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TodoListApp(
                        navController = navController,
                        permissionViewModel = globalViewModel,
                        deeplinkViewModel = deeplinkIntentViewModel
                    )
                }
            }
        }
    }
}
