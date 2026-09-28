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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.components.FlameWarningBanner
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.NavyMuted
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGray
import com.example.ui.theme.StatusGreen

@Composable
fun RelaysFireScreen(
    status: RobotStatus,
    cameraState: StreamState,
    cameraIp: String,
    onRetryCamera: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleRelaySoil: () -> Unit,
    onToggleRelayPump: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isOnline = status.connected
    val isRelay1On = status.relay1Soil == true
    val isRelay2On = status.relay2Pump == true

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

        // Relay 1 Card: Soil / Load
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("relay_1_card"),
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
                            imageVector = Icons.Default.Power,
                            contentDescription = "Relay 1",
                            tint = if (isRelay1On) StatusGreen else StatusGray,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "RELAY 1: SOIL / LOAD",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = DarkNavy
                            )
                            Text(
                                text = if (!isOnline) "OFFLINE" else if (isRelay1On) "POWER ACTIVE (ON)" else "DISABLED (OFF)",
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
                        modifier = Modifier.testTag("relay_1_switch")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = NavyMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Channel 1 controls auxiliary soil sensor power / load circuit.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = NavyMuted
                        )
                    }
                }
            }
        }

        // Relay 2 Card: Water Pump
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("relay_2_card"),
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
                            imageVector = Icons.Default.WaterDamage,
                            contentDescription = "Relay 2 Pump",
                            tint = if (isRelay2On) StatusBlue else StatusGray,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "RELAY 2: WATER PUMP",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = DarkNavy
                            )
                            Text(
                                text = if (!isOnline) "OFFLINE" else if (isRelay2On) "PUMP DISCHARGING (ON)" else "PUMP IDLE (OFF)",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = if (isRelay2On) StatusBlue else StatusGray
                            )
                        }
                    }

                    Switch(
                        checked = isRelay2On,
                        onCheckedChange = { onToggleRelayPump() },
                        enabled = isOnline,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = StatusBlue,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.testTag("relay_2_switch")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Notice",
                            tint = NavyMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Channel 2 controls physical DC submersible pump through optocoupled relay.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = NavyMuted
                        )
                    }
                }
            }
        }

        // Flame Sensor & Fire Detection Banner
        FlameWarningBanner(
            flameDetected = if (isOnline) status.flameDetected else null
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
