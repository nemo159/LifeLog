package com.rmtm.lifelog.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmtm.lifelog.core.model.Entry
import com.rmtm.lifelog.data.repository.EntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [뷰모델: 상세 보기]
 * 특정 일기의 상세 정보를 관리합니다.
 * - 화면이 열릴 때 전달받은 ID로 DB에서 데이터를 찾아옵니다.
 * - 삭제 기능을 수행합니다.
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repo: EntryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val entryId: Long = checkNotNull(savedStateHandle["entryId"])

    val state: StateFlow<DetailState> = repo.observeEntry(entryId)
        .map { DetailState(entry = it, loading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DetailState()
        )

    fun delete(onDone: () -> Unit) {
        val entry = state.value.entry ?: return
        viewModelScope.launch {
            repo.delete(entry)
            onDone()
        }
    }
}
