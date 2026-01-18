package com.rmtm.lifelog.feature.editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
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
                    "사진 (${ui.selectedUris.size}/5)",
                    style = MaterialTheme.typography.titleMedium
                )
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
