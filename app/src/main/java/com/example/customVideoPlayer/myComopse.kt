package com.example.customVideoPlayer

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var uriData by remember { mutableStateOf(Uri.parse("content://media/external/video/media/1000000515")) }

    // Launcher for picking a video
    val pickVideoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            uriData = uri
        }
    }

    // Launcher for permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isPartial = permissions[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] ?: false
        val isFull = permissions[Manifest.permission.READ_MEDIA_VIDEO] ?: false
        val isOld = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false

        if (isFull || isPartial || isOld) {
            pickVideoLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Photo Picker doesn't strictly need permissions on T+, but we can launch it directly
                pickVideoLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            } else {
                permissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            }
        }) {
            Text(text = "Select Video")
        }

//        // 1. Use AndroidView to display the PlayerView
//        AndroidView(
//            factory = { ctx ->
//                videoPlayerApi.getPlayerView()
//            },
//            modifier = Modifier.weight(1f) // Takes up available space
//        )
//
//        // 2. Use LaunchedEffect so 'prepare' only runs ONCE
//        LaunchedEffect(uriData) {
//            // videoPlayerApi.prepare(
//            //    uri = uriData,
//            //    mimeType = "video/mp4"
//            // )
//        }

        // Use key to recreate AndroidView when uriData changes, ensuring factory is called again
        key(uriData) {
            AndroidView(
                factory = { ctx ->
                    // Use the Utility to get the View
                    videoPlayerApi.getFullPlayerView(uriData, showOverLayUI = true)
                },
                modifier = modifier
            )
        }
    }
}
