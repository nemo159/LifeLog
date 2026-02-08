package com.rmtm.lifelog.feature.editor

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmtm.lifelog.core.model.Entry
import com.rmtm.lifelog.core.model.Photo
import com.rmtm.lifelog.data.repository.EntryRepository
import com.rmtm.lifelog.util.ImageStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import android.content.Context

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
    private val imageStorageManager: ImageStorageManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _currentEntryId = MutableStateFlow<Long?>(null)
    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    fun onMoodChanged(mood: Mood) {
        _state.value = _state.value.copy(mood = mood)
    }

    fun onNoteChanged(note: String) {
        _state.value = _state.value.copy(note = note)
    }

    fun onPhotosSelected(newUris: List<Uri>) {
        val currentSelectedUris = _state.value.selectedUris
        val combinedUris = (currentSelectedUris + newUris).distinct()
        _state.value = _state.value.copy(selectedUris = combinedUris)
    }

    fun loadEntryForEdit(entryId: Long) {
        viewModelScope.launch {
            val entry = repo.getEntryById(entryId)
            entry?.let {
                _currentEntryId.value = it.id
                _state.value = _state.value.copy(
                    date = LocalDate.ofEpochDay(it.dateEpochDay),
                    mood = Mood.fromValue(it.mood),
                    note = it.note,
                    selectedUris = it.photos.map { photo -> Uri.parse("file://" + photo.uri) }
                )
            }
        }
    }

    fun onPhotoRemoved(uri: Uri) {
        _state.value = _state.value.copy(
            selectedUris = _state.value.selectedUris.filter { it != uri }
        )
    }

    fun getTmpFileUri(): Uri {
        return imageStorageManager.getTmpFileUri()
    }

    /**
     * 저장 처리
     * - 선택된 사진들을 내부 저장소로 복사한 후 DB에 저장합니다.
     */
    fun save(onDone: () -> Unit) {
        val currentState = _state.value
        viewModelScope.launch {
            _state.value = currentState.copy(saving = true)

            // Get original entry if exists for createdAt
            val originalEntry = _currentEntryId.value?.let { repo.getEntryById(it) }

            // 1. 사진들을 처리 (새로운 사진은 내부 저장소로 복사, 기존 사진은 경로 재사용)
            val processedPhotos = mutableListOf<Photo>()
            val internalFilesDirPath = context.filesDir.absolutePath

            for (uri in currentState.selectedUris) {
                // An existing internal photo would have scheme "file" and its path starting with internalFilesDirPath
                val isAlreadyInternalFile = uri.scheme == "file" && uri.path?.startsWith(internalFilesDirPath) == true

                if (isAlreadyInternalFile) {
                    // If it's an existing internal file, use its path directly
                    // Note: uri.path for file:// URIs is typically the absolute path without the scheme
                    processedPhotos.add(Photo(entryId = _currentEntryId.value ?: 0L, uri = uri.path!!))
                } else {
                    // Otherwise, it's a new photo from picker (content://) or a malformed URI
                    // Save it to internal storage and use the new localPath
                    val localPath = imageStorageManager.saveImageToInternalStorage(uri)
                    if (localPath != null) {
                        processedPhotos.add(Photo(entryId = _currentEntryId.value ?: 0L, uri = localPath))
                    } else {
                        // Handle error, e.g., log, show toast. For now, skip.
                    }
                }
            }

            // 2. Entry 및 Photo 정보 저장
            val now = System.currentTimeMillis()
            val entry = Entry(
                id = _currentEntryId.value ?: 0L, // Use existing ID if available
                dateEpochDay = currentState.date.toEpochDay(),
                mood = currentState.mood.value,
                note = currentState.note,
                createdAt = originalEntry?.createdAt ?: now, // Retain original createdAt or set new
                updatedAt = now
            )

            repo.upsert(entry, processedPhotos) // Changed photos to processedPhotos
            _state.value = currentState.copy(saving = false) // Reset saving state
            onDone()
        }
    }
}
