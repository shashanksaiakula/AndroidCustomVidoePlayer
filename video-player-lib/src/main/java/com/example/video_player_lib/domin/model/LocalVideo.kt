package com.example.video_player_lib.domin.model

import android.net.Uri

data class LocalVideo(
    val id: Long,
    val name: String,
    val uri: Uri,
    val duration: Long,
    val size: Long,
    val mimeType: String? = null
)