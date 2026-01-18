package com.rmtm.lifelog.feature.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.rmtm.lifelog.util.toLocalDateString
import kotlinx.coroutines.flow.StateFlow

import com.rmtm.lifelog.core.model.Mood

/**
 * [상세 보기 화면]
 * 목록에서 항목을 클릭했을 때 보여주는 화면입니다.
 * - 일기의 모든 내용(날짜, 기분, 글, 사진)을 크게 보여줍니다.
 * - 삭제 버튼을 통해 기록을 지울 수 있습니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    state: StateFlow<DetailState>,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    val uiState = state.collectAsStateWithLifecycle()
    val ui = uiState.value

    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("기록 상세") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { padding ->
        if (ui.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (ui.entry == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("기록을 찾을 수 없습니다.")
            }
        } else {
            val entry = ui.entry
            val mood = Mood.fromValue(entry.mood)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = entry.dateEpochDay.toLocalDateString(),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${mood.emoji} ${mood.label}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = entry.note,
                    style = MaterialTheme.typography.bodyLarge
                )

                if (entry.photos.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    Text("사진", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(entry.photos) { photo ->
                            Card {
                                AsyncImage(
                                    model = photo.uri,
                                    contentDescription = null,
                                    modifier = Modifier.size(250.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("기록 삭제") },
            text = { Text("정말 이 기록을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}
