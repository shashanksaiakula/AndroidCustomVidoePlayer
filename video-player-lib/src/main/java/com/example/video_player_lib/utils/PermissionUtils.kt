package com.example.video_player_lib.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

object PermissionUtils {
    // Method 1: Get permission based on Android version
    fun getVideoPermissionString(): String {
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

    fun getVideoPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            getVideoPermissionString()
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun getMediaPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            val intent =
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                    Log.e("check", "getMediaPermission: $data")
                }
            context.startActivity(intent).apply {
                Log.e("check", "getMediaPermission: activrty", )
            }
        }
        return Environment.isExternalStorageManager()
    }
}