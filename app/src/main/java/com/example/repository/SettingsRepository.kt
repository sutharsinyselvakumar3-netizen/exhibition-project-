package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("agri_robot_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    fun getSettings(): AppSettings = _settingsFlow.value

    fun updateSettings(newSettings: AppSettings) {
        // Enforce safety limits
        val validated = newSettings.copy(
            soilServoMax = newSettings.soilServoMax.coerceIn(1, 45),
            soilServoMin = newSettings.soilServoMin.coerceIn(0, 44),
            cameraServoMax = newSettings.cameraServoMax.coerceIn(1, 180),
            cameraServoMin = newSettings.cameraServoMin.coerceIn(0, 179)
        )

        prefs.edit().apply {
            putString("wifi_ssid", validated.wifiSsid)
            putString("wifi_password", validated.wifiPassword)
            putString("esp32_ip", validated.esp32Ip)
            putInt("esp32_port", validated.esp32Port)
            putString("camera_ip", validated.cameraIp)
            putInt("camera_port", validated.cameraPort)
            putString("camera_path", validated.cameraPath)
            putInt("soil_dry", validated.soilDryThreshold)
            putInt("soil_wet", validated.soilWetThreshold)
            putInt("water_low", validated.waterLowThreshold)
            putInt("water_critical", validated.waterCriticalThreshold)
            putInt("flame_threshold", validated.flameThreshold)
            putInt("cam_servo_min", validated.cameraServoMin)
            putInt("cam_servo_max", validated.cameraServoMax)
            putInt("soil_servo_min", validated.soilServoMin)
            putInt("soil_servo_max", validated.soilServoMax)
            apply()
        }
        _settingsFlow.value = validated
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _settingsFlow.value = AppSettings()
    }

    private fun loadSettings(): AppSettings {
        return AppSettings(
            wifiSsid = prefs.getString("wifi_ssid", "") ?: "",
            wifiPassword = prefs.getString("wifi_password", "") ?: "",
            esp32Ip = prefs.getString("esp32_ip", "192.168.1.50") ?: "192.168.1.50",
            esp32Port = prefs.getInt("esp32_port", 80),
            cameraIp = prefs.getString("camera_ip", "192.168.1.51") ?: "192.168.1.51",
            cameraPort = prefs.getInt("camera_port", 81),
            cameraPath = prefs.getString("camera_path", "/stream") ?: "/stream",
            soilDryThreshold = prefs.getInt("soil_dry", 30),
            soilWetThreshold = prefs.getInt("soil_wet", 70),
            waterLowThreshold = prefs.getInt("water_low", 25),
            waterCriticalThreshold = prefs.getInt("water_critical", 10),
            flameThreshold = prefs.getInt("flame_threshold", 50),
            cameraServoMin = prefs.getInt("cam_servo_min", 0),
            cameraServoMax = prefs.getInt("cam_servo_max", 180),
            soilServoMin = prefs.getInt("soil_servo_min", 0),
            soilServoMax = prefs.getInt("soil_servo_max", 45).coerceIn(0, 45) // Hard limited to 45
        )
    }
}
