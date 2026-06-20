package com.example.video_player_lib.api.view

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.video_player_lib.api.viewmodel.TranscriptViewModel
import com.example.video_player_lib.api.viewmodel.VideoPlayerViewModel
import com.example.video_player_lib.presentation.components.BottomModel
import com.example.video_player_lib.presentation.components.CustomTopBar
import kotlinx.coroutines.delay

@SuppressLint("ViewModelConstructorInComposable")
@OptIn(UnstableApi::class)
@Composable
fun CustomVideoPlayer(
    modifier: Modifier = Modifier,
    viewModel: VideoPlayerViewModel,
    uri: Uri,
    transcriptViewModel: TranscriptViewModel,
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
    val showVolume by viewModel.showVolume.collectAsState()
    var showValue by remember { mutableStateOf(false) }
    var showBifgtness by remember { mutableStateOf(false) }
    var isRightDrag by remember { mutableStateOf(false) }
    var initialVolume by remember { mutableStateOf(0f) }
    var initialBrightness by remember { mutableStateOf(0.5f) }
    var currentDragVolume by remember { mutableStateOf(0f) }
    var currentDragBrightness by remember { mutableStateOf(0.5f) }
    var delaySliderVolume by remember { mutableStateOf(false) }
    var delaySliderBrightness by remember { mutableStateOf(false) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    val volume by viewModel.valume.collectAsState()

    val view = LocalView.current

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
        viewModel.getIdFromUri(uri)
        // 1. Load model ONCE when screen launches
        transcriptViewModel.updateUri(uri)
    }

    LaunchedEffect(isForwardVisible) {
        if (isForwardVisible) {
            delay(600)
            isForwardVisible = false
        }
    }

    LaunchedEffect(isPlaying, controllVisibility, showVolume) {
        if (controllVisibility && !showVolume) {
            delay(3000)
            controllVisibility = false
        }
    }
    LaunchedEffect(showBifgtness, showValue) {
        if (showBifgtness) {
            delay(1500)
            showBifgtness = false
        }
        if (showValue) {
            delay(1500)
            showValue = false
        }
    }

    // NOTE: Do not auto-start heavy transcription when uriData changes. Transcription is started
    // on-demand from the UI (e.g., when user selects the Transcript tab) to avoid background CPU usage.

    Box(
        modifier = modifier
            .then(videoModifier)
            .background(Color.Black)
            .clipToBounds()
            .pointerInput(Unit) {
                if (showOverLayUI) {
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
            }
            .pointerInput(Unit) {
                if (showOverLayUI)
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            isRightDrag = offset.x > size.width / 2
                            if (isRightDrag) {

                                val window = (view.context as? Activity)?.window
                                val layoutParams = window?.attributes
                                initialBrightness =
                                    layoutParams?.screenBrightness?.takeIf { it >= 0f } ?: 0.5f
                                currentDragBrightness = initialBrightness
                                showBifgtness = true
                                delaySliderBrightness = true
                            } else {
                                initialVolume = volume
                                currentDragVolume = initialVolume
                                showValue = true
                                delaySliderVolume = true
                            }
                        },
//                        onDragEnd = {
//                            if (isRightDrag) {
//                                showBifgtness = false
//                            } else {
//                                showValue = false
//                            }
//                        },
                        onDragCancel = {
                            if (isRightDrag) {
                                val window = (view.context as? Activity)?.window
                                window?.let {
                                    val layoutParams = it.attributes
                                    layoutParams.screenBrightness = initialBrightness
                                    it.attributes = layoutParams
                                }
                                showBifgtness = false
                            } else {
//                                viewModel.exoPlayer.volume = initialVolume
                                viewModel.volumeReading(initialVolume)
                                showValue = false
                            }
                        },
                        onVerticalDrag = { _, dragAmount ->
                            val deltaY = dragAmount
                            val sensitivity = 1f / 500f
                            val changeAmount = -deltaY * sensitivity

                            if (isFullScreen) {
                                if (isRightDrag) {
                                    val newBrightness =
                                        (currentDragBrightness + changeAmount).coerceIn(0f, 1f)
                                    currentDragBrightness = newBrightness
                                    val window = (view.context as? Activity)?.window
                                    window?.let {
                                        val layoutParams = it.attributes
                                        layoutParams.screenBrightness = newBrightness
                                        it.attributes = layoutParams
                                    }
                                } else {
                                    val newVolume =
                                        (currentDragVolume + changeAmount).coerceIn(0f, 1f)
                                    currentDragVolume = newVolume
//                                viewModel.exoPlayer.volume = newVolume
                                    viewModel.volumeReading(newVolume)

                                }
                            }
                        }
                    )
            },
//        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
//                        .background(Color.Red.copy(alpha = 0.5f), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
//                .align(Alignment.TopCenter)
                .zIndex(1f)
        ) {
            CustomTopBar(
                title = "",
                isVideoPage = true,
                navigationIcon = {
                    IconButton(onClick = {

                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showSpeedMenu = !showSpeedMenu }) {
                        Icon(Icons.Default.MoreVert, "Settings", tint = Color.White)
                    }
                }
            )
        }
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
        if (isLongPress) {
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
                    modifier = Modifier
                        .fillMaxWidth()
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
        if (isFullScreen) {
            Log.e("check", "CustomVideoPlayer: $showBifgtness & $showValue")
            if (showBifgtness) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp, bottom = 15.dp)
                        .background(
                            Color.LightGray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedVisibility(
                        visible = delaySliderBrightness,
                    )
                    {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.width(80.dp).padding(vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier.height(100.dp).width(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CustomSlider(
                                    currentDuration = (currentDragBrightness * 1000).toLong(),
                                    totalDuration = 1000,
                                    onValueChage = { sliderValue ->
                                        val newBrightness = (sliderValue / 1000f).coerceIn(0f, 1f)
                                        currentDragBrightness = newBrightness
                                        val window = (view.context as? Activity)?.window
                                        window?.let {
                                            val layoutParams = it.attributes
                                            layoutParams.screenBrightness = newBrightness
                                            it.attributes = layoutParams
                                        }
                                    },
                                    modifier = Modifier
                                        .requiredWidth(130.dp)
                                        .requiredHeight(40.dp)
                                        .rotate(270f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Brightness7,
                                    contentDescription = "Brightness",
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(24.dp),
                                    tint = Color.White
                                )
                                Text(
                                    text = "${(currentDragBrightness * 100).toInt()}%",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            if (showValue) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp, bottom = 15.dp)
                        .background(
                            Color.LightGray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedVisibility(
                        visible = delaySliderVolume,
                    )
                    {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.width(80.dp).padding(vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier.height(100.dp).width(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CustomSlider(
                                    currentDuration = (currentDragVolume * 1000).toLong(),
                                    totalDuration = 1000,
                                    onValueChage = { sliderValue ->
                                        val newVolume = (sliderValue / 1000f).coerceIn(0f, 1f)
                                        currentDragVolume = viewModel.valume.value
//                                        viewModel.exoPlayer.volume = newVolume
                                        viewModel.volumeReading(newVolume)
                                        if (newVolume == 0f) viewModel.mute(true) else viewModel.mute(
                                            false
                                        )
                                    },
                                    modifier = Modifier
                                        .requiredWidth(130.dp)
                                        .requiredHeight(40.dp)
                                        .rotate(270f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Volume",
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(24.dp),
                                    tint = Color.White
                                )
                                Text(
                                    text = "${(currentDragVolume * 100).toInt()}%",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Double Tap Indicators (placed outside controllVisibility so they work anytime)
        AnimatedVisibility(
            visible = isForwardVisible && !isRifgtDoubleTap,
            enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
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
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (!isLongPress) {
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

        BottomModel(
            showSheet = showSpeedMenu,
            onDismiss = { showSpeedMenu = !showSpeedMenu },
            doubleTapSeek = {
                viewModel.setDoubleTapSeek(it)
            },
            onSpeedChange = {
                viewModel.onLongPress(it)
            },
            onLongPress ={
                viewModel.setFastPlaySpreed(it)
            },
            isVideo = true,

        )
    }
}
