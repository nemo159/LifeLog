package com.rmtm.lifelog.feature.editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    state: StateFlow<EditorState>,
    onMoodChanged: (Int) -> Unit,
    onNoteChanged: (String) -> Unit,
    onPhotosSelected: (List<android.net.Uri>) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val uiState = state.collectAsStateWithLifecycle()
    val ui = uiState.value

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                onPhotosSelected(uris)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("기록 작성") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("오늘 기분은 어떠신가요? (1~5)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Slider(
                value = ui.mood.toFloat(),
                onValueChange = { onMoodChanged(it.toInt()) },
                valueRange = 1f..5f,
                steps = 3
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = ui.note,
                onValueChange = onNoteChanged,
                label = { Text("메모") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp)
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("사진 (${ui.selectedUris.size}/5)", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
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
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.size(100.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            } else {
                Text("선택된 사진이 없습니다.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            }

            Spacer(Modifier.weight(1f))

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
