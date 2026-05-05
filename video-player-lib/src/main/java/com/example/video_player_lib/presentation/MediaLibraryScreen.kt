package com.example.video_player_lib.presentation

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.request.CachePolicy
import com.example.video_player_lib.utils.PermissionUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.example.video_player_lib.domin.model.LocalVideo
import com.example.video_player_lib.presentation.components.BottomModel
import com.example.video_player_lib.presentation.components.CustomDialog
import com.example.video_player_lib.presentation.components.CustomListView
import com.example.video_player_lib.presentation.components.CustomTopBar
import com.example.video_player_lib.presentation.components.RenameDialog
import com.example.video_player_lib.utils.shareVideo

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MediaLibraryScreen(
    onVideoSelected: (LocalVideo) -> Unit,
    isMainList: Boolean = true,
    viewModel: VideoPickerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    var isShowDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<LocalVideo?>(null) }
    var showRename by remember { mutableStateOf(false) }


    val imageLoder = remember {
        ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }
    // delete completely
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Success! Remove from list
            viewModel.deleteVideo(selectedItem)
            selectedItem = null
        }
    }

    val permission = PermissionUtils.getVideoPermission()

    val permissionState = rememberPermissionState(permission)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedVideoId by viewModel.selectedVideoId.collectAsStateWithLifecycle()


    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            viewModel.loadVideos()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isMainList) {
            CustomTopBar(title = "Media Library")
        }
        if (permissionState.status.isGranted) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        items = uiState.videos,
                        key = { it.id },
                        contentType = { "video_item" }
                    ) { video ->
                        CustomListView(
                            item = video,
                            onItemClick = { clickedVideo ->
                                viewModel.onVideoSelected(clickedVideo.id)
                                onVideoSelected(clickedVideo)
                            },
                            imageLoader = imageLoder,
                            modifier = Modifier.background(
                                if (selectedVideoId == video.id) Color.Gray.copy(
                                    alpha = 0.2f
                                ) else Color.Transparent
                            )
                        ) {
                            selectedItem = video
                            showBottomSheet = !showBottomSheet
                        }
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text("Allow Access to Videos")
                }
            }
        }
        BottomModel(
            showSheet = showBottomSheet, onDismiss = { showBottomSheet = false },
            item = selectedItem,
            onRename = {
                showBottomSheet = false
                showRename = true
            },
            onDelete = {
                showBottomSheet = false
                // If on Android 11+ and "All Files Access" is NOT granted, send user to settings
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                    val intent =
                        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                    context.startActivity(intent)
                } else {
                    // If already granted (or older Android), just show your delete confirmation dialog
                    isShowDialog = true
                }
            },
            onShare = {
                selectedItem?.let { video ->
                    shareVideo(context, video.uri, video.name)
                }
            }
        )

        if (isShowDialog) {
            CustomDialog(
                title = "Delete Video",
                content = "Are you sure you want to delete ${selectedItem?.name} video?",
                onDismiss = { isShowDialog = false },
                confirmButtonText = "Delete",
                onConfirm = {
                    isShowDialog = false
// Execute actual delete logic
                    selectedItem?.let { video ->
                        // Check if we have "All Files Access" (MANAGE_EXTERNAL_STORAGE)
                        val hasAllFilesAccess = PermissionUtils.hasAllFilesAccess()

                        if (hasAllFilesAccess) {
                            // Delete directly - No Google dialog will show
                            try {
                                context.contentResolver.delete(video.uri, null, null)
                                viewModel.deleteVideo(video)
                                selectedItem = null
                            } catch (e: Exception) {
                                // Handle potential errors
                            }
                        } else {
                            // Fallback to the system dialog if permission isn't granted
                            val pendingIntent = MediaStore.createTrashRequest(
                                context.contentResolver,
                                listOf(video.uri),
                                true
                            )
                            launcher.launch(
                                IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                            )
                        }
                    }
                },
                dismissButtonText = "Cancel",
                onDismissButton = { isShowDialog = false }
            )

        }
        if(showRename){
            RenameDialog(
                currentName = selectedItem?.name ?: "",
                onDismiss = { showRename = false },
                onConfirm = { newName ->
                    showRename = false
                    selectedItem?.let { video ->
                        val hasAllFilesAccess = PermissionUtils.hasAllFilesAccess()
                        if (hasAllFilesAccess) {
                            try {
                                val values = ContentValues().apply {
                                    put(MediaStore.MediaColumns.DISPLAY_NAME, newName)
                                }
                                val rowsUpdated =
                                    context.contentResolver.update(video.uri, values, null, null)
                                if (rowsUpdated > 0) {
                                    // Optimistically update UI, then reload
                                    viewModel.renameVideo(video, newName)
                                    viewModel.loadVideos()  // Reload to sync with MediaStore
                                    Toast.makeText(
                                        context,
                                        "Renamed successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(context, "Rename failed", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } else {
                            Toast.makeText(context, "please grant permissions ", Toast.LENGTH_SHORT).show()

                        }
                    }
                }
            )
        }
    }
}
