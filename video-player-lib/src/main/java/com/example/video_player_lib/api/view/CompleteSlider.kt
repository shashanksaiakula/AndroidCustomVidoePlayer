package com.example.video_player_lib.api.view

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.WidthFull
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import com.example.video_player_lib.api.viewmodel.VideoPlayerViewModel

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteSlider(
    modifier: Modifier = Modifier,
    viewModel: VideoPlayerViewModel,
    onRatioReceived : (Int) -> Unit = {}
) {

    val isFullScreen = viewModel.isFullScreen.collectAsState().value
    val resizeMode = viewModel.resize.collectAsState().value
    val context = LocalContext.current
    val currentPosition = viewModel.currentPosition.collectAsState().value
    val duration by viewModel.duration.collectAsState()

    LaunchedEffect(isFullScreen) {
        val activity = context.findActivity()
        val window = activity?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            if (isFullScreen) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            DurationText(
                modifier = Modifier.padding(horizontal = 16.dp),
                currentDuration = currentPosition,
                totalDuration = duration
            )
            if (isFullScreen) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomIcon(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Volume",
                        color = Color.White,
                        onClick = {}
                    )
                    CustomIcon(
                        icon = when (resizeMode) {
                            AspectRatioFrameLayout.RESIZE_MODE_FIT -> Icons.Default.AspectRatio
                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> Icons.Default.Fullscreen
                            AspectRatioFrameLayout.RESIZE_MODE_FILL -> Icons.Default.WidthFull
                            else -> Icons.Default.AspectRatio
                        },
                        contentDescription = "Resize",
                        color = Color.White
                    ) {
                        val nextMode = when (resizeMode) {
                            AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                            AspectRatioFrameLayout.RESIZE_MODE_FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                            else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                        viewModel.updateSize(nextMode)
                        onRatioReceived(nextMode)
                    }
                    CustomIcon(
                        icon = Icons.Default.FullscreenExit,
                        contentDescription = "Exit Fullscreen",
                        color = Color.White,
                        onClick = {
                            viewModel.isFullScreen(!isFullScreen)
                        }
                    )
                }
            } else {
                CustomIcon(
                    icon = Icons.Default.Fullscreen,
                    contentDescription = "Fullscreen",
                    color = Color.White,
                    onClick = {
                        viewModel.isFullScreen(!isFullScreen)
                    }
                )
            }
        }
        CustomSlider(
            viewModel = viewModel,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
