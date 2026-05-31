package com.example.video_player_lib.api.view

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward5
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.example.video_player_lib.api.viewmodel.VideoPlayerViewModel

@OptIn(UnstableApi::class)
@Composable
fun CustomControls(
    modifier: Modifier = Modifier,
    viewModel: VideoPlayerViewModel,
    playPause: String = "Play",
) {

    val isFullScreen = viewModel.isFullScreen.collectAsState().value

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isFullScreen) Modifier.padding(
                    horizontal = 56.dp,
                    vertical = 16.dp
                ) else Modifier.padding(16.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (playPause == "Replay") Arrangement.SpaceBetween else Arrangement.SpaceAround
    ) {
        CustomIcon(
            icon = Icons.Default.SkipPrevious,
            contentDescription = "Forward 5 seconds",
            size = 48,
            onClick = { viewModel.playPervious() }
        )
        CustomIcon(
            icon = when (playPause) {
                "Play" -> Icons.Default.PlayArrow
                "Replay" -> Icons.Default.Replay
                else -> Icons.Default.Pause
            },
            contentDescription = "Play/Pause/Replay ",
            size = 48,
            onClick = { viewModel.togglePlayPause(playPause) }
        )
        CustomIcon(
            icon = Icons.Default.SkipNext,
            contentDescription = "Forward 5 seconds",
            size = 48,
            onClick = { viewModel.playNext() }
        )
    }

}