package com.rmtm.lifelog.feature.editor

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmtm.lifelog.core.model.Entry
import com.rmtm.lifelog.core.model.Photo
import com.rmtm.lifelog.data.repository.EntryRepository
import com.rmtm.lifelog.util.ImageStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

import com.rmtm.lifelog.core.model.Mood

/**
 * [EditorState]
 * - 작성 화면에서 입력받는 값들을 상태로 관리합니다.
 */
data class EditorState(
    val date: LocalDate = LocalDate.now(),
    val mood: Mood = Mood.CALM,
    val note: String = "",
    val selectedUris: List<Uri> = emptyList(),
    val saving: Boolean = false
)

/**
 * [뷰모델: 작성/편집]
 * 작성 화면의 입력 데이터(날짜, 기분, 내용, 사진)를 관리합니다.
 * - 사용자가 '저장'을 누르면 데이터를 DB에 저장하고 파일을 관리합니다.
 */
@HiltViewModel
class EditorViewModel @Inject constructor(
    private val repo: EntryRepository,
    private val imageStorageManager: ImageStorageManager
) : ViewModel() {

    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    fun onMoodChanged(mood: Mood) {
        _state.value = _state.value.copy(mood = mood)
    }

    fun onNoteChanged(note: String) {
        _state.value = _state.value.copy(note = note)
    }

    fun onPhotosSelected(uris: List<Uri>) {
        _state.value = _state.value.copy(selectedUris = uris)
    }

    /**
     * 저장 처리
     * - 선택된 사진들을 내부 저장소로 복사한 후 DB에 저장합니다.
     */
    fun save(onDone: () -> Unit) {
        val currentState = _state.value
        viewModelScope.launch {
            _state.value = currentState.copy(saving = true)

            // 1. 사진들을 내부 저장소로 복사
            val photos = currentState.selectedUris.mapNotNull { uri ->
                val localPath = imageStorageManager.saveImageToInternalStorage(uri)
                if (localPath != null) {
                    Photo(entryId = 0L, uri = localPath)
                } else null
            }

            // 2. Entry 및 Photo 정보 저장
            val now = System.currentTimeMillis()
            val entry = Entry(
                dateEpochDay = currentState.date.toEpochDay(),
                mood = currentState.mood.value,
                note = currentState.note,
                createdAt = now,
                updatedAt = now
            )

            repo.upsert(entry, photos)
            onDone()
        }
    }
}
