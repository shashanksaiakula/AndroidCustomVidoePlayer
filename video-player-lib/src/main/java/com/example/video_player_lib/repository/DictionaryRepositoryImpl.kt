package com.example.video_player_lib.repository

import com.example.video_player_lib.domin.model.WordDefinition
import com.example.video_player_lib.domin.repository.DictionaryRepository
import com.example.video_player_lib.networkcall.DictionaryApi

class DictionaryRepositoryImpl(private val api: DictionaryApi) : DictionaryRepository {
    override suspend fun getDefinition(word: String): Result<WordDefinition> {
        return try {
            val response = api.fetchWord(word).first()
            val domainModel = WordDefinition(
                word = response.word,
                definition = response.meanings.firstOrNull()?.definitions?.firstOrNull()?.definition
            )
            Result.success(domainModel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}