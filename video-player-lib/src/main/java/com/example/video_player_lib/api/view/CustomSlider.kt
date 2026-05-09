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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.example.video_player_lib.api.viewmodel.VideoPlayerViewModel

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSlider(modifier: Modifier = Modifier, viewModel: VideoPlayerViewModel) {

    val currentPosition = viewModel.currentPosition.collectAsState().value
    val duration = viewModel.duration.collectAsState().value


    Slider(
        value = currentPosition.coerceIn(0L,duration.coerceAtLeast(0L)).toFloat(),
        onValueChange = { viewModel.seekTo(it.toLong()) },
        valueRange = 0f..duration.coerceAtLeast(1L).toFloat(),
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .padding(horizontal = 8.dp, )
        ,
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

//                        noteList.forEach {
//                            if (id == it.key) {
//                                it.value.forEach { note ->
//                                    val progress =
//                                        timeStampToLong(
//                                            note.substringBefore(
//                                                " - "
//                                            ).trim()
//                                        ) / duration.coerceAtLeast(1L).toFloat()
//                                    val xOffset = progress * trackWidth
//                                    drawRect(
//                                        Color.Red,
//                                        topLeft = Offset(xOffset - 1f, 0f),
//                                        size = Size(16f, trackHeight + 2)
//                                    )
//                                }
//                            }
//                        }
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