package com.example.myapplication.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.SeekParameters
import com.example.myapplication.data.repository.VideoRepositoryImpl
import com.example.myapplication.domin.repository.VideoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
        return ExoPlayer.Builder(context, renderersFactory)
            .setSeekParameters(SeekParameters.EXACT)
            .setLoadControl(loadControl)
            .build().apply {
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            }
    }
}
