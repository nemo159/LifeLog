package com.rmtm.lifelog.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmtm.lifelog.core.model.Entry
import com.rmtm.lifelog.data.repository.EntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOrder {
    DESC, ASC
}

/**
 * [TimelineState]
 * - 타임라인 화면이 필요로 하는 상태 데이터입니다.
 */
data class TimelineState(
    val loading: Boolean = true,
    val entries: List<Entry> = emptyList(),
    val sortOrder: SortOrder = SortOrder.DESC
)

/**
 * [뷰모델: 타임라인]
 * 타임라인 화면의 데이터와 상태를 관리합니다.
 * - 저장소에서 일기 목록을 가져와서 화면에 전달합니다.
 * - 최신순/과거순 정렬 기능을 처리합니다.
 */
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repo: EntryRepository
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(SortOrder.DESC)
    private val _state = MutableStateFlow(TimelineState())
    val state: StateFlow<TimelineState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeEntries().collect { list ->
                updateState(list, _sortOrder.value)
            }
        }
    }

    fun toggleSortOrder() {
        val newOrder = if (_sortOrder.value == SortOrder.DESC) SortOrder.ASC else SortOrder.DESC
        _sortOrder.value = newOrder
        updateState(_state.value.entries, newOrder)
    }

    private fun updateState(entries: List<Entry>, sortOrder: SortOrder) {
        val sortedList = if (sortOrder == SortOrder.DESC) {
            entries.sortedByDescending { it.dateEpochDay }
        } else {
            entries.sortedBy { it.dateEpochDay }
        }
        _state.value = _state.value.copy(
            loading = false,
            entries = sortedList,
            sortOrder = sortOrder
        )
    }
}
