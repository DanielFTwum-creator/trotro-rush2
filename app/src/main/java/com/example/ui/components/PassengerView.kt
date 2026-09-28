package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppThemeMode
import com.example.model.Colour
import com.example.ui.theme.LocalAppThemeMode

@Composable
fun PassengerView(
    colour: Colour,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    showBorder: Boolean = true
) {
    val themeMode = LocalAppThemeMode.current
    val bgColor = when (themeMode) {
        AppThemeMode.LIGHT -> colour.lightColor
        AppThemeMode.DARK -> colour.darkColor
        AppThemeMode.HIGH_CONTRAST -> colour.highContrastColor
    }

    val borderColor = if (themeMode == AppThemeMode.HIGH_CONTRAST) Color.White else Color(0x66FFFFFF)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor)
            .then(
                if (showBorder) Modifier.border(1.5.dp, borderColor, CircleShape) else Modifier
            )
            .semantics {
                contentDescription = "${colour.displayName} passenger with ${colour.symbolDescription} symbol"
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = colour.symbolChar,
            color = colour.onColor,
            fontWeight = FontWeight.Black,
            fontSize = (size.value * 0.45).sp
        )
    }
}
