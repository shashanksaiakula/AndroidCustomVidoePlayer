package com.example.customVideoPlayer

import android.net.Uri
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.video_player_lib.VideoPlayerApi

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun myComopse(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // ✅ Use remember so it doesn't re-init on every UI change
    val videoPlayerApi = remember { VideoPlayerApi.initialize(context) }
    val uri = Uri.parse("")

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Use AndroidView to display the PlayerView
        AndroidView(
            factory = { ctx ->
                videoPlayerApi.getPlayerView() as View
            },
            modifier = Modifier.weight(1f) // Takes up available space
        )

        // 2. Use LaunchedEffect so 'prepare' only runs ONCE
        LaunchedEffect(uri) {
            videoPlayerApi.prepare(
                uri = uri,
                mimeType = "video/mp4"
            )
        }
    }
}

