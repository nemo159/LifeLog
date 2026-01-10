package com.rmtm.lifelog.feature.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rmtm.lifelog.core.model.Entry
import com.rmtm.lifelog.util.toLocalDateString
import kotlinx.coroutines.flow.StateFlow

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow

/**
 * [타임라인 화면]
 * 앱을 켰을 때 처음 나오는 목록 화면입니다.
 * - 저장된 일기들을 날짜순으로 리스트 형태로 보여줍니다.
 * - '+' 버튼을 누르면 작성 화면으로 이동합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    state: StateFlow<TimelineState>,
    onAdd: () -> Unit,
    onEntryClick: (Entry) -> Unit,
    onToggleSort: () -> Unit
) {
    val uiState = state.collectAsStateWithLifecycle()
    val ui = uiState.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LifeLog") },
                actions = {
                    IconButton(onClick = onToggleSort) {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = "정렬",
                            tint = if (ui.sortOrder == SortOrder.ASC) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Text("+") }
        }
    ) { padding ->
        if (ui.loading) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            if (ui.entries.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("아직 기록이 없습니다.\n오른쪽 아래 + 버튼으로 첫 기록을 추가해보세요.")
                }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                    items(
                        items = ui.entries,
                        key = { entry -> entry.id }
                    ) { entry ->
                        EntryCard(entry, onClick = { onEntryClick(entry) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryCard(entry: Entry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(entry.dateEpochDay.toLocalDateString(), style = MaterialTheme.typography.titleMedium)
                Text("기분: ${entry.mood}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = entry.note,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
