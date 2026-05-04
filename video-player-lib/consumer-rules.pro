# Add project specific consumer ProGuard rules here.
# These rules will be applied to the app that depends on this library.

# Hilt/Dagger specific rules (though usually handled by their own library)
-keep class com.example.video_player_lib.di.** { *; }

# Media3/ExoPlayer might need specific keeps if they aren't already included in their own consumer rules
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.common.** { *; }
