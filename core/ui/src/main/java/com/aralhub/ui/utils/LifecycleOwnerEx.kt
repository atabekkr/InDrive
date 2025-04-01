package com.aralhub.ui.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

object LifecycleOwnerEx {

    fun <T> LifecycleOwner.observeState(stateFlow: SharedFlow<T>, action: (T) -> Unit) {
        this.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                stateFlow.collect {
                    action(it)
                }
            }
        }
    }
}