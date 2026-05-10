package com.example.video_player_lib.utils

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.annotation.OptIn
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.ui.PlayerView
import com.example.video_player_lib.api.view.CustomVideoPlayer
import com.example.video_player_lib.api.viewmodel.VideoPlayerViewModel

@OptIn(UnstableApi::class)
object ExoPlayerUtils {
    // For API (standalone)
    fun createExoPlayer(context: Context): ExoPlayer {
        val renderersFactory = DefaultRenderersFactory(context).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            setEnableDecoderFallback(true)
        }
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(2500, 10000, 1000, 1500)
            .build()
        return createExoPlayerWithDeps(context, renderersFactory, loadControl)
    }

    // Shared logic (used by module)
    fun createExoPlayerWithDeps(
        context: Context,
        renderersFactory: RenderersFactory,
        loadControl: LoadControl
    ): ExoPlayer {
        return ExoPlayer.Builder(context, renderersFactory)
            .setSeekParameters(SeekParameters.EXACT)
            .setLoadControl(loadControl)
            .build()
            .apply {
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            }
    }
    fun getPlayerView(context: Context, exoPlayer: ExoPlayer?) : View {
        return PlayerView(context).apply {
            player = exoPlayer
            useController = false
        }

    }
    fun getFullPlayerView(context: Context, viewModel: VideoPlayerViewModel, uri: Uri?,showOverLayUI: Boolean) : View {
        return ComposeView(context).apply {
            // This ensures the composition is disposed of when the view is removed from the screen
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                uri?.let {
                    CustomVideoPlayer(uri = it, viewModel =viewModel, showOverLayUI = showOverLayUI)
                }
            }
        }
    }
}