package com.example.video_player_lib.domin.repository

import com.example.video_player_lib.domin.model.WordDefinition

interface DictionaryRepository {
    suspend fun getDefinition(word: String): Result<WordDefinition>
}