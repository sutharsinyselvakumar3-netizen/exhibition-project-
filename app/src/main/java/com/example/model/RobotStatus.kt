package com.example.model

enum class RobotMovementState(val label: String) {
    STOPPED("STOPPED"),
    MOVING("MOVING"),
    FORWARD("FORWARD"),
    REVERSE("REVERSE"),
    LEFT("TURNING LEFT"),
    RIGHT("TURNING RIGHT")
}

data class RobotStatus(
    val connected: Boolean = false,
    val movementState: RobotMovementState = RobotMovementState.STOPPED,
    val speed: Int = 50,
    val soilMoisture: Int? = null,
    val waterLevel: Int? = null,
    val relay1Soil: Boolean? = null,
    val relay2Pump: Boolean? = null,
    val flameDetected: Boolean? = null,
    val cameraServoAngle: Int = 90,
    val soilServoAngle: Int = 0,
    val lastUpdateTimeMs: Long = 0L,
    val errorMessage: String? = null
) {
    companion object {
        val OFFLINE = RobotStatus(
            connected = false,
            movementState = RobotMovementState.STOPPED,
            speed = 50,
            soilMoisture = null,
            waterLevel = null,
            relay1Soil = null,
            relay2Pump = null,
            flameDetected = null,
            cameraServoAngle = 90,
            soilServoAngle = 0,
            lastUpdateTimeMs = 0L,
            errorMessage = "ESP32 Disconnected"
        )
    }
}
