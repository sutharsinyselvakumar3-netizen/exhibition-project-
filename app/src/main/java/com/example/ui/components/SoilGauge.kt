package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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

@Composable
fun SoilGauge(
    soilPercent: Int?,
    dryThreshold: Int,
    wetThreshold: Int,
    modifier: Modifier = Modifier
) {
    val isAvailable = soilPercent != null
    val targetVal = (soilPercent ?: 0).coerceIn(0, 100).toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = if (isAvailable) targetVal / 100f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "SoilProgress"
    )

    val (stateLabel, stateColor, stateBg) = when {
        !isAvailable -> Triple("UNKNOWN (OFFLINE)", StatusGray, Color(0xFFF1F5F9))
        targetVal < dryThreshold -> Triple("DRY", StatusOrange, Color(0xFFFFEDD5))
        targetVal > wetThreshold -> Triple("WET", StatusBlue, Color(0xFFDBEAFE))
        else -> Triple("NORMAL", StatusGreen, Color(0xFFDCFCE7))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("soil_moisture_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SOIL MOISTURE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = DarkNavy
                )

                Surface(
                    color = stateBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stateLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = stateColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.size(170.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(170.dp)) {
                    val strokeW = 16.dp.toPx()
                    val arcSize = size.width - strokeW
                    val arcOffset = Offset(strokeW / 2, strokeW / 2)

                    // Track background arc (240 degrees)
                    drawArc(
                        color = Color(0xFFE2E8F0),
                        startAngle = 150f,
                        sweepAngle = 240f,
                        useCenter = false,
                        topLeft = arcOffset,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )

                    // Value arc
                    if (isAvailable && animatedProgress > 0f) {
                        drawArc(
                            color = stateColor,
                            startAngle = 150f,
                            sweepAngle = 240f * animatedProgress,
                            useCenter = false,
                            topLeft = arcOffset,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isAvailable) "$soilPercent%" else "--%",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        ),
                        color = if (isAvailable) DarkNavy else StatusGray
                    )
                    Text(
                        text = "RANGE: 0–100%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        ),
                        color = NavyMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    text = "Dry: < $dryThreshold%",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = StatusOrange,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Normal: $dryThreshold–$wetThreshold%",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = StatusGreen,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Wet: > $wetThreshold%",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = StatusBlue,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
