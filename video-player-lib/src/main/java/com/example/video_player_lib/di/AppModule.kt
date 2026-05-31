package com.example.video_player_lib.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.RenderersFactory
import com.example.video_player_lib.domin.repository.DictionaryRepository
import com.example.video_player_lib.domin.repository.VideoRepository
import com.example.video_player_lib.networkcall.DictionaryApi
import com.example.video_player_lib.repository.DictionaryRepositoryImpl
import com.example.video_player_lib.repository.VideoRepositoryImpl
import com.example.video_player_lib.utils.ExoPlayerUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideVideoRepository(
        @ApplicationContext context: Context
    ): VideoRepository {
        return VideoRepositoryImpl(context)
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideRenderersFactory(@ApplicationContext context: Context): RenderersFactory {
        return DefaultRenderersFactory(context).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            setEnableDecoderFallback(true)
        }
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideLoadControl(): LoadControl {
        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                2500, // Min buffer to start
                10000, // Max buffer
                1000, // Buffer for playback
                1500  // Buffer after re-buffer
            ).build()
    }

    @OptIn(UnstableApi::class)
    @Provides
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        renderersFactory: RenderersFactory,
        loadControl: LoadControl
    ): ExoPlayer {
        return ExoPlayerUtils.createExoPlayerWithDeps(context, renderersFactory, loadControl)
    }


    @Provides
    @Singleton
    fun provideDictionaryApi(): DictionaryApi {
        return Retrofit.Builder()
            .baseUrl("https://api.dictionaryapi.dev/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DictionaryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDictionaryRepository(api: DictionaryApi): DictionaryRepository {
        return DictionaryRepositoryImpl(api)
    }
}
