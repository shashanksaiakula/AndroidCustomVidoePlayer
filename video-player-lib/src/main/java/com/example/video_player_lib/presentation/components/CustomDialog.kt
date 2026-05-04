package com.example.video_player_lib.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CustomDialog(modifier: Modifier = Modifier,
                 title : String = "",
                 content : String = "",
                 onDismiss : () -> Unit = {},
                 confirmButtonText : String = "OK",
                 onConfirm : () -> Unit = {},
                 dismissButtonText : String = "Cancel",
                 onDismissButton : () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest =  onDismiss,
        title = {Text(text = title)} ,
        text =  {Text(text = content)},
        confirmButton = {
            Button(
                onClick ={
                    onConfirm()
                }
            ){
                Text(text = confirmButtonText)
            }
        },
        dismissButton = {
            Button(
                onClick ={
                    onDismissButton()
                }
            ){
                Text(text = dismissButtonText)
            }
        }

    )
}