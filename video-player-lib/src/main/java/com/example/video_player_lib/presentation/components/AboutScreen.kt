package com.example.video_player_lib.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.video_player_lib.domin.model.LocalVideo
import com.example.video_player_lib.utils.formatFileSize
import com.example.video_player_lib.utils.formatTime

@Composable
fun AboutScreen(modifier: Modifier = Modifier,item : LocalVideo?) {
    Column (modifier = modifier){
        Text(text = "Title : ${item?.name}")
        Text(text = "Duration : ${formatTime(item?.duration ?: 0L)}")
        Text(text = "Size : ${formatFileSize(item?.size ?: 0L)}")
//        Text(text = "Mime Type : ${item?.mimeType}")
    }
}