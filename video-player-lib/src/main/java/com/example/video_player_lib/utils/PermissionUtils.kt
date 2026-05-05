package com.example.video_player_lib.utils

import android.Manifest
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi

object PermissionUtils {
    // Method 1: Get permission based on Android version
    fun getVideoPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    // Method 2: Check if has all files access permission
    @RequiresApi(Build.VERSION_CODES.R)
    fun hasAllFilesAccess(): Boolean {
        return Environment.isExternalStorageManager()
    }
}