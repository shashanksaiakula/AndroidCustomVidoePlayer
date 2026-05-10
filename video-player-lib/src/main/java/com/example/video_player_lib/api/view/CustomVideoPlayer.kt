package com.example.video_player_lib.api.view

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.video_player_lib.api.viewmodel.VideoPlayerViewModel
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun CustomVideoPlayer(
    modifier: Modifier = Modifier,
    viewModel: VideoPlayerViewModel,
    uri: Uri,
    showOverLayUI: Boolean = true,
    slider: @Composable (modifier: Modifier, onRatioReceived: (Int) -> Unit) -> Unit = { sliderModifier, onRatioReceived ->
        CompleteSlider(
            modifier = sliderModifier,
            viewModel = viewModel,
            onRatioReceived = onRatioReceived
        )
    }
) {
    val resizeMode by viewModel.resize.collectAsState()
    val isFullScreen by viewModel.isFullScreen.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPos by viewModel.currentPosition.collectAsState()
    val totalDur by viewModel.duration.collectAsState()
    val fastPalySpeed by viewModel.FastPlaybackSpeed.collectAsState()
    val doubleTapSeek by viewModel.doubleTapSeek.collectAsState()
    var isLongPress by remember { mutableStateOf(false) }
    var isRifgtDoubleTap by remember { mutableStateOf(false) }
    var controllVisibility by remember { mutableStateOf(true) }
    var isForwardVisible by remember { mutableStateOf(false) }

    val playPauseState = when {
        isPlaying -> "Pause"
        totalDur > 0 && currentPos >= totalDur - 100 -> "Replay"
        else -> "Play"
    }

    val videoModifier = if (isFullScreen) {
        Modifier.fillMaxSize()
    } else {
        Modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f)
    }

    LaunchedEffect(uri) {
        viewModel.preparePlayer(uri, null)
    }

    LaunchedEffect(isForwardVisible) {
        if (isForwardVisible) {
            delay(600)
            isForwardVisible = false
        }
    }

    LaunchedEffect(isPlaying, controllVisibility) {
        if (controllVisibility) {
            delay(3000)
            controllVisibility = false
        }
    }

    Box(
        modifier = modifier
            .then(videoModifier)
            .background(Color.Black)
            .clipToBounds()
            .pointerInput(Unit) {
                if(showOverLayUI) {
                    detectTapGestures(
                        onTap = { controllVisibility = !controllVisibility },
                        onDoubleTap = { offset ->
                            controllVisibility = false
                            val isRight = offset.x > size.width / 2
                            isRifgtDoubleTap = isRight
                            isForwardVisible = true
                            if (isRight) {
                                viewModel.seekByForward(doubleTapSeek)
                            } else {
                                viewModel.seekByReverse(doubleTapSeek)
                            }
                        },
                        onLongPress = {
                            isLongPress = true
                            viewModel.onLongPress(fastPalySpeed)
                            controllVisibility = true
                        },
                        onPress = {
                            try {
                                awaitRelease()
                            } finally {
                                isLongPress = false
                                controllVisibility = false
                                viewModel.onLongPress(1.0f)
                            }
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = viewModel.exoPlayer
                    useController = false
                    setBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { playerView ->
                playerView.resizeMode = resizeMode
            },
            modifier = Modifier.fillMaxSize()
        )
        if(isLongPress) {
            AnimatedVisibility(
                visible = isLongPress,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${fastPalySpeed.toInt()}X",
                        color = Color.White,
                        fontSize = 24.sp
                    )
                    CustomIcon(
                        icon = Icons.Default.FastForward,
                        contentDescription = "LongPress Indicator",
                    ) { }
                }
            }
        }

        // Double Tap Indicators (placed outside controllVisibility so they work anytime)
        AnimatedVisibility(
            visible = isForwardVisible && !isRifgtDoubleTap,
            enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
//            Box(modifier = Modifier
//                .fillMaxHeight()
//                .clip(shape = RoundedCornerShape(topEnd =80.dp, bottomEnd = 80.dp))
//                .fillMaxWidth(0.5f) // Covers left half
//                .background(Color.Black.copy(alpha = 0.3f))
//            )
            CustomOnDouble(
                visible = isForwardVisible && !isRifgtDoubleTap,
                icon = Icons.Default.Replay,
                seek = doubleTapSeek,
                isFarword = false
            ) { }
        }

        // Right Side Overlay
        AnimatedVisibility(
            visible = isForwardVisible && isRifgtDoubleTap,
            enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
//            Box(modifier = Modifier
//                .fillMaxHeight()
//                .clip(shape = RoundedCornerShape(topStart =80.dp, bottomStart = 80.dp))
//                .fillMaxWidth(0.5f) // Covers right half
//                .background(Color.Black.copy(alpha = 0.3f))
//            )
            CustomOnDouble(
                visible = isForwardVisible && isRifgtDoubleTap,
                icon = Icons.Default.Replay,
                seek = doubleTapSeek,
                isFarword = true
            ) { }
        }

        if (showOverLayUI) {
            AnimatedVisibility(
                visible = controllVisibility,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.matchParentSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                if(!isLongPress) {
                    CustomControls(
                        modifier = Modifier.align(Alignment.Center),
                        viewModel = viewModel,
                        playPause = playPauseState
                    )
                }
                    slider(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = if (isFullScreen) 24.dp else 8.dp)
                    ) { newMode ->
                        viewModel.updateSize(newMode)
                    }
                }
            }
        }
    }
}
