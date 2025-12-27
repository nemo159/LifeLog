package com.rmtm.lifelog.data.repository

import com.rmtm.lifelog.core.model.Entry
import com.rmtm.lifelog.core.model.Photo
import kotlinx.coroutines.flow.Flow

/**
 * [EntryRepository]
 * - UI(ViewModel)에서 DB/파일/네트워크 등의 세부 구현을 모르도록 추상화합니다.
 * - 향후 AI 분석 결과 저장, 백업, 검색/필터 확장 시에도 Repository 레벨에서 통합하기 쉽습니다.
 */
interface EntryRepository {
    fun observeEntries(): Flow<List<Entry>>
    suspend fun upsert(entry: Entry, photos: List<Photo>): Long
}
