package com.example.video_player_lib.presentation.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
// Slider is not used directly here; CustomSlider is used instead
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.video_player_lib.api.view.CustomSlider
import com.example.video_player_lib.domin.model.LocalVideo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomModel(
    showSheet: Boolean,
    onDismiss: () -> Unit,
    item: LocalVideo? = null,
    onRename: () -> Unit = {},
    onDelete:() -> Unit = {},
    onShare: () -> Unit = {},
    isVideo : Boolean = false,
    doubleTapSeek: (seek : Long) -> Unit = {},
    onSpeedChange : (speed : Float) -> Unit = {},
    onLongPress: (speed : Float) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState()
    var doubleSeekValue by remember { mutableStateOf(20000L) }
    var SpeekValue by remember { mutableStateOf(1.0f) }
    var longPressValue by remember { mutableStateOf(0L) }
    var showDoubleSlider by remember { mutableStateOf(false) }
    var showSpeedSlider by remember { mutableStateOf(false) }
    var showLongSlider by remember { mutableStateOf(false) }
    val list = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f, 4.0f)
    // Define your available steps
    val speedSteps = listOf("2x", "3x", "4x", "8x", "16x")
// Corresponding numerical values you want to return
    val speedValues = listOf(2f, 3f, 4f, 8f, 16f)


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
                if (!isVideo) {
                    AboutScreen(item = item)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly // Even spacing looks better for 4 items
                ) {
                    Log.e("check", "BottomModel: $doubleTapSeek",)

                    if (!isVideo) {
                        // SHARE
                        MenuActionItem(
                            icon = Icons.Default.Share,
                            label = "Share",
                            onClick = onShare
                        )

                        // RENAME
                        MenuActionItem(
                            icon = Icons.Default.DriveFileRenameOutline,
                            label = "Rename",
                            onClick = onRename
                        )

                        // DELETE
                        MenuActionItem(
                            icon = Icons.Default.Delete,
                            label = "Delete",
                            onClick = onDelete
                        )
                    } else {
                        MenuActionItem(
                            icon = Icons.AutoMirrored.Filled.Forward,
                            label = "Double Tap Seek",
                            onClick = {
                                showDoubleSlider = true
                                showLongSlider = false
                                showSpeedSlider = false
//                                doubleTapSeek(doubleSeekValue)
                            })

                        MenuActionItem(
                            icon = Icons.Default.DoubleArrow,
                            label = "Video Speed",
                            onClick = {
                                showLongSlider = false
                                showDoubleSlider = false
                                showSpeedSlider = true
                            })

                        MenuActionItem(
                            icon = Icons.Default.TouchApp,
                            label = "Long Press",
                            onClick = {
                                showLongSlider = true
                                showDoubleSlider = false
                                showSpeedSlider = false
                            }
                        )
                    }
                }
                if (showDoubleSlider) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Double Tap Seek: ${doubleSeekValue / 1000} sec",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth()
                                .padding(top = 10.dp),
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        CustomSlider(
                            modifier = Modifier.padding(horizontal = 30.dp, vertical = 8.dp),
                            currentDuration = doubleSeekValue,
                            totalDuration = 60000,
                            onValueChage = {
                                doubleSeekValue = it.toLong()
                                doubleTapSeek(doubleSeekValue)
                            },
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.5f),
                        )
                    }
                }
                if (showSpeedSlider) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Video Speed: ${SpeekValue}X",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth()
                                .padding(top = 10.dp),
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        // Decorative chips for speed selection
                        LazyRow(
                            modifier = Modifier.padding(vertical = 8.dp),
                        ) {
                            items(list) { speed ->
                                val selected = SpeekValue == speed
                                val backgroundColor =
                                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                val contentColor =
                                    if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = backgroundColor,
                                    tonalElevation = if (selected) 4.dp else 0.dp,
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp)
                                        .clickable {
                                            SpeekValue = speed
                                            onSpeedChange(SpeekValue)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${speed}x",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = contentColor
                                        )
                                    }
                                }
                            }
                        }

                    }
                }
                if (showLongSlider) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Find the current index based on longPressValue to position the slider thumb correctly
                        val currentIndex = speedValues.indexOf(longPressValue.toFloat()).coerceAtLeast(0)

                        CustomSlider(
                            modifier = Modifier.padding(horizontal = 30.dp),
                            // Current duration parameter acts as our index step now
                            currentDuration = currentIndex.toLong(),
                            // Total duration is the maximum index position (size - 1)
                            totalDuration = (speedSteps.size - 1).toLong(),
                            onValueChage = { floatIndex ->
                                val index = floatIndex.toInt().coerceIn(0, speedSteps.size - 1)
                                // Return the actual specific float speed value (e.g. 4.0f)
                                val selectedSpeed = speedValues[index]
                                longPressValue = selectedSpeed.toLong()
                                onLongPress(selectedSpeed)
                            },
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.inversePrimary,
                            // Native steps between indices = (Total Items - 2)
                            step = speedSteps.size - 2
                        )

                        // Labels Row placed directly below the CustomSlider
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 38.dp), // Slightly wider padding to align with slider thumb limits
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            speedSteps.forEachIndexed { index, label ->
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (index == currentIndex) {
                                        MaterialTheme.colorScheme.primary // Highlight selected label
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
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
    onClick: () -> Unit,
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
