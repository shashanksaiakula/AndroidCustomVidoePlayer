package com.example.video_player_lib.api.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun CustomIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String,
    color: Color = Color.White,
    size : Int = 24,
    onClick: () -> Unit
) {
    IconButton(
        onClick = {
            onClick()
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = color,
            modifier = modifier
                .size(size.dp)
                .background(Color.LightGray.copy(alpha = 0.5f), shape = androidx.compose.foundation.shape.CircleShape)

        )
    }
}