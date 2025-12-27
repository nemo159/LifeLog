package com.rmtm.lifelog.feature.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmtm.lifelog.core.model.Entry
import com.rmtm.lifelog.core.model.Photo
import com.rmtm.lifelog.data.repository.EntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * [EditorState]
 * - 작성 화면에서 입력받는 값들을 상태로 관리합니다.
 * - 사진(photos)은 구조만 미리 준비해 둡니다.
 *   (target 36 정책 대응을 위해 권한 없이 Photo Picker로 붙이는 방향이 안전)
 */
data class EditorState(
    val date: LocalDate = LocalDate.now(),
    val mood: Int = 3,
    val note: String = "",
    val photos: List<Photo> = emptyList(),
    val saving: Boolean = false
)

/**
 * [EditorViewModel]
 * - 사용자 입력을 받아 Entry를 생성하고 DB에 저장합니다.
 * - 저장 완료 시 콜백(onDone)으로 화면을 닫는 흐름을 구성합니다.
 */
@HiltViewModel
class EditorViewModel @Inject constructor(
    private val repo: EntryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    /**
     * 저장 처리
     * - MVP에서는 새 기록 추가만 지원합니다.
     * - 추후 "수정"을 넣으려면 id를 포함해 upsert 하면 됩니다.
     */
    fun save(input: EditorState, onDone: () -> Unit) {
        viewModelScope.launch {
            _state.value = input.copy(saving = true)

            val now = System.currentTimeMillis()

            val entry = Entry(
                dateEpochDay = input.date.toEpochDay(),
                mood = input.mood.coerceIn(1, 5),
                note = input.note,
                createdAt = now,
                updatedAt = now
            )

            repo.upsert(entry, input.photos)
            onDone()
        }
    }
}
