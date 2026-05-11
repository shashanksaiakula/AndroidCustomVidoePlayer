package com.example.video_player_lib.di

import android.content.Context
import com.example.video_player_lib.presentation.transcript.TranscriptManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
object TranscriptModule {
    @Provides
    fun provideTranscriptManager(
        @ApplicationContext context: Context
    ): TranscriptManager = TranscriptManager(context)
}