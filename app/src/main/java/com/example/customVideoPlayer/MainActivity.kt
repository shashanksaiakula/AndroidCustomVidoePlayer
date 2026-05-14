package com.example.customVideoPlayer

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.arthenica.ffmpegkit.FFmpegKit
import com.example.customVideoPlayer.ui.theme.MyApplicationTheme
import com.example.transcript_engine.WhisperBridge
import com.example.transcript_engine.copyModelToStorage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.WHITE

        FFmpegKit.executeAsync("-version") { session ->

            Log.e(
                "FFMPEG",
                session.output ?: "NO OUTPUT"
            )
        }
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    myComopse(modifier = Modifier.padding(innerPadding))
//                    App(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}