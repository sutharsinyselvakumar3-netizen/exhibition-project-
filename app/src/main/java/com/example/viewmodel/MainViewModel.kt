package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppSettings
import com.example.model.RobotMovementState
import com.example.model.RobotStatus
import com.example.model.StreamState
import com.example.repository.SettingsRepository
import com.example.service.CameraService
import com.example.service.Esp32Service
import com.example.service.NotificationHelper
import com.example.service.VibrationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(
    private val settingsRepository: SettingsRepository,
    private val esp32Service: Esp32Service,
    private val cameraService: CameraService,
    private val vibrationHelper: VibrationHelper,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    val appSettings: StateFlow<AppSettings> = settingsRepository.settingsFlow
    val cameraState: StateFlow<StreamState> = cameraService.streamState

    private val _robotStatus = MutableStateFlow(RobotStatus.OFFLINE)
    val robotStatus: StateFlow<RobotStatus> = _robotStatus.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _esp32TestStatus = MutableStateFlow<String?>(null)
    val esp32TestStatus: StateFlow<String?> = _esp32TestStatus.asStateFlow()

    private val _cameraTestStatus = MutableStateFlow<String?>(null)
    val cameraTestStatus: StateFlow<String?> = _cameraTestStatus.asStateFlow()

    // Tracking for state transitions to avoid repetitive alerts
    private var previousWaterLevel: Int? = null
    private var previousFlameDetected: Boolean? = null

    private var pollingJob: Job? = null

    init {
        startPolling()
        startCameraStream()
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                val settings = appSettings.value
                val result = esp32Service.fetchStatus(settings)
                result.fold(
                    onSuccess = { newStatus ->
                        handleStatusTransitions(newStatus, settings)
                        _robotStatus.value = newStatus
                    },
                    onFailure = { error ->
                        _robotStatus.value = RobotStatus.OFFLINE.copy(
                            errorMessage = error.localizedMessage ?: "ESP32 Disconnected"
                        )
                    }
                )
                delay(1500)
            }
        }
    }

    private fun handleStatusTransitions(newStatus: RobotStatus, settings: AppSettings) {
        val currWater = newStatus.waterLevel
        if (currWater != null) {
            val wasLowOrCrit = previousWaterLevel?.let { it <= settings.waterLowThreshold } ?: false
            val isNowLow = currWater <= settings.waterLowThreshold
            if (!wasLowOrCrit && isNowLow) {
                // Trigger phone vibration once on entering LOW state
                vibrationHelper.vibrateLowWater()
            }
            previousWaterLevel = currWater
        }

        val currFlame = newStatus.flameDetected
        if (currFlame != null) {
            val wasFlame = previousFlameDetected == true
            if (!wasFlame && currFlame) {
                // Fire transition to active
                vibrationHelper.vibrateFireDetected()
                notificationHelper.postFireAlert()
            } else if (wasFlame && !currFlame) {
                notificationHelper.dismissFireAlert()
            }
            previousFlameDetected = currFlame
        }
    }

    fun startCameraStream() {
        cameraService.startStream(viewModelScope, appSettings.value)
    }

    fun reconnectCamera() {
        cameraService.restartStream(viewModelScope, appSettings.value)
    }

    fun sendMotorCommand(command: String) {
        viewModelScope.launch {
            if (!_robotStatus.value.connected) {
                _toastMessage.value = "Cannot move: ESP32 is offline"
                return@launch
            }
            val result = esp32Service.sendMotorCommand(appSettings.value, command)
            result.onSuccess {
                val state = when (command.lowercase()) {
                    "forward" -> RobotMovementState.FORWARD
                    "reverse" -> RobotMovementState.REVERSE
                    "left" -> RobotMovementState.LEFT
                    "right" -> RobotMovementState.RIGHT
                    else -> RobotMovementState.STOPPED
                }
                _robotStatus.value = _robotStatus.value.copy(movementState = state)
            }.onFailure { err ->
                _toastMessage.value = "Motor command failed: ${err.localizedMessage}"
            }
        }
    }

    fun sendSpeed(speed: Int) {
        val clamped = speed.coerceIn(0, 100)
        _robotStatus.value = _robotStatus.value.copy(speed = clamped)
        viewModelScope.launch {
            if (!_robotStatus.value.connected) return@launch
            esp32Service.sendSpeed(appSettings.value, clamped)
        }
    }

    fun sendCameraServo(angle: Int) {
        val clamped = angle.coerceIn(0, 180)
        _robotStatus.value = _robotStatus.value.copy(cameraServoAngle = clamped)
        viewModelScope.launch {
            if (!_robotStatus.value.connected) return@launch
            esp32Service.sendCameraServo(appSettings.value, clamped)
        }
    }

    fun sendSoilServo(angle: Int) {
        // STRICT SAFETY LIMIT: Servo 2 soil sensor arm must NEVER exceed 45 degrees
        val clamped = angle.coerceIn(0, 45)
        _robotStatus.value = _robotStatus.value.copy(soilServoAngle = clamped)
        viewModelScope.launch {
            if (!_robotStatus.value.connected) return@launch
            esp32Service.sendSoilServo(appSettings.value, clamped)
        }
    }

    fun toggleRelaySoil() {
        val target = !(_robotStatus.value.relay1Soil ?: false)
        viewModelScope.launch {
            if (!_robotStatus.value.connected) {
                _toastMessage.value = "ESP32 is offline"
                return@launch
            }
            val res = esp32Service.sendRelaySoil(appSettings.value, target)
            res.onSuccess {
                _robotStatus.value = _robotStatus.value.copy(relay1Soil = target)
            }.onFailure {
                _toastMessage.value = "Soil Relay command failed"
            }
        }
    }

    fun toggleRelayPump() {
        val target = !(_robotStatus.value.relay2Pump ?: false)
        viewModelScope.launch {
            if (!_robotStatus.value.connected) {
                _toastMessage.value = "ESP32 is offline"
                return@launch
            }
            val res = esp32Service.sendRelayPump(appSettings.value, target)
            res.onSuccess {
                _robotStatus.value = _robotStatus.value.copy(relay2Pump = target)
            }.onFailure {
                _toastMessage.value = "Water Pump command failed"
            }
        }
    }

    fun emergencyStop() {
        _robotStatus.value = _robotStatus.value.copy(movementState = RobotMovementState.STOPPED)
        viewModelScope.launch {
            esp32Service.emergencyStop(appSettings.value)
            _toastMessage.value = "EMERGENCY STOP ACTIVATED"
        }
    }

    fun testEsp32Connection() {
        viewModelScope.launch {
            _esp32TestStatus.value = "Testing..."
            val result = esp32Service.testConnection(appSettings.value)
            _esp32TestStatus.value = when (result) {
                is Esp32Service.ConnectionTestResult.Connected -> "Connected"
                is Esp32Service.ConnectionTestResult.Timeout -> "Timeout"
                is Esp32Service.ConnectionTestResult.Failed -> "Failed: ${result.reason}"
            }
        }
    }

    fun testCameraConnection() {
        viewModelScope.launch {
            _cameraTestStatus.value = "Testing..."
            val ok = cameraService.testCamera(appSettings.value)
            _cameraTestStatus.value = if (ok) "CAMERA ONLINE" else "CAMERA OFFLINE"
        }
    }

    fun saveSettings(newSettings: AppSettings) {
        settingsRepository.updateSettings(newSettings)
        _toastMessage.value = "Settings saved"
        // Restart services with new endpoints
        reconnectCamera()
    }

    fun resetSettingsToDefaults() {
        settingsRepository.resetToDefaults()
        _toastMessage.value = "Settings reset to defaults"
        reconnectCamera()
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        cameraService.stopStream()
    }
}
