package com.example.video_player_lib.api.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.example.video_player_lib.api.viewmodel.VideoPlayerViewModel
import com.example.video_player_lib.utils.timeStampToLong

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSlider(modifier: Modifier = Modifier,
                 currentDuration: Long = 0L, totalDuration: Long = 0L,
                 onValueChage : (value: Float) -> Unit ={},
                 onValueChangeFinished: () -> Unit = {},
                 id: String = "",                   // Current video ID to filter matching notes
                 noteList: Map<String, List<String>> = emptyMap(), // Map containing timestamp note strings
) {

    var localSliderValue by remember { mutableFloatStateOf(currentDuration.toFloat()) }

    // UI state block: Prevents background ExoPlayer ticks from hijacking user input midway
    var isUserDragging by remember { mutableStateOf(false) }

    LaunchedEffect(currentDuration) {
        if (!isUserDragging) {
            localSliderValue = currentDuration.toFloat()
        }
    }

    Slider(
        value = localSliderValue.coerceIn(0f, totalDuration.toFloat().coerceAtLeast(1f)),
        onValueChange = {
            isUserDragging = true
            localSliderValue = it
            onValueChage(it) },
        onValueChangeFinished = {
            isUserDragging = false
            onValueChangeFinished() // Re-enables standard background ticker tracking safely
        },
        valueRange = 0f..totalDuration.toFloat(),
        modifier = modifier
            .height(24.dp)
            .padding(horizontal = 8.dp),
        thumb = {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.Gray, CircleShape)
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                modifier = Modifier
                    .height(4.dp)
                    .drawWithContent {
                        drawContent()
                        val trackWidth = size.width
                        val trackHeight = size.height

                        noteList.forEach {
                            if (id == it.key) {
                                it.value.forEach { note ->
                                    val progress =
                                        timeStampToLong(
                                            note.substringBefore(
                                                " - "
                                            ).trim()
                                        ) / totalDuration.coerceAtLeast(1L).toFloat()
                                    val xOffset = progress * trackWidth
                                    drawRect(
                                        Color.Red,
                                        topLeft = Offset(xOffset - 1f, 0f),
                                        size = Size(16f, trackHeight + 2)
                                    )
                                }
                            }
                        }
                    },
                drawStopIndicator = null,
                thumbTrackGapSize = 0.dp,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.Black.copy(alpha = 0.5f)
                )
            )
        }
    )
}