package com.example.myapplication.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.myapplication.domin.model.LocalVideo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomModel(
    showSheet: Boolean,
    onDismiss: () -> Unit,
    item: LocalVideo? = null,
    onRename: () -> Unit,
    onDelete:() -> Unit,
    onShare: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .padding(bottom = 32.dp, start = 16.dp, end = 16.dp, top = 8.dp)
                    .fillMaxWidth()
            ) {
                AboutScreen(item = item)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly // Even spacing looks better for 4 items
                ) {

                    // SHARE
                    MenuActionItem(icon = Icons.Default.Share, label = "Share", onClick = onShare)

                    // RENAME
                    MenuActionItem(icon = Icons.Default.DriveFileRenameOutline, label = "Rename", onClick = onRename)

                    // DELETE
                    MenuActionItem(icon = Icons.Default.Delete, label = "Delete", onClick = onDelete)
                }
            }
        }
    }
}

// Helper component to keep the Row code clean
@Composable
fun MenuActionItem(
    icon: ImageVector ,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp)
        )
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
