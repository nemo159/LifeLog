package com.rmtm.lifelog.feature.timeline

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rmtm.lifelog.core.model.Entry
import com.rmtm.lifelog.core.model.Mood
import com.rmtm.lifelog.ui.AdBanner
import com.rmtm.lifelog.util.toLocalDateTimeString
import kotlinx.coroutines.flow.StateFlow

/**
 * [타임라인 화면]
 * 앱을 켰을 때 처음 나오는 목록 화면입니다.
 * - 저장된 일기들을 날짜순으로 리스트 형태로 보여줍니다.
 * - '+' 버튼을 누르면 작성 화면으로 이동합니다.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TimelineScreen(
    state: StateFlow<TimelineState>,
    onAdd: () -> Unit,
    onEntryClick: (Entry) -> Unit,
    onSortChange: (SortOrder) -> Unit,
    onDateSelect: (Int?, Int?) -> Unit,
    onSettingsClick: () -> Unit,
    onToggleHeaderExpansion: (String) -> Unit
) {
    val uiState = state.collectAsStateWithLifecycle()
    val ui = uiState.value
    var showSortMenu by remember { mutableStateOf(false) }
    var showYearMenu by remember { mutableStateOf(false) }
    var showMonthMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {},
                    actions = {
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.List,
                                    contentDescription = "정렬",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                SortOrder.entries.forEach { order ->
                                    DropdownMenuItem(
                                        text = { Text(order.label) },
                                        onClick = {
                                            onSortChange(order)
                                            showSortMenu = false
                                        },
                                        leadingIcon = if (ui.sortOrder == order) {
                                            { Icon(Icons.Default.Check, contentDescription = null) }
                                        } else null
                                    )
                                }
                            }
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "설정",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
                // 필터 바
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 연도 선택
                    Box {
                        FilterChip(
                            selected = ui.selectedYear != null,
                            onClick = { showYearMenu = true },
                            label = { Text(ui.selectedYear?.toString()?.plus("년") ?: "전체 년도") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                        )
                        DropdownMenu(
                            expanded = showYearMenu,
                            onDismissRequest = { showYearMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("전체 년도") },
                                onClick = {
                                    onDateSelect(null, null)
                                    showYearMenu = false
                                }
                            )
                            ui.availableYears.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text("${year}년") },
                                    onClick = {
                                        onDateSelect(year, null)
                                        showYearMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // 월 선택 (연도가 선택되었을 때만 표시)
                    if (ui.selectedYear != null) {
                        Box {
                            FilterChip(
                                selected = ui.selectedMonth != null,
                                onClick = { showMonthMenu = true },
                                label = { Text(ui.selectedMonth?.toString()?.plus("월") ?: "전체 월") },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                            )
                            DropdownMenu(
                                expanded = showMonthMenu,
                                onDismissRequest = { showMonthMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("전체 월") },
                                    onClick = {
                                        onDateSelect(ui.selectedYear, null)
                                        showMonthMenu = false
                                    }
                                )
                                ui.availableMonths.forEach { month ->
                                    DropdownMenuItem(
                                        text = { Text("${month}월") },
                                        onClick = {
                                            onDateSelect(ui.selectedYear, month)
                                            showMonthMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            AdBanner()
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Text("+") }
        }
    ) { padding ->
        if (ui.loading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            if (ui.entries.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp), contentAlignment = Alignment.Center
                ) {
                    Text("해당 기간에 기록이 없습니다.")
                }
            } else {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    ui.groupedEntries.forEach { (header, entries) ->
                        val isExpanded = ui.expansionState.getOrDefault(header, true)
                        stickyHeader {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleHeaderExpansion(header) }
                                    .animateContentSize(),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = header,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "rotation")
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = if (isExpanded) "접기" else "펼치기",
                                        modifier = Modifier.rotate(rotation)
                                    )
                                }
                            }
                        }
                        if (isExpanded) {
                            items(
                                items = entries,
                                key = { entry -> entry.id }
                            ) { entry ->
                                EntryCard(entry, onClick = { onEntryClick(entry) })
                            }
                        }
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
                Text(
                    entry.createdAt.toLocalDateTimeString(),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = Mood.fromValue(entry.mood).emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
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
