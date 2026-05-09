package com.example.video_player_lib.api.view

import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.video_player_lib.utils.formatTime

@Composable
fun DurationText(
    modifier: Modifier = Modifier,
    currentDuration: Long,
    totalDuration: Long,
    color: Color = Color.White,
    fontSize: TextUnit = 12.sp
) {
    Text(
        text = "${formatTime(currentDuration)} / ${formatTime(totalDuration)}",
        modifier = modifier,
        color = color,
        fontSize = fontSize
    )
}