package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RobotStatus
import com.example.model.StreamState
import com.example.ui.components.CameraStreamView
import com.example.ui.components.SoilGauge
import com.example.ui.components.WaterTankIndicator
import com.example.ui.theme.AgriBlue
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.NavyMuted
import com.example.ui.theme.StatusGray
import com.example.ui.theme.StatusGreen

@Composable
fun SoilWaterScreen(
    status: RobotStatus,
    cameraState: StreamState,
    cameraIp: String,
    soilDryThreshold: Int,
    soilWetThreshold: Int,
    waterLowThreshold: Int,
    waterCriticalThreshold: Int,
    onRetryCamera: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleRelaySoil: () -> Unit,
    onSoilServoChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isOnline = status.connected
    val isRelay1On = status.relay1Soil == true

    var sliderSoilServo by remember(status.soilServoAngle) {
        mutableFloatStateOf(status.soilServoAngle.coerceIn(0, 45).toFloat())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Real ESP32-CAM stream at top
        CameraStreamView(
            streamState = cameraState,
            cameraIp = cameraIp,
            onRetry = onRetryCamera,
            onOpenSettings = onOpenSettings
        )

        // Relay 1: Soil / Load Relay Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("soil_relay_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = BorderStroke(1.dp, BorderLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Power,
                        contentDescription = "Relay 1",
                        tint = if (isRelay1On) StatusGreen else StatusGray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SOIL / LOAD RELAY",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = DarkNavy
                        )
                        Text(
                            text = if (!isOnline) "OFFLINE" else if (isRelay1On) "POWER ENGAGED (ON)" else "DISENGAGED (OFF)",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = if (isRelay1On) StatusGreen else StatusGray
                        )
                    }
                }

                Switch(
                    checked = isRelay1On,
                    onCheckedChange = { onToggleRelaySoil() },
                    enabled = isOnline,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = StatusGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.testTag("soil_relay_switch")
                )
            }
        }

        // Soil Moisture Circular Gauge
        SoilGauge(
            soilPercent = if (isOnline) status.soilMoisture else null,
            dryThreshold = soilDryThreshold,
            wetThreshold = soilWetThreshold
        )

        // Servo 2 — Soil Sensor Arm (Strict Range 0–45° Only!)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("soil_servo_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = BorderStroke(1.dp, BorderLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Soil Arm Servo",
                            tint = if (isOnline) AgriBlue else StatusGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "SOIL SENSOR SERVO 2",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = DarkNavy
                            )
                            Text(
                                text = "Strict Range: 0–45° Only",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = NavyMuted
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isOnline) "Angle: ${sliderSoilServo.toInt()}°" else "OFFLINE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            ),
                            color = if (isOnline) AgriBlue else StatusGray,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // STRICT range 0 to 45 degrees
                Slider(
                    value = sliderSoilServo,
                    onValueChange = {
                        sliderSoilServo = it.coerceIn(0f, 45f)
                    },
                    onValueChangeFinished = {
                        val finalAngle = sliderSoilServo.toInt().coerceIn(0, 45)
                        onSoilServoChange(finalAngle)
                    },
                    valueRange = 0f..45f,
                    enabled = isOnline,
                    colors = SliderDefaults.colors(
                        thumbColor = AgriBlue,
                        activeTrackColor = AgriBlue,
                        inactiveTrackColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("soil_servo_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0° (Retracted)", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = NavyMuted)
                    Text("22° (Mid)", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = NavyMuted)
                    Text("45° (Max Ground Contact)", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = NavyMuted)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick buttons: HOME (0°), CENTER (22°)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            sliderSoilServo = 0f
                            onSoilServoChange(0)
                        },
                        enabled = isOnline,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("soil_servo_home_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy)
                    ) {
                        Text("HOME (0°)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            sliderSoilServo = 22f
                            onSoilServoChange(22)
                        },
                        enabled = isOnline,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("soil_servo_center_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy)
                    ) {
                        Text("CENTER (22°)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    ElevatedButton(
                        onClick = {
                            sliderSoilServo = 45f
                            onSoilServoChange(45)
                        },
                        enabled = isOnline,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("soil_servo_measure_btn"),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = AgriBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text("PROBE (45°)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Water Level Tank Indicator
        WaterTankIndicator(
            waterPercent = if (isOnline) status.waterLevel else null,
            lowThreshold = waterLowThreshold,
            criticalThreshold = waterCriticalThreshold
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
