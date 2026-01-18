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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class SortOrder(val label: String) {
    DATE_DESC("최신순"),
    DATE_ASC("과거순")
}

/**
 * [TimelineState]
 * - 타임라인 화면이 필요로 하는 상태 데이터입니다.
 */
data class TimelineState(
    val loading: Boolean = true,
    val entries: List<Entry> = emptyList(),
    val groupedEntries: Map<String, List<Entry>> = emptyMap(),
    val sortOrder: SortOrder = SortOrder.DATE_DESC,
    val availableYears: List<Int> = emptyList(),
    val availableMonths: List<Int> = emptyList(),
    val selectedYear: Int? = null,
    val selectedMonth: Int? = null
)

/**
 * [뷰모델: 타임라인]
 * 타임라인 화면의 데이터와 상태를 관리합니다.
 * - 저장소에서 일기 목록을 가져와서 화면에 전달합니다.
 * - 연도/월 필터링 및 최신순/과거순 정렬 기능을 처리합니다.
 */
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repo: EntryRepository
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    private val _selectedYear = MutableStateFlow<Int?>(null)
    private val _selectedMonth = MutableStateFlow<Int?>(null)
    
    private val _state = MutableStateFlow(TimelineState())
    val state: StateFlow<TimelineState> = _state.asStateFlow()

    private var currentEntries: List<Entry> = emptyList()

    init {
        viewModelScope.launch {
            repo.observeEntries().collect { list ->
                currentEntries = list
                updateState()
            }
        }
    }

    fun onSortOrderChanged(newOrder: SortOrder) {
        _sortOrder.value = newOrder
        updateState()
    }

    fun selectDate(year: Int?, month: Int?) {
        _selectedYear.value = year
        _selectedMonth.value = month
        updateState()
    }

    private fun updateState() {
        val sortOrder = _sortOrder.value
        val year = _selectedYear.value
        val month = _selectedMonth.value
        val allEntries = currentEntries

        // 1. 전체 데이터에서 연도 목록 추출 (내림차순)
        val years = allEntries.map { LocalDate.ofEpochDay(it.dateEpochDay).year }
            .distinct()
            .sortedDescending()

        // 2. 선택된 연도에 해당하는 월 목록 추출 (오름차순)
        val months = if (year != null) {
            allEntries.filter { LocalDate.ofEpochDay(it.dateEpochDay).year == year }
                .map { LocalDate.ofEpochDay(it.dateEpochDay).monthValue }
                .distinct()
                .sorted()
        } else {
            emptyList()
        }

        // 3. 필터링
        val filtered = allEntries.filter { entry ->
            val date = LocalDate.ofEpochDay(entry.dateEpochDay)
            val yearMatch = year == null || date.year == year
            val monthMatch = month == null || date.monthValue == month
            yearMatch && monthMatch
        }

        // 4. 정렬
        val sortedList = if (sortOrder == SortOrder.DATE_DESC) {
            filtered.sortedByDescending { it.dateEpochDay }
        } else {
            filtered.sortedBy { it.dateEpochDay }
        }

        // 5. 그룹화
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월")
        val grouped = sortedList.groupBy { entry ->
            LocalDate.ofEpochDay(entry.dateEpochDay).format(formatter)
        }

        _state.value = _state.value.copy(
            loading = false,
            entries = sortedList,
            groupedEntries = grouped,
            sortOrder = sortOrder,
            availableYears = years,
            availableMonths = months,
            selectedYear = year,
            selectedMonth = month
        )
    }
}
