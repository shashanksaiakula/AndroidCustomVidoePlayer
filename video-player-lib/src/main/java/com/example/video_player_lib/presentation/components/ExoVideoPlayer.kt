package com.example.video_player_lib.presentation.components

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Forward5
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.WidthFull
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.example.video_player_lib.presentation.VideoPickerViewModel
import com.example.video_player_lib.utils.formatTime
import com.example.video_player_lib.utils.timeStampToLong

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExoVideoPlayer(
    id: Long,
    uri: Uri,
    name: String,
    modifier: Modifier = Modifier,
    mimeType: String? = null,
    viewModel: VideoPickerViewModel = hiltViewModel(),
    isFullScreen : (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    
    // States for transient feedback
    var isForwardVisible by remember { mutableStateOf(false) }
    var isRewindVisible by remember { mutableStateOf(false) }
    var isShowFastForward by remember { mutableStateOf(false) }

    var controlsVisible by remember { mutableStateOf(true) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    val noteList = viewModel.listOfNotes.collectAsState().value
    val window = activity?.window
    val view = LocalView.current
    
    val isFullScreen by viewModel.isFullScreen.collectAsState()
    val isRotation by viewModel.isRotation.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Preserve resize mode across rotation
    var resizeMode by rememberSaveable {
        mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT)
    }

    LaunchedEffect(uri, mimeType) {
        viewModel.preparePlayer(uri, mimeType)
    }

    DisposableEffect(viewModel.exoPlayer) {
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {}
        }
        viewModel.exoPlayer.addListener(listener)
        onDispose {
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
            viewModel.exoPlayer.removeListener(listener)
            viewModel.exoPlayer.stop()
            viewModel.onRotation(false)
            viewModel.onBackPress(false)
            viewModel.onVideoSelected(0)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Auto-hide controls
    LaunchedEffect(controlsVisible, isPlaying) {
        if (controlsVisible && isPlaying) {
            delay(3000)
            controlsVisible = false
        }
    }

    // Transient visibility durations
    LaunchedEffect(isForwardVisible) { if (isForwardVisible) { delay(600); isForwardVisible = false } }
    LaunchedEffect(isRewindVisible) { if (isRewindVisible) { delay(600); isRewindVisible = false } }

    // System Bar Management
    LaunchedEffect(isFullScreen, isRotation, isLandscape) {
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            if (isFullScreen || isRotation || isLandscape) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                isFullScreen(true)
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
                isFullScreen(false)
            }
        }
    }

    // Orientation Management
    LaunchedEffect(isRotation) {
        if (isRotation) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    Box(
        modifier = modifier
            .background(Color.Black)
            .then(
                if (isFullScreen || isRotation || isLandscape) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(10/9f)
                }
            )
            .clipToBounds() // Ensure content stays inside the frame
            .zIndex(10f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { controlsVisible = !controlsVisible },
                    onDoubleTap = { offset ->
                        val isRight = offset.x > size.width / 2
                        if (isRight) {
                            viewModel.skipForward()
                            isForwardVisible = true
                        } else {
                            viewModel.skipBackward()
                            isRewindVisible = true
                        }
                    },
                    onLongPress = {
                        viewModel.onLongPress(2.0f)
                        isShowFastForward = true
                    },
                    onPress = {
                        try {
                            awaitRelease()
                        } finally {
                            viewModel.onLongPress(1.0f)
                            isShowFastForward = false
                        }
                    }
                )
            }
    ) {
        // 1. Video Player - Constrained to the Box bounds
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = viewModel.exoPlayer
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.resizeMode = resizeMode
            }
        )

        // 2. Feedback Indicators (Always centered, independent of controls layout)
        Box(modifier = Modifier.fillMaxSize()) {
            // Rewind feedback
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.35f),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = isRewindVisible,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White.copy(0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Replay5, null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                }
            }

            // Forward feedback
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxWidth(0.35f),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = isForwardVisible,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White.copy(0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Forward5, null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                }
            }

            // 2x Fast Forward indicator
            if (isShowFastForward) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("2x", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.FastForward, null, tint = Color.White)
                    }
                }
            }
        }

        // 3. Controls Overlay
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
//                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .align(Alignment.TopCenter)
                ) {
                    CustomTopBar(
                        title = name,
                        isVideoPage = true,
                        navigationIcon = {
                            IconButton(onClick = {
                                viewModel.onBackPress(true)
                                viewModel.viewFullScreen(false)
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

                DropdownMenu(expanded = showSpeedMenu, onDismissRequest = { showSpeedMenu = false }) {
                    listOf(0.5f, 0.75f, 1.0f, 1.5f, 1.75f, 2.0f).forEach { speed ->
                        DropdownMenuItem(
                            text = { Text("${speed}x ${if (speed == 1.0f) "(Normal)" else ""}") },
                            onClick = {
                                viewModel.setPlaybackSpeed(speed)
                                showSpeedMenu = false
                            }
                        )
                    }
                }

                // Play/Pause - Pure Center
                IconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(72.dp)
                        .background(Color.White.copy(0.3f), CircleShape)
                        .zIndex(5f)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Bottom Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
//                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .padding(bottom = 16.dp, top = 8.dp)
                ) {
                    // Time and Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Resize Mode Button
                            IconButton(onClick = {
                                resizeMode = when (resizeMode) {
                                    AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                    else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                            }) {
                                Icon(
                                    imageVector = when (resizeMode) {
                                        AspectRatioFrameLayout.RESIZE_MODE_FIT -> Icons.Default.AspectRatio
                                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> Icons.Default.Fullscreen
                                        AspectRatioFrameLayout.RESIZE_MODE_FILL -> Icons.Default.WidthFull
                                        else -> Icons.Default.AspectRatio
                                    },
                                    contentDescription = "Resize",
                                    tint = Color.White
                                )
                            }
                            // Rotation Button
                            IconButton(onClick = { viewModel.onRotation(!isRotation) }) {
                                Icon(Icons.Default.ScreenRotation, "Rotate", tint = Color.White)
                            }
                            // Fullscreen Button
                            IconButton(onClick = { viewModel.viewFullScreen(!isFullScreen) }) {
                                Icon(if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen, "Fullscreen", tint = Color.White)
                            }
                        }
                    }

                    // Progress Slider
                    Slider(
                        value = currentPosition.coerceIn(0L, duration.coerceAtLeast(0L)).toFloat(),
                        onValueChange = { viewModel.seekTo(it.toLong()) },
                        valueRange = 0f..duration.coerceAtLeast(1L).toFloat(),
                        modifier = Modifier.fillMaxWidth().height(16.dp).padding(horizontal = 16.dp),
                        thumb = { Box(modifier = Modifier.size(16.dp).background(Color.Gray, CircleShape)) },
                        track = { sliderState ->
                            SliderDefaults.Track(
                                sliderState = sliderState,
                                modifier = Modifier.height(4.dp)
                                    .drawWithContent{
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
                                                        ) / duration.coerceAtLeast(1L).toFloat()
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
            }
        }
    }
}
