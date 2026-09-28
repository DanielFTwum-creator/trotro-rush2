package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppThemeMode
import com.example.model.Vehicle
import com.example.ui.theme.LocalAppThemeMode

@Composable
fun SlotView(
    slotIndex: Int,
    vehicle: Vehicle?,
    isRecentlyMoved: Boolean = false,
    moveTrigger: Long = 0L,
    isReducedMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val themeMode = LocalAppThemeMode.current
    val shape = RoundedCornerShape(8.dp)

    if (vehicle == null) {
        // Free Slot
        Box(
            modifier = modifier
                .height(68.dp)
                .clip(shape)
                .background(
                    if (themeMode == AppThemeMode.HIGH_CONTRAST) Color.Black
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
                .border(
                    width = 1.5.dp,
                    color = if (themeMode == AppThemeMode.HIGH_CONTRAST) Color.White
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = shape
                )
                .semantics {
                    contentDescription = "Parking slot ${slotIndex + 1}: Free"
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "P${slotIndex + 1}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "FREE",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    } else {
        // Occupied Slot
        val vColor = when (themeMode) {
            AppThemeMode.LIGHT -> vehicle.colour.lightColor
            AppThemeMode.DARK -> vehicle.colour.darkColor
            AppThemeMode.HIGH_CONTRAST -> vehicle.colour.highContrastColor
        }

        Box(
            modifier = modifier.height(68.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(vColor)
                    .border(
                        width = 2.dp,
                        color = if (themeMode == AppThemeMode.HIGH_CONTRAST) Color.White else Color(0x88FFFFFF),
                        shape = shape
                    )
                    .semantics {
                        contentDescription = "Slot ${slotIndex + 1}: ${vehicle.colour.displayName} ${vehicle.type.displayName}, ${vehicle.boarded} of ${vehicle.seats} seats filled"
                    }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = vehicle.colour.symbolChar,
                            color = vehicle.colour.onColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = vehicle.type.displayName,
                            color = vehicle.colour.onColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }

                    Text(
                        text = "${vehicle.boarded}/${vehicle.seats}",
                        color = vehicle.colour.onColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    // Mini Seat Progress
                    val progress = (vehicle.boarded.toFloat() / vehicle.seats.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = vehicle.colour.onColor,
                        trackColor = Color(0x44000000)
                    )
                }
            }

            // Subtle Road Dust particle effect around the trotro vehicle in the bay
            if (isRecentlyMoved) {
                RoadDustEffect(
                    triggerKey = moveTrigger,
                    isReducedMotion = isReducedMotion,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
