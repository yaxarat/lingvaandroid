package dev.atajan.lingva_android.mvi

import android.util.Log

fun Any.stateLogger(state: String, intention: String) {
    Log.d(this.javaClass.simpleName, "state: $state \nintention: $intention")
}