package com.sijayyx.todone

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeeplinkIntentViewModel : ViewModel() {
    private val _deeplinkIntentState = MutableStateFlow(DeeplinkIntentState())
    val deeplinkIntentState = _deeplinkIntentState.asStateFlow()

    fun setDeeplink(deeplinkUri: Uri) {
        _deeplinkIntentState.value = _deeplinkIntentState.value.copy(deeplinkUri)
    }

    fun clearDeeplinkState() {
        _deeplinkIntentState.value = DeeplinkIntentState()
    }
}

data class DeeplinkIntentState(
    val deeplinkUri: Uri = Uri.EMPTY
)