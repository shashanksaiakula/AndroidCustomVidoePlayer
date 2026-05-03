package com.example.myapplication.domin.model

import android.net.Uri

data class LocalVideo(
    val id: Long,
    val name: String,
    val uri: Uri,
    val duration: Long,
    val size: Long,
    val mimeType: String? = null
)