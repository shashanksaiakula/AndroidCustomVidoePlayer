package com.example.video_player_lib.domin.repository

import com.example.video_player_lib.domin.model.LocalVideo

interface VideoRepository {
    suspend fun getLocalVideos(): List<LocalVideo>
}