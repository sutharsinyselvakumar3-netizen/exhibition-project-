package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.ui.theme.AgriBlue
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.NavyMuted
import com.example.ui.theme.RoyalWhite
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGray
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusRed

@Composable
fun SettingsScreen(
    currentSettings: AppSettings,
    esp32TestStatus: String?,
    cameraTestStatus: String?,
    onSaveSettings: (AppSettings) -> Unit,
    onResetDefaults: () -> Unit,
    onTestEsp32: () -> Unit,
    onTestCamera: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var wifiSsid by remember { mutableStateOf(currentSettings.wifiSsid) }
    var wifiPassword by remember { mutableStateOf(currentSettings.wifiPassword) }

    var esp32Ip by remember { mutableStateOf(currentSettings.esp32Ip) }
    var esp32Port by remember { mutableStateOf(currentSettings.esp32Port.toString()) }

    var cameraIp by remember { mutableStateOf(currentSettings.cameraIp) }
    var cameraPort by remember { mutableStateOf(currentSettings.cameraPort.toString()) }
    var cameraPath by remember { mutableStateOf(currentSettings.cameraPath) }

    var soilDry by remember { mutableStateOf(currentSettings.soilDryThreshold.toString()) }
    var soilWet by remember { mutableStateOf(currentSettings.soilWetThreshold.toString()) }

    var waterLow by remember { mutableStateOf(currentSettings.waterLowThreshold.toString()) }
    var waterCrit by remember { mutableStateOf(currentSettings.waterCriticalThreshold.toString()) }

    var flameThresh by remember { mutableStateOf(currentSettings.flameThreshold.toString()) }

    var camServoMin by remember { mutableStateOf(currentSettings.cameraServoMin.toString()) }
    var camServoMax by remember { mutableStateOf(currentSettings.cameraServoMax.toString()) }

    var soilServoMin by remember { mutableStateOf(currentSettings.soilServoMin.toString()) }
    var soilServoMax by remember { mutableStateOf(currentSettings.soilServoMax.coerceIn(0, 45).toString()) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = RoyalWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DarkNavy
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "SETTINGS & HARDWARE CONFIG",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = DarkNavy
                    )
                    Text(
                        text = "Configure IP addresses, thresholds & calibration",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = NavyMuted
                    )
                }
            }

            HorizontalDivider(color = BorderLight)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Wi-Fi Settings Section
                SettingsCard(title = "WI-FI / HOTSPOT SETTINGS", icon = Icons.Default.Wifi) {
                    Text(
                        text = "Connect phone to the same local Wi-Fi or router network used by ESP32 and ESP32-CAM.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = NavyMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = wifiSsid,
                        onValueChange = { wifiSsid = it },
                        label = { Text("Wi-Fi / Hotspot SSID") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_wifi_ssid"),
                        colors = agriTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = wifiPassword,
                        onValueChange = { wifiPassword = it },
                        label = { Text("Wi-Fi Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_wifi_password"),
                        colors = agriTextFieldColors()
                    )
                }

                // 2. ESP32 Connection Section
                SettingsCard(title = "ESP32 CONNECTION (MAIN CONTROLLER)", icon = Icons.Default.Memory) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = esp32Ip,
                            onValueChange = { esp32Ip = it },
                            label = { Text("ESP32 IP Address") },
                            placeholder = { Text("192.168.1.50") },
                            modifier = Modifier
                                .weight(2f)
                                .testTag("settings_esp32_ip"),
                            colors = agriTextFieldColors()
                        )

                        OutlinedTextField(
                            value = esp32Port,
                            onValueChange = { esp32Port = it },
                            label = { Text("Port") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_esp32_port"),
                            colors = agriTextFieldColors()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onTestEsp32,
                            colors = ButtonDefaults.buttonColors(containerColor = AgriBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("test_esp32_button")
                        ) {
                            Text("TEST ESP32 CONNECTION", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (esp32TestStatus != null) {
                            val color = when {
                                esp32TestStatus.contains("Connected", ignoreCase = true) -> StatusGreen
                                esp32TestStatus.contains("Testing", ignoreCase = true) -> StatusBlue
                                else -> StatusRed
                            }
                            Text(
                                text = esp32TestStatus,
                                color = color,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                // 3. ESP32-CAM Connection Section
                SettingsCard(title = "ESP32-CAM CONNECTION (LIVE STREAM)", icon = Icons.Default.Videocam) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = cameraIp,
                            onValueChange = { cameraIp = it },
                            label = { Text("Camera IP") },
                            placeholder = { Text("192.168.1.51") },
                            modifier = Modifier
                                .weight(2f)
                                .testTag("settings_camera_ip"),
                            colors = agriTextFieldColors()
                        )

                        OutlinedTextField(
                            value = cameraPort,
                            onValueChange = { cameraPort = it },
                            label = { Text("Port") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_camera_port"),
                            colors = agriTextFieldColors()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Full Stream Endpoint: http://${cameraIp.trim()}:${cameraPort.trim()}$cameraPath",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = AgriBlue
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onTestCamera,
                            colors = ButtonDefaults.buttonColors(containerColor = AgriBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("test_camera_button")
                        ) {
                            Text("TEST CAMERA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (cameraTestStatus != null) {
                            val color = when {
                                cameraTestStatus.contains("ONLINE", ignoreCase = true) -> StatusGreen
                                cameraTestStatus.contains("Testing", ignoreCase = true) -> StatusBlue
                                else -> StatusRed
                            }
                            Text(
                                text = cameraTestStatus,
                                color = color,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                // 4. Sensor Thresholds Section
                SettingsCard(title = "SENSOR THRESHOLDS", icon = Icons.Default.Sensors) {
                    Text(
                        text = "Soil Moisture Thresholds (%)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = soilDry,
                            onValueChange = { soilDry = it },
                            label = { Text("Dry Threshold %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_soil_dry"),
                            colors = agriTextFieldColors()
                        )
                        OutlinedTextField(
                            value = soilWet,
                            onValueChange = { soilWet = it },
                            label = { Text("Wet Threshold %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_soil_wet"),
                            colors = agriTextFieldColors()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Water Level Alert Thresholds (%)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = waterLow,
                            onValueChange = { waterLow = it },
                            label = { Text("Low Alert %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_water_low"),
                            colors = agriTextFieldColors()
                        )
                        OutlinedTextField(
                            value = waterCrit,
                            onValueChange = { waterCrit = it },
                            label = { Text("Critical %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_water_crit"),
                            colors = agriTextFieldColors()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Flame Sensor Trigger Level",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = flameThresh,
                        onValueChange = { flameThresh = it },
                        label = { Text("Flame Sensitivity / Threshold") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_flame_thresh"),
                        colors = agriTextFieldColors()
                    )
                }

                // 5. Servo Calibration Section
                SettingsCard(title = "SERVO LIMITS & CALIBRATION", icon = Icons.Default.Tune) {
                    Text(
                        text = "Servo 1 (Camera Pan: 0–180°)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = camServoMin,
                            onValueChange = { camServoMin = it },
                            label = { Text("Min (0°)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = agriTextFieldColors()
                        )
                        OutlinedTextField(
                            value = camServoMax,
                            onValueChange = { camServoMax = it },
                            label = { Text("Max (180°)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = agriTextFieldColors()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Servo 2 (Soil Sensor Arm: STRICT 0–45° LIMIT)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = StatusOrange
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Safety limitation: Soil arm physical travel must NEVER exceed 45 degrees.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = NavyMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = soilServoMin,
                            onValueChange = { soilServoMin = it },
                            label = { Text("Min (0°)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = agriTextFieldColors()
                        )
                        OutlinedTextField(
                            value = soilServoMax,
                            onValueChange = {
                                val num = it.toIntOrNull() ?: 0
                                soilServoMax = num.coerceIn(0, 45).toString()
                            },
                            label = { Text("Max (Cap: 45°)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_soil_servo_max"),
                            colors = agriTextFieldColors()
                        )
                    }
                }

                // 6. Hardware Reference Guide
                SettingsCard(title = "HARDWARE PINOUT & SPECIFICATION", icon = Icons.Default.Build) {
                    Text(
                        text = "Final Agricultural Robot Hardware Mapping:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val specList = listOf(
                        "ESP32 DevKit V1" to "Main controller & HTTP REST Server",
                        "ESP32-CAM" to "Dedicated Live Video Stream (Port 81)",
                        "L298N Driver" to "2 DC Geared Motors (IN1-IN4, ENA, ENB)",
                        "Servo 1" to "Camera Turning Servo (Range: 0–180°)",
                        "Servo 2" to "Soil Moisture Sensor Arm (Range: 0–45° only)",
                        "Relay 1" to "Soil Sensor / Aux Load power control",
                        "Relay 2" to "DC Submersible Water Pump power",
                        "Sensors" to "Soil Moisture, Water Level, Optical Flame IR"
                    )

                    specList.forEach { (item, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "• $item:",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = DarkNavy
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = NavyMuted
                            )
                        }
                    }
                }

                // Action buttons: Save & Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onResetDefaults,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_reset_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RESET DEFAULTS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val newSet = currentSettings.copy(
                                wifiSsid = wifiSsid.trim(),
                                wifiPassword = wifiPassword,
                                esp32Ip = esp32Ip.trim(),
                                esp32Port = esp32Port.toIntOrNull() ?: 80,
                                cameraIp = cameraIp.trim(),
                                cameraPort = cameraPort.toIntOrNull() ?: 81,
                                cameraPath = cameraPath.trim(),
                                soilDryThreshold = soilDry.toIntOrNull() ?: 30,
                                soilWetThreshold = soilWet.toIntOrNull() ?: 70,
                                waterLowThreshold = waterLow.toIntOrNull() ?: 25,
                                waterCriticalThreshold = waterCrit.toIntOrNull() ?: 10,
                                flameThreshold = flameThresh.toIntOrNull() ?: 50,
                                cameraServoMin = camServoMin.toIntOrNull() ?: 0,
                                cameraServoMax = (camServoMax.toIntOrNull() ?: 180).coerceIn(0, 180),
                                soilServoMin = soilServoMin.toIntOrNull() ?: 0,
                                soilServoMax = (soilServoMax.toIntOrNull() ?: 45).coerceIn(0, 45) // HARD LIMIT
                            )
                            onSaveSettings(newSet)
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_save_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVE SETTINGS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = AgriBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = DarkNavy
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun agriTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AgriBlue,
    unfocusedBorderColor = Color(0xFFCBD5E1),
    focusedLabelColor = AgriBlue,
    unfocusedLabelColor = NavyMuted,
    cursorColor = AgriBlue
)
