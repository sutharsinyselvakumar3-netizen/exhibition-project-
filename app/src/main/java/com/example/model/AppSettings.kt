package com.example.model

data class AppSettings(
    val wifiSsid: String = "",
    val wifiPassword: String = "",
    val esp32Ip: String = "192.168.1.50",
    val esp32Port: Int = 80,
    val cameraIp: String = "192.168.1.51",
    val cameraPort: Int = 81,
    val cameraPath: String = "/stream",
    val soilDryThreshold: Int = 30,
    val soilWetThreshold: Int = 70,
    val waterLowThreshold: Int = 25,
    val waterCriticalThreshold: Int = 10,
    val flameThreshold: Int = 50,
    val cameraServoMin: Int = 0,
    val cameraServoMax: Int = 180,
    val soilServoMin: Int = 0,
    val soilServoMax: Int = 45 // Strictly capped at 45
) {
    val esp32BaseUrl: String
        get() {
            val cleanIp = esp32Ip.trim().removePrefix("http://").removePrefix("https://").removeSuffix("/")
            return "http://$cleanIp:$esp32Port"
        }

    val cameraStreamUrl: String
        get() {
            val cleanIp = cameraIp.trim().removePrefix("http://").removePrefix("https://").removeSuffix("/")
            val cleanPath = if (cameraPath.startsWith("/")) cameraPath else "/$cameraPath"
            return "http://$cleanIp:$cameraPort$cleanPath"
        }
}
