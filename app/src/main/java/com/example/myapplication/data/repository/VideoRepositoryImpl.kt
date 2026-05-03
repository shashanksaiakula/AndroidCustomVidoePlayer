package com.example.myapplication.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.myapplication.domin.model.LocalVideo
import com.example.myapplication.domin.repository.VideoRepository

class VideoRepositoryImpl(private val context: Context) : VideoRepository{
    override suspend fun getLocalVideos(): List<LocalVideo> {
        val videos = mutableListOf<LocalVideo>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.MIME_TYPE
        )

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val mimeTypeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                videos.add(
                    LocalVideo(
                        id = id,
                        name = cursor.getString(nameCol),
                        uri = uri,
                        duration = cursor.getLong(durationCol),
                        size = cursor.getLong(sizeCol),
                        mimeType = cursor.getString(mimeTypeCol)
                    )
                )
            }
        }
        return videos
    }
}