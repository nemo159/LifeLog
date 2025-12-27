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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    state: StateFlow<TimelineState>,
    onAdd: () -> Unit
) {
    val uiState = state.collectAsStateWithLifecycle()
    val ui = uiState.value

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("일상 데이터 아카이브") })
        },
        floatingActionButton = @Composable {
            FloatingActionButton(onClick = onAdd) { Text("+") }
        }
    ) { padding ->
        if (ui.loading) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            if (ui.entries.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                    Text("아직 기록이 없습니다.\n오른쪽 아래 + 버튼으로 첫 기록을 추가해보세요.")
                }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                    // ✅ 해결: 'key'를 지정하여 올바른 items 함수를 사용하도록 유도합니다.
                    // 이렇게 하면 타입 추론 오류가 해결됩니다.
                    // 'entry.id' 부분은 Entry 모델의 고유 식별자로 변경해야 합니다.
                    // 만약 id가 없다면 'entry.dateEpochDay'와 같이 고유성이 높은 값을 사용하세요.
                    items(
                        items = ui.entries,
                        key = { entry ->
                            println("TimelineScreen: key: $entry")
                            entry.id /* 또는 entry.id */ }
                            //entry.dateEpochDay /* 또는 entry.id */ }
                    ) { entry ->
                        EntryCard(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryCard(entry: Entry) {
    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clickable(enabled = false) { }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(entry.dateEpochDay.toLocalDateString(), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text("기분: ${entry.mood} / 5")
            Spacer(Modifier.height(6.dp))
            Text(entry.note.take(120))
        }
    }
}
