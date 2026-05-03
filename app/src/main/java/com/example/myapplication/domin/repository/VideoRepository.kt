package com.example.myapplication.domin.repository

import com.example.myapplication.domin.model.LocalVideo

interface VideoRepository {
    suspend fun getLocalVideos(): List<LocalVideo>
}