package com.senthapps.snapassist.util

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner

object AppVisibility {
  fun isForeground(@Suppress("UNUSED_PARAMETER") ctx: Context): Boolean {
    val state = ProcessLifecycleOwner.get().lifecycle.currentState
    return state.isAtLeast(Lifecycle.State.STARTED)
  }
}