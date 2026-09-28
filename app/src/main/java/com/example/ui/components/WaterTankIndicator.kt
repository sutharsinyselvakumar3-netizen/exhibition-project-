package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.NavyMuted
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGray
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusRed

@Composable
fun WaterTankIndicator(
    waterPercent: Int?,
    lowThreshold: Int,
    criticalThreshold: Int,
    modifier: Modifier = Modifier
) {
    val isAvailable = waterPercent != null
    val targetVal = (waterPercent ?: 0).coerceIn(0, 100).toFloat()
    val animatedFill by animateFloatAsState(
        targetValue = if (isAvailable) targetVal / 100f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "WaterLevelFill"
    )

    val (statusLabel, statusColor, statusBg, isLowAlert) = when {
        !isAvailable -> Quad("UNKNOWN (OFFLINE)", StatusGray, Color(0xFFF1F5F9), false)
        targetVal <= criticalThreshold -> Quad("CRITICAL", StatusRed, Color(0xFFFEE2E2), true)
        targetVal <= lowThreshold -> Quad("LOW", StatusOrange, Color(0xFFFFEDD5), true)
        targetVal >= 75 -> Quad("HIGH", StatusBlue, Color(0xFFDBEAFE), false)
        else -> Quad("NORMAL", StatusGreen, Color(0xFFDCFCE7), false)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("water_tank_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Water Level",
                        tint = StatusBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WATER LEVEL",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = DarkNavy
                    )
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Warning banner if Low or Critical
            if (isAvailable && isLowAlert) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = if (targetVal <= criticalThreshold) Color(0xFFFEE2E2) else Color(0xFFFFEDD5),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Low Water Warning",
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (targetVal <= criticalThreshold) "⚠ CRITICAL WATER LEVEL - REFILL IMMEDIATELY" else "⚠ LOW WATER - REFILL RESERVOIR SOON",
                            color = statusColor,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tank visual layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Vertical water tank container
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(2.dp, Color(0xFFCBD5E1), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Tank fill animation
                    if (isAvailable && animatedFill > 0f) {
                        val liquidBrush = Brush.verticalGradient(
                            colors = if (isLowAlert) {
                                listOf(statusColor.copy(alpha = 0.8f), statusColor)
                            } else {
                                listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                            }
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(animatedFill)
                                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                                .background(liquidBrush)
                        )
                    }

                    // Tank level tick lines
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 0..4) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.35f)
                                    .height(1.dp)
                                    .background(Color(0xFF94A3B8))
                            )
                        }
                    }
                }

                // Readings and Thresholds breakdown
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isAvailable) "$waterPercent%" else "--%",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 36.sp
                        ),
                        color = if (isAvailable) DarkNavy else StatusGray
                    )
                    Text(
                        text = "CAPACITY STATUS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = NavyMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ThresholdRow(label = "Critical", value = "≤ $criticalThreshold%", color = StatusRed)
                    Spacer(modifier = Modifier.height(4.dp))
                    ThresholdRow(label = "Low Warning", value = "≤ $lowThreshold%", color = StatusOrange)
                    Spacer(modifier = Modifier.height(4.dp))
                    ThresholdRow(label = "Normal / High", value = "> $lowThreshold%", color = StatusGreen)
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun ThresholdRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = DarkNavy
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
    }
}
