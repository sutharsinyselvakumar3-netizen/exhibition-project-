package com.example.service

import com.example.model.AppSettings
import com.example.model.RobotMovementState
import com.example.model.RobotStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class Esp32Service {

    private val client = OkHttpClient.Builder()
        .connectTimeout(2500, TimeUnit.MILLISECONDS)
        .readTimeout(2500, TimeUnit.MILLISECONDS)
        .writeTimeout(2500, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(false)
        .build()

    sealed class ConnectionTestResult {
        object Connected : ConnectionTestResult()
        data class Failed(val reason: String) : ConnectionTestResult()
        object Timeout : ConnectionTestResult()
    }

    suspend fun testConnection(settings: AppSettings): ConnectionTestResult = withContext(Dispatchers.IO) {
        val url = "${settings.esp32BaseUrl}/api/status"
        val request = Request.Builder().url(url).get().build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    ConnectionTestResult.Connected
                } else {
                    ConnectionTestResult.Failed("HTTP ${response.code}: ${response.message}")
                }
            }
        } catch (e: java.net.SocketTimeoutException) {
            ConnectionTestResult.Timeout
        } catch (e: Exception) {
            ConnectionTestResult.Failed(e.localizedMessage ?: "Connection error")
        }
    }

    suspend fun fetchStatus(settings: AppSettings): Result<RobotStatus> = withContext(Dispatchers.IO) {
        val url = "${settings.esp32BaseUrl}/api/status"
        val request = Request.Builder().url(url).get().build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
                }
                val bodyString = response.body?.string() ?: ""
                val json = JSONObject(bodyString)

                val movementStr = json.optString("robot", "stopped").lowercase()
                val movementState = when (movementStr) {
                    "forward" -> RobotMovementState.FORWARD
                    "reverse" -> RobotMovementState.REVERSE
                    "left" -> RobotMovementState.LEFT
                    "right" -> RobotMovementState.RIGHT
                    "moving" -> RobotMovementState.MOVING
                    else -> RobotMovementState.STOPPED
                }

                val speed = json.optInt("speed", 50).coerceIn(0, 100)
                val soil = if (json.has("soil")) json.getInt("soil").coerceIn(0, 100) else null
                val water = if (json.has("water")) json.getInt("water").coerceIn(0, 100) else null

                val relay1 = when {
                    json.has("relay1") -> json.getBoolean("relay1")
                    json.has("soil_relay") -> json.getBoolean("soil_relay")
                    else -> null
                }

                val relay2 = when {
                    json.has("relay2") -> json.getBoolean("relay2")
                    json.has("pump_relay") -> json.getBoolean("pump_relay")
                    json.has("pump") -> json.getBoolean("pump")
                    else -> null
                }

                val flame = when {
                    json.has("flame") -> json.getBoolean("flame")
                    json.has("fire") -> json.getBoolean("fire")
                    else -> null
                }

                val camServo = json.optInt("camera_servo", 90).coerceIn(0, 180)
                val soilServo = json.optInt("soil_servo", 0).coerceIn(0, 45) // Hard cap at 45

                val status = RobotStatus(
                    connected = true,
                    movementState = movementState,
                    speed = speed,
                    soilMoisture = soil,
                    waterLevel = water,
                    relay1Soil = relay1,
                    relay2Pump = relay2,
                    flameDetected = flame,
                    cameraServoAngle = camServo,
                    soilServoAngle = soilServo,
                    lastUpdateTimeMs = System.currentTimeMillis(),
                    errorMessage = null
                )
                Result.success(status)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMotorCommand(settings: AppSettings, command: String): Result<String> = withContext(Dispatchers.IO) {
        val endpoint = when (command.lowercase()) {
            "forward" -> "/api/motor/forward"
            "reverse" -> "/api/motor/reverse"
            "left" -> "/api/motor/left"
            "right" -> "/api/motor/right"
            else -> "/api/motor/stop"
        }
        sendPost(settings, endpoint)
    }

    suspend fun sendSpeed(settings: AppSettings, speedPercent: Int): Result<String> = withContext(Dispatchers.IO) {
        val clamped = speedPercent.coerceIn(0, 100)
        val endpoint = "/api/motor/speed?val=$clamped"
        sendPost(settings, endpoint)
    }

    suspend fun sendCameraServo(settings: AppSettings, angle: Int): Result<String> = withContext(Dispatchers.IO) {
        val clamped = angle.coerceIn(0, 180)
        val endpoint = "/api/camera_servo?angle=$clamped"
        sendPost(settings, endpoint)
    }

    suspend fun sendSoilServo(settings: AppSettings, angle: Int): Result<String> = withContext(Dispatchers.IO) {
        val clamped = angle.coerceIn(0, 45) // HARD LIMIT TO 45 DEGREES
        val endpoint = "/api/soil_servo?angle=$clamped"
        sendPost(settings, endpoint)
    }

    suspend fun sendRelaySoil(settings: AppSettings, targetState: Boolean): Result<String> = withContext(Dispatchers.IO) {
        val stateParam = if (targetState) "on" else "off"
        val endpoint = "/api/relay/soil?state=$stateParam"
        sendPost(settings, endpoint)
    }

    suspend fun sendRelayPump(settings: AppSettings, targetState: Boolean): Result<String> = withContext(Dispatchers.IO) {
        val stateParam = if (targetState) "on" else "off"
        val endpoint = "/api/relay/pump?state=$stateParam"
        sendPost(settings, endpoint)
    }

    suspend fun emergencyStop(settings: AppSettings): Result<String> = withContext(Dispatchers.IO) {
        // Send stop immediately
        val endpoint = "/api/motor/stop"
        sendPost(settings, endpoint)
    }

    private fun sendPost(settings: AppSettings, endpoint: String): Result<String> {
        val url = "${settings.esp32BaseUrl}$endpoint"
        val emptyBody = "".toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(emptyBody)
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(response.body?.string() ?: "OK")
                } else {
                    Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
