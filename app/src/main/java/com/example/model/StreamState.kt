package com.example.model

import android.graphics.Bitmap

sealed interface StreamState {
    object Idle : StreamState
    object Connecting : StreamState
    data class Streaming(val frame: Bitmap, val fps: Int = 0) : StreamState
    data class Error(val title: String, val message: String) : StreamState
}
