package com.rmtm.lifelog.data.repository

import com.rmtm.lifelog.core.model.Entry
import com.rmtm.lifelog.core.model.Photo
import kotlinx.coroutines.flow.Flow

/**
 * [저장소 인터페이스]
 * 이 파일은 데이터(일기)를 다루는 기능들의 목록(설계도)입니다.
 * - "일기 목록을 줘", "저장해", "삭제해" 같은 기능들이 정의되어 있습니다.
 * - 실제 구현은 'DefaultEntryRepository'에서 합니다.
 */
interface EntryRepository {
    fun observeEntries(): Flow<List<Entry>>
    fun observeEntry(id: Long): Flow<Entry?>
    suspend fun upsert(entry: Entry, photos: List<Photo>): Long
    suspend fun delete(entry: Entry)
}
