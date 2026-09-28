package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RobotMovementState
import com.example.model.RobotStatus
import com.example.model.StreamState
import com.example.ui.components.StatusBadgeStyle
import com.example.ui.components.StatusCard
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.NavyMuted

@Composable
fun HomeScreen(
    status: RobotStatus,
    cameraState: StreamState,
    soilDryThreshold: Int,
    soilWetThreshold: Int,
    waterLowThreshold: Int,
    waterCriticalThreshold: Int,
    modifier: Modifier = Modifier
) {
    val isOnline = status.connected
    val isCameraOnline = cameraState is StreamState.Streaming

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Welcome banner item
        item(span = { GridItemSpan(2) }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF1F5F9)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SYSTEM TELEMETRY OVERVIEW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = NavyMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isOnline) "All telemetric systems linked to ESP32 DevKit V1" else "ESP32 disconnected — showing verified hardware status",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = DarkNavy
                    )
                }
            }
        }

        // 1. ESP32
        item {
            StatusCard(
                title = "ESP32",
                value = if (isOnline) "ONLINE" else "OFFLINE",
                icon = Icons.Default.Memory,
                statusStyle = if (isOnline) StatusBadgeStyle.GREEN else StatusBadgeStyle.GRAY,
                subValue = if (isOnline) "Port 80 • Linked" else "No Ping",
                testTag = "home_status_esp32"
            )
        }

        // 2. CAMERA
        item {
            StatusCard(
                title = "CAMERA",
                value = if (isCameraOnline) "ONLINE" else "OFFLINE",
                icon = Icons.Default.Videocam,
                statusStyle = if (isCameraOnline) StatusBadgeStyle.GREEN else StatusBadgeStyle.GRAY,
                subValue = if (isCameraOnline) "ESP32-CAM Live" else "Stream Unreachable",
                testTag = "home_status_camera"
            )
        }

        // 3. ROBOT
        item {
            val isMoving = status.movementState != RobotMovementState.STOPPED
            StatusCard(
                title = "ROBOT",
                value = if (!isOnline) "UNKNOWN" else if (isMoving) "MOVING" else "STOPPED",
                icon = Icons.Default.ElectricBolt,
                statusStyle = when {
                    !isOnline -> StatusBadgeStyle.GRAY
                    isMoving -> StatusBadgeStyle.BLUE
                    else -> StatusBadgeStyle.GREEN
                },
                subValue = if (isOnline) status.movementState.label else "Offline",
                testTag = "home_status_robot"
            )
        }

        // 4. SPEED
        item {
            StatusCard(
                title = "SPEED",
                value = if (isOnline) "${status.speed} %" else "UNKNOWN",
                icon = Icons.Default.Speed,
                statusStyle = if (isOnline) StatusBadgeStyle.BLUE else StatusBadgeStyle.GRAY,
                subValue = if (isOnline) "PWM Duty Cycle" else "Offline",
                testTag = "home_status_speed"
            )
        }

        // 5. SOIL
        item {
            val soilVal = status.soilMoisture
            val (label, style) = when {
                !isOnline || soilVal == null -> "UNKNOWN" to StatusBadgeStyle.GRAY
                soilVal < soilDryThreshold -> "$soilVal %" to StatusBadgeStyle.ORANGE
                soilVal > soilWetThreshold -> "$soilVal %" to StatusBadgeStyle.BLUE
                else -> "$soilVal %" to StatusBadgeStyle.GREEN
            }
            StatusCard(
                title = "SOIL",
                value = label,
                icon = Icons.Default.Yard,
                statusStyle = style,
                subValue = when {
                    !isOnline || soilVal == null -> "Offline"
                    soilVal < soilDryThreshold -> "Dry Condition"
                    soilVal > soilWetThreshold -> "Wet Condition"
                    else -> "Optimal Moisture"
                },
                testTag = "home_status_soil"
            )
        }

        // 6. WATER
        item {
            val waterVal = status.waterLevel
            val (label, style) = when {
                !isOnline || waterVal == null -> "UNKNOWN" to StatusBadgeStyle.GRAY
                waterVal <= waterCriticalThreshold -> "$waterVal %" to StatusBadgeStyle.RED
                waterVal <= waterLowThreshold -> "$waterVal %" to StatusBadgeStyle.ORANGE
                else -> "$waterVal %" to StatusBadgeStyle.BLUE
            }
            StatusCard(
                title = "WATER",
                value = label,
                icon = Icons.Default.WaterDrop,
                statusStyle = style,
                subValue = when {
                    !isOnline || waterVal == null -> "Offline"
                    waterVal <= waterCriticalThreshold -> "Critical"
                    waterVal <= waterLowThreshold -> "Low Warning"
                    else -> "Adequate Level"
                },
                testTag = "home_status_water"
            )
        }

        // 7. SOIL RELAY
        item {
            val relay1 = status.relay1Soil
            val (label, style) = when {
                !isOnline || relay1 == null -> "UNKNOWN" to StatusBadgeStyle.GRAY
                relay1 -> "ON" to StatusBadgeStyle.GREEN
                else -> "OFF" to StatusBadgeStyle.GRAY
            }
            StatusCard(
                title = "SOIL RELAY",
                value = label,
                icon = Icons.Default.Power,
                statusStyle = style,
                subValue = if (isOnline) "Relay 1 Load" else "Offline",
                testTag = "home_status_soil_relay"
            )
        }

        // 8. WATER PUMP
        item {
            val relay2 = status.relay2Pump
            val (label, style) = when {
                !isOnline || relay2 == null -> "UNKNOWN" to StatusBadgeStyle.GRAY
                relay2 -> "ON" to StatusBadgeStyle.GREEN
                else -> "OFF" to StatusBadgeStyle.GRAY
            }
            StatusCard(
                title = "WATER PUMP",
                value = label,
                icon = Icons.Default.Power,
                statusStyle = style,
                subValue = if (isOnline) "Relay 2 Pump" else "Offline",
                testTag = "home_status_water_pump"
            )
        }

        // 9. FIRE
        item(span = { GridItemSpan(2) }) {
            val flame = status.flameDetected
            val (label, style) = when {
                !isOnline || flame == null -> "UNKNOWN" to StatusBadgeStyle.GRAY
                flame -> "DETECTED" to StatusBadgeStyle.RED
                else -> "SAFE" to StatusBadgeStyle.GREEN
            }
            StatusCard(
                title = "FIRE DETECTION",
                value = label,
                icon = Icons.Default.LocalFireDepartment,
                statusStyle = style,
                subValue = when {
                    !isOnline || flame == null -> "Hardware status unknown"
                    flame -> "Critical hazard: Active flame sensor reading!"
                    else -> "All thermal and optical parameters safe"
                },
                testTag = "home_status_fire"
            )
        }
    }
}
