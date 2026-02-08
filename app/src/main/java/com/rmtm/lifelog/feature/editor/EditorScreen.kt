package com.rmtm.lifelog.feature.editor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.rmtm.lifelog.core.model.Mood
import kotlinx.coroutines.flow.StateFlow

/**
 * [작성/편집 화면]
 * 일기를 새로 쓰거나 수정하는 화면입니다.
 * - 기분(1~5점), 내용, 사진을 입력받습니다.
 * - 사진 선택기를 통해 갤러리에서 사진을 가져옵니다.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    state: StateFlow<EditorState>,
    onMoodChanged: (Mood) -> Unit,
    onNoteChanged: (String) -> Unit,
    onPhotosSelected: (List<Uri>) -> Unit,
    onPhotoRemoved: (Uri) -> Unit,
    getTmpFileUri: () -> Uri,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val uiState = state.collectAsStateWithLifecycle()
    val ui = uiState.value
    val context = LocalContext.current
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var showCameraPermissionRationaleDialog by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }


    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                onPhotosSelected(uris)
            }
        }
    )

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempImageUri?.let { uri ->
                    onPhotosSelected(listOf(uri))
                }
            }
        }
    )

    if (showPhotoSourceDialog) {
        PhotoSourceDialog(
            onDismiss = { showPhotoSourceDialog = false },
            onGalleryClick = {
                showPhotoSourceDialog = false
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onCameraClick = {
                showPhotoSourceDialog = false
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    val uri = getTmpFileUri()
                    tempImageUri = uri
                    takePictureLauncher.launch(uri)
                } else {
                    showCameraPermissionRationaleDialog = true
                }
            }
        )
    }

    if (showCameraPermissionRationaleDialog) {
        CameraPermissionRationaleDialog(
            onDismiss = { showCameraPermissionRationaleDialog = false },
            onConfirm = {
                showCameraPermissionRationaleDialog = false
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("오늘 기분은 어떠신가요?", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Mood.entries.forEach { mood ->
                    FilterChip(
                        selected = ui.mood == mood,
                        onClick = { onMoodChanged(mood) },
                        label = { Text("${mood.emoji} ${mood.label}") },
                        leadingIcon = if (ui.mood == mood) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = ui.note,
                onValueChange = onNoteChanged,
                label = { Text("메모") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "사진",
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = {
                    showPhotoSourceDialog = true
                }) {
                    Text("사진 선택")
                }
            }

            Spacer(Modifier.height(8.dp))

            if (ui.selectedUris.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(100.dp)
                ) {
                    items(ui.selectedUris) { uri ->
                        Card {
                            Box(modifier = Modifier.size(100.dp)) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(), // Fill the Box size
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { onPhotoRemoved(uri) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(24.dp) // Smaller size for the close button
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "사진 삭제",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant // Use a color that stands out
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    "선택된 사진이 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = !ui.saving
                ) {
                    Text(if (ui.saving) "저장 중..." else "저장")
                }

                Spacer(Modifier.width(12.dp))

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    enabled = !ui.saving
                ) {
                    Text("취소")
                }
            }
        }
    }
}

@Composable
private fun CameraPermissionRationaleDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("권한 필요") },
        text = { Text("카메라 권한을 허용해야 이용할 수 있습니다. 설정으로 이동하시겠습니까?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("설정으로 이동")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
private fun PhotoSourceDialog(
    onDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("사진 추가") },
        text = {
            Column {
                Text(
                    text = "사진첩",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onGalleryClick)
                        .padding(vertical = 12.dp)
                )
                Text(
                    text = "카메라",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onCameraClick)
                        .padding(vertical = 12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
