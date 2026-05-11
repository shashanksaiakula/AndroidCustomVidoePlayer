package com.example.video_player_lib.utils

import android.content.Context
import android.content.Intent

fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

fun formatTime(seconds: Double): String {
    val mins = (seconds / 60).toInt()
    val secs = (seconds % 60).toInt()
    val millis = ((seconds % 1) * 100).toInt()
    return String.format("%02d:%02d.%02d", mins, secs, millis)
}

fun timeStampToLong(timeStamp: String): Long {
    return try {
        // Split by colon OR dot to be safe
        val parts = timeStamp.split(":", ".")

        when (parts.size) {
            4 -> { // HH:mm:ss:ms
                val (h, m, s, ms) = parts.map { it.toLong() }
                ((h * 3600 + m * 60 + s) * 1000) + ms
            }
            3 -> { // mm:ss:ms
                val (m, s, ms) = parts.map { it.toLong() }
                ((m * 60 + s) * 1000) + ms
            }
            2 -> { // mm:ss
                val (m, s) = parts.map { it.toLong() }
                (m * 60 + s) * 1000
            }
            else -> 0L
        }
    } catch (e: Exception) {
        0L // Return 0 if the string is formatted badly
    }
}

fun formatFileSize(size: Long): String {
    val kb = size / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0

    return when {
        gb >= 1 -> "%.2f GB".format(gb)
        mb >= 1 -> "%.2f MB".format(mb)
        kb >= 1 -> "%.2f KB".format(kb)
        else -> "$size Bytes"
    }
}

fun shareVideo(context: Context, videoUri: android.net.Uri, videoName: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "video/*" // Tells Android we are sharing a video
        putExtra(Intent.EXTRA_STREAM, videoUri)
        putExtra(Intent.EXTRA_SUBJECT, videoName)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Important for file access
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Video via"))
}
