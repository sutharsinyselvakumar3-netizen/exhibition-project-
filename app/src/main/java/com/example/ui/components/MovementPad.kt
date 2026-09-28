package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RobotMovementState
import com.example.ui.theme.AgriBlue
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.NavyMuted
import com.example.ui.theme.StatusGray
import com.example.ui.theme.StatusRed

@Composable
fun MovementPad(
    currentState: RobotMovementState,
    isEnabled: Boolean,
    onCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "L298N DUAL DC MOTOR DRIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = NavyMuted
                )

                Surface(
                    color = if (currentState == RobotMovementState.STOPPED) StatusGray.copy(alpha = 0.15f) else Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = currentState.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (currentState == RobotMovementState.STOPPED) StatusGray else Color(0xFF16A34A),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Forward Button
            DirectionButton(
                icon = Icons.Default.ArrowUpward,
                label = "FORWARD",
                isActive = currentState == RobotMovementState.FORWARD,
                isEnabled = isEnabled,
                testTag = "motor_forward_button",
                onPressStateChanged = { pressed ->
                    if (pressed) onCommand("forward") else onCommand("stop")
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Left, Stop, Right
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DirectionButton(
                    icon = Icons.Default.ArrowBack,
                    label = "LEFT",
                    isActive = currentState == RobotMovementState.LEFT,
                    isEnabled = isEnabled,
                    testTag = "motor_left_button",
                    onPressStateChanged = { pressed ->
                        if (pressed) onCommand("left") else onCommand("stop")
                    }
                )

                Spacer(modifier = Modifier.size(12.dp))

                // Center STOP button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(if (isEnabled) StatusRed.copy(alpha = 0.12f) else StatusGray.copy(alpha = 0.1f))
                        .clickable(enabled = isEnabled) { onCommand("stop") }
                        .testTag("motor_stop_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = if (isEnabled) StatusRed else StatusGray,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "STOP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            ),
                            color = if (isEnabled) StatusRed else StatusGray
                        )
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))

                DirectionButton(
                    icon = Icons.Default.ArrowForward,
                    label = "RIGHT",
                    isActive = currentState == RobotMovementState.RIGHT,
                    isEnabled = isEnabled,
                    testTag = "motor_right_button",
                    onPressStateChanged = { pressed ->
                        if (pressed) onCommand("right") else onCommand("stop")
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reverse Button
            DirectionButton(
                icon = Icons.Default.ArrowDownward,
                label = "REVERSE",
                isActive = currentState == RobotMovementState.REVERSE,
                isEnabled = isEnabled,
                testTag = "motor_reverse_button",
                onPressStateChanged = { pressed ->
                    if (pressed) onCommand("reverse") else onCommand("stop")
                }
            )
        }
    }
}

@Composable
private fun DirectionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    isEnabled: Boolean,
    testTag: String,
    onPressStateChanged: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isEnabled) {
            onPressStateChanged(isPressed)
        }
    }

    val bgColor = when {
        !isEnabled -> StatusGray.copy(alpha = 0.15f)
        isPressed || isActive -> AgriBlue
        else -> Color(0xFFF1F5F9)
    }

    val contentColor = when {
        !isEnabled -> StatusGray
        isPressed || isActive -> Color.White
        else -> DarkNavy
    }

    Box(
        modifier = Modifier
            .size(width = 84.dp, height = 64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = AgriBlue),
                enabled = isEnabled,
                onClick = {}
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = contentColor
            )
        }
    }
}
