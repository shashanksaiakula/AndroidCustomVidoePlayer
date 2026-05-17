package com.example.video_player_lib.presentation.components

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.media3.common.util.UnstableApi
import com.example.video_player_lib.api.view.CustomVideoPlayer
import com.example.video_player_lib.api.viewmodel.VideoPlayerViewModel
import com.example.video_player_lib.domin.model.LocalVideo
import com.example.video_player_lib.presentation.MediaLibraryScreen
import com.example.video_player_lib.presentation.VideoPickerViewModel
import com.example.video_player_lib.utils.formatTime
import com.example.video_player_lib.utils.timeStampToLong

@SuppressLint("ViewModelConstructorInComposable")
@OptIn(UnstableApi::class)
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun App(modifier: Modifier = Modifier) {
    var selectedVideo by remember { mutableStateOf<LocalVideo?>(null) }
    val viewModel: VideoPickerViewModel = hiltViewModel()
    val focusManager = LocalFocusManager.current
    var timeStamp by remember { mutableStateOf(0L) }
    var addNote by remember { mutableStateOf("") }
    val context = LocalContext.current
    val videplayViewModel : VideoPlayerViewModel = remember {
        VideoPlayerViewModel(viewModel.exoPlayer, context)
    }
    val isFullScreenEnabled = videplayViewModel.isFullScreen.collectAsState().value
    val notesList = videplayViewModel.listOfNotes.collectAsState().value
    val id = videplayViewModel.id.collectAsState().value

    LaunchedEffect(viewModel.isPressed) {
        viewModel.isPressed.collect {
            selectedVideo = null
        }
    }



    Box(modifier = modifier.fillMaxSize()) {
        if (selectedVideo == null) {
            MediaLibraryScreen(onVideoSelected = { video ->
                selectedVideo = video
            })
        } else {
            BackHandler {
                selectedVideo = null
            }
            Column {
//                ExoVideoPlayer(
//                    id = selectedVideo!!.id,
//                    uri = selectedVideo!!.uri,
//                    mimeType = selectedVideo!!.mimeType,
//                    name = selectedVideo!!.name
//                ) { isFullScreen ->
//                    isFullScreenEnabled.value = isFullScreen
//                }
                CustomVideoPlayer(
                    uri = selectedVideo!!.uri,
                    viewModel = videplayViewModel,
                )
                if (!isFullScreenEnabled) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.secondary.copy(alpha = .2f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            "Notes",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.selectTab("notes")
                                }
                                .background(
                                    color = if (viewModel.tabSelected.collectAsState().value == "notes") MaterialTheme.colorScheme.secondary.copy(
                                        alpha = .2f
                                    ) else MaterialTheme.colorScheme.secondary.copy(alpha = .0f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "List",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.selectTab("list")
                                }
                                .background(
                                    color = if (viewModel.tabSelected.collectAsState().value == "list") MaterialTheme.colorScheme.secondary.copy(
                                        alpha = .2f
                                    ) else MaterialTheme.colorScheme.secondary.copy(alpha = .0f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            textAlign = TextAlign.Center
                        )
                    }
                    if (viewModel.tabSelected.collectAsState().value == "list") {
                        MediaLibraryScreen(
                            onVideoSelected = { video ->
                                selectedVideo = video
                            }, isMainList = false
                        )
                    }
                    if (viewModel.tabSelected.collectAsState().value == "notes") {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                notesList.forEach { (id, notes) ->
                                    item {
                                        if (selectedVideo?.id == id) {
                                            notes.forEach { note ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(8.dp)
                                                        .border(
                                                            4.dp,
                                                            Color.Gray.copy(alpha = .5f),
                                                            RoundedCornerShape(4.dp)
                                                        )
                                                        .clickable {
                                                            videplayViewModel.seekTo(
                                                                timeStampToLong(
                                                                    note.substringBefore("-").trim()
                                                                )
                                                            )
//                                                            videplayViewModel.exoPlayer.seekTo(
//                                                                timeStampToLong(
//                                                                    note.substringBefore("-").trim()
//                                                                )
//                                                            )
                                                        }
                                                        .padding(10.dp), // Inner padding for the Row
                                                    horizontalArrangement = Arrangement.Start, // Changed to Start so the box is next to the text
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val part1 = note.substringBefore("-").trim()
                                                    val rest = note.substringAfter("-", "")

                                                    // 1. The Bounding Box with Curves
                                                    Surface(
                                                        color = Color.Blue.copy(alpha = 0.2f), // Your background color
                                                        shape = RoundedCornerShape(4.dp),      // This creates the "Curve"
                                                    ) {
                                                        Text(
                                                            text = part1,
                                                            color = Color.Black,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(
                                                                horizontal = 6.dp,
                                                                vertical = 2.dp
                                                            ),
                                                            style = MaterialTheme.typography.bodyMedium
                                                        )
                                                    }

                                                    // 2. The Rest of the Text
                                                    Text(
                                                        text = " $rest",
                                                        color = Color.Black,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.padding(start = 4.dp)
                                                    )
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = addNote,
                                onValueChange = { note ->
                                    addNote = note
                                },
                                prefix = {
                                    Text(
                                        text = "${formatTime(timeStamp)} - ",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                label = { Text("add note") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            timeStamp = videplayViewModel.pauseVideo()
                                        }
                                    },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        videplayViewModel.play()
                                        focusManager.clearFocus()
                                        videplayViewModel.addNote(
                                            note = "${formatTime(timeStamp)} - ${addNote}",
                                            id = id
                                        )
                                        timeStamp = 0L
                                        addNote = ""
                                    }
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
