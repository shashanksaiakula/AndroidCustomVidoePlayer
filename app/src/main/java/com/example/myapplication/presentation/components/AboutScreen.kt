package com.example.myapplication.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.domin.model.LocalVideo
import com.example.myapplication.utils.formatFileSize
import com.example.myapplication.utils.formatTime

@Composable
fun AboutScreen(modifier: Modifier = Modifier,item : LocalVideo?) {
    Column (modifier = modifier){
        Text(text = "Title : ${item?.name}")
        Text(text = "Duration : ${formatTime(item?.duration ?: 0L)}")
        Text(text = "Size : ${formatFileSize(item?.size ?: 0L)}")
//        Text(text = "Mime Type : ${item?.mimeType}")
    }
}