package com.rmtm.lifelog.feature.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    state: StateFlow<EditorState>,
    onSave: (EditorState) -> Unit,
    onCancel: () -> Unit
) {
    val uiState = state.collectAsStateWithLifecycle()
    val ui = uiState.value

    var mood by remember(ui.mood) { mutableIntStateOf(ui.mood) }
    var note by remember(ui.note) { mutableStateOf(ui.note) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("새 기록 작성") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("기분(1~5)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Slider(
                value = mood.toFloat(),
                onValueChange = { mood = it.toInt().coerceIn(1, 5) },
                valueRange = 1f..5f,
                steps = 3
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("메모") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp)
            )

            Spacer(Modifier.height(16.dp))
            Text("사진: ${ui.photos.size}장 (다음 단계에서 Photo Picker로 추가)")

            Spacer(Modifier.height(24.dp))
            Row {
                Button(
                    onClick = { onSave(ui.copy(mood = mood, note = note)) },
                    enabled = !ui.saving
                ) {
                    Text(if (ui.saving) "저장 중..." else "저장")
                }

                Spacer(Modifier.width(12.dp))

                OutlinedButton(onClick = onCancel, enabled = !ui.saving) {
                    Text("취소")
                }
            }
        }
    }
}
