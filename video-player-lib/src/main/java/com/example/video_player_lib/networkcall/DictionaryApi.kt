package com.example.video_player_lib.networkcall

import androidx.compose.ui.graphics.Path
import retrofit2.http.GET

interface DictionaryApi {
    @GET("entries/en/{word}")
    suspend fun fetchWord(@retrofit2.http.Path("word") word: String): List<DictionaryResponseDto>
}

// DTOs to match the API structure
data class DictionaryResponseDto(
    val word: String,
    val meanings: List<MeaningDto>
)

data class MeaningDto(
    val definitions: List<DefinitionDto>
)

data class DefinitionDto(
    val definition: String
)