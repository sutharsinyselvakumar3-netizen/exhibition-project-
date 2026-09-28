package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.repository.SettingsRepository
import com.example.service.CameraService
import com.example.service.Esp32Service
import com.example.service.NotificationHelper
import com.example.service.VibrationHelper

class MainViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val settingsRepo = SettingsRepository(context.applicationContext)
            val esp32Service = Esp32Service()
            val cameraService = CameraService()
            val vibrationHelper = VibrationHelper(context.applicationContext)
            val notificationHelper = NotificationHelper(context.applicationContext)
            return MainViewModel(
                settingsRepo,
                esp32Service,
                cameraService,
                vibrationHelper,
                notificationHelper
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
