package com.example.customVideoPlayer

import android.Manifest
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.customVideoPlayer.utils.UriUtils
import com.example.transcript_engine.WhisperBridge
import com.example.transcript_engine.copyModelToStorage
import com.example.video_player_lib.VideoPlayerApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun myComopse(
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    val videoPlayerApi =
        remember {
            VideoPlayerApi.initialize(context)
        }

    val bridge =
        remember {
            WhisperBridge()
        }

    var uriData by remember {
        mutableStateOf<Uri?>(null)
    }

    var transcript by remember {
        mutableStateOf("")
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    // Load model ONCE
    LaunchedEffect(Unit) {

        withContext(Dispatchers.IO) {

            val modelPath =
                copyModelToStorage(context)

            val loaded =
                bridge.loadModel(modelPath)

            Log.e(
                "WHISPER_MODEL",
                loaded.toString()
            )
        }
    }

    // Process video when URI changes
    LaunchedEffect(uriData) {

        val uri = uriData ?: return@LaunchedEffect

        isLoading = true

        try {

            val realVideoPath =
                withContext(Dispatchers.IO) {

                    UriUtils.copyUriToFile(
                        context,
                        uri
                    )
                }

            Log.e(
                "WHISPER",
                "VIDEO COPIED"
            )

            val wavPath =
                AudioExtractor.extractAudio(
                    context,
                    realVideoPath
                )

            if (wavPath == null) {

                Log.e(
                    "WHISPER",
                    "AUDIO EXTRACTION FAILED"
                )

                isLoading = false

                return@LaunchedEffect
            }

            Log.e(
                "WHISPER",
                "STARTING JNI"
            )

            val result =
                withContext(Dispatchers.IO) {

                    bridge.transcribeAudio(
                        wavPath
                    )
                }

            Log.e(
                "WHISPER_RESULT",
                result
            )

            transcript = result

        } catch (e: Exception) {

            Log.e(
                "WHISPER_ERROR",
                e.stackTraceToString()
            )

        } finally {

            isLoading = false
        }
    }

    DisposableEffect(Unit) {

        onDispose {

            bridge.releaseModel()
        }
    }

    val pickVideoLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->

            if (uri != null) {

                uriData = uri
            }
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val isPartial =
                permissions[
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ] ?: false

            val isFull =
                permissions[
                    Manifest.permission.READ_MEDIA_VIDEO
                ] ?: false

            val isOld =
                permissions[
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ] ?: false

            if (
                isFull ||
                isPartial ||
                isOld
            ) {

                pickVideoLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts
                            .PickVisualMedia
                            .VideoOnly
                    )
                )
            }
        }

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        Button(
            onClick = {

                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.TIRAMISU
                ) {

                    pickVideoLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts
                                .PickVisualMedia
                                .VideoOnly
                        )
                    )

                } else {

                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    )
                }
            }
        ) {

            Text("Select Video")
        }

        if (isLoading) {

            Text(
                text = "Generating transcript..."
            )
        }

        if (transcript.isNotEmpty()) {
            Text(
                text = transcript
            )
        }

        uriData?.let { uri ->

            key(uri) {

                AndroidView(
                    factory = {

                        videoPlayerApi.getFullPlayerView(
                            uri,
                            showOverLayUI = true
                        )
                    },
                    modifier = modifier
                )
            }
        }
    }
}