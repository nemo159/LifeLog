package com.rmtm.lifelog.data.repository

import com.rmtm.lifelog.core.model.Entry
import com.rmtm.lifelog.core.model.Photo
import com.rmtm.lifelog.data.local.dao.EntryDao
import com.rmtm.lifelog.data.local.dao.PhotoDao
import com.rmtm.lifelog.data.local.mapper.toDomain
import com.rmtm.lifelog.data.local.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * [DefaultEntryRepository]
 * - Room DAO를 이용해 실제 데이터 처리를 수행합니다.
 * - 사진은 Entry에 종속되므로, upsert 시 photos도 함께 정합성을 맞추는 방식으로 확장 가능합니다.
 *   (현재 MVP에서는 photos는 저장 구조만 준비하고, UI에서 아직 생성하지 않는 형태)
 */
class DefaultEntryRepository @Inject constructor(
    private val entryDao: EntryDao,
    private val photoDao: PhotoDao
) : EntryRepository {

    override fun observeEntries(): Flow<List<Entry>> =
        entryDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(entry: Entry, photos: List<Photo>): Long {
        val newId = entryDao.upsert(entry.toEntity())
        val targetId = if (entry.id == 0L) newId else entry.id

        // MVP에서는 아직 photos UI를 붙이지 않았지만,
        // 기본틀로 “Entry 저장과 함께 사진 정합성 맞추기” 로직을 마련해 둡니다.
        photoDao.deleteByEntry(targetId)
        if (photos.isNotEmpty()) {
            photoDao.upsertAll(photos.map { it.copy(entryId = targetId).toEntity() })
        }
        return targetId
    }
}
