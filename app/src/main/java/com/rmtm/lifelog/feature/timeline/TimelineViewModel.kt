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

/**
 * [TimelineState]
 * - 타임라인 화면이 필요로 하는 상태 데이터입니다.
 */
data class TimelineState(
    val loading: Boolean = true,
    val entries: List<Entry> = emptyList()
)

/**
 * [TimelineViewModel]
 * - 기록 목록을 관찰(Flow)하고, UI에 상태로 제공합니다.
 * - DB 변경 시 자동으로 목록이 갱신됩니다.
 */
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repo: EntryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TimelineState())
    val state: StateFlow<TimelineState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeEntries().collect { list ->
                _state.value = TimelineState(
                    loading = false,
                    entries = list
                )
            }
        }
    }
}
