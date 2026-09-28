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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.ui.components.EmergencyStopButton
import com.example.ui.components.MovementPad
import com.example.ui.theme.AgriBlue
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.NavyMuted
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGray

@Composable
fun RobotControlScreen(
    status: RobotStatus,
    cameraState: StreamState,
    cameraIp: String,
    onRetryCamera: () -> Unit,
    onOpenSettings: () -> Unit,
    onMotorCommand: (String) -> Unit,
    onSpeedChange: (Int) -> Unit,
    onCameraServoChange: (Int) -> Unit,
    onEmergencyStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isOnline = status.connected

    var sliderSpeed by remember(status.speed) { mutableFloatStateOf(status.speed.toFloat()) }
    var sliderCameraServo by remember(status.cameraServoAngle) { mutableFloatStateOf(status.cameraServoAngle.toFloat()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Real ESP32-CAM live stream at the top
        CameraStreamView(
            streamState = cameraState,
            cameraIp = cameraIp,
            onRetry = onRetryCamera,
            onOpenSettings = onOpenSettings
        )

        // Movement Controls (cross layout)
        MovementPad(
            currentState = status.movementState,
            isEnabled = isOnline,
            onCommand = onMotorCommand
        )

        // Speed Control Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("speed_control_card"),
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
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Speed",
                            tint = if (isOnline) StatusBlue else StatusGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SPEED",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = DarkNavy
                        )
                    }

                    Text(
                        text = if (isOnline) "${sliderSpeed.toInt()}%" else "OFFLINE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        ),
                        color = if (isOnline) AgriBlue else StatusGray
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Slider(
                    value = sliderSpeed,
                    onValueChange = {
                        sliderSpeed = it
                    },
                    onValueChangeFinished = {
                        onSpeedChange(sliderSpeed.toInt())
                    },
                    valueRange = 0f..100f,
                    enabled = isOnline,
                    colors = SliderDefaults.colors(
                        thumbColor = AgriBlue,
                        activeTrackColor = AgriBlue,
                        inactiveTrackColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("speed_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0% (Min)", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = NavyMuted)
                    Text("50%", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = NavyMuted)
                    Text("100% (Max)", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = NavyMuted)
                }
            }
        }

        // Camera Servo 1 Control Card (0–180 degrees)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("camera_servo_card"),
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
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera Servo",
                            tint = if (isOnline) AgriBlue else StatusGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CAMERA SERVO 1",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = DarkNavy
                        )
                    }

                    Text(
                        text = if (isOnline) "${sliderCameraServo.toInt()}°" else "OFFLINE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        ),
                        color = if (isOnline) AgriBlue else StatusGray
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Slider(
                    value = sliderCameraServo,
                    onValueChange = {
                        sliderCameraServo = it
                    },
                    onValueChangeFinished = {
                        onCameraServoChange(sliderCameraServo.toInt())
                    },
                    valueRange = 0f..180f,
                    enabled = isOnline,
                    colors = SliderDefaults.colors(
                        thumbColor = AgriBlue,
                        activeTrackColor = AgriBlue,
                        inactiveTrackColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("camera_servo_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0° (Left)", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = NavyMuted)
                    Text("90° (Center)", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = NavyMuted)
                    Text("180° (Right)", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = NavyMuted)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick buttons: LEFT, CENTER, RIGHT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            sliderCameraServo = 0f
                            onCameraServoChange(0)
                        },
                        enabled = isOnline,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("camera_servo_left_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy)
                    ) {
                        Text("LEFT (0°)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            sliderCameraServo = 90f
                            onCameraServoChange(90)
                        },
                        enabled = isOnline,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("camera_servo_center_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy)
                    ) {
                        Text("CENTER (90°)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            sliderCameraServo = 180f
                            onCameraServoChange(180)
                        },
                        enabled = isOnline,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("camera_servo_right_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy)
                    ) {
                        Text("RIGHT (180°)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Emergency Stop Button
        EmergencyStopButton(
            onEmergencyStop = onEmergencyStop
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
