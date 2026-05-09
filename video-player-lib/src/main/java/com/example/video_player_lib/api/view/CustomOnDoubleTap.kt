package com.example.video_player_lib.api.view

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
@Composable
fun CustomOnDouble(
    modifier: Modifier = Modifier,
    visible: Boolean,
    icon: ImageVector,
    isFarword: Boolean = true,
    seek: Long = 5000L,
    onClick : () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier // Apply alignment modifier here
    ) {
        // This is the circular indicator in the middle of the side
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.White.copy(0.2f), CircleShape)
                .clickable{
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.size(100.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,) {
               CustomIcon(
                   icon = icon,
                   modifier = Modifier.size(30.dp)
                       .then(if(isFarword) Modifier.rotate(-180f).graphicsLayer(scaleY = -1f)  else Modifier),
                   contentDescription = "farword/reverse",
               ) {
                   onClick()
               }
                Text(
                    text = "${if (isFarword) "+" else "-"}${seek / 1000}s",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}