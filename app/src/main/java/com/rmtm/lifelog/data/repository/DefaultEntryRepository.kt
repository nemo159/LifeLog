package com.rmtm.lifelog.data.repository

import com.rmtm.lifelog.core.model.Entry
import com.rmtm.lifelog.core.model.Photo
import com.rmtm.lifelog.data.local.dao.EntryDao
import com.rmtm.lifelog.data.local.dao.PhotoDao
import com.rmtm.lifelog.data.local.mapper.toDomain
import com.rmtm.lifelog.data.local.mapper.toEntity
import com.rmtm.lifelog.util.ImageStorageManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * [DefaultEntryRepository]
 * - Room DAO를 이용해 실제 데이터 처리를 수행합니다.
 */
class DefaultEntryRepository @Inject constructor(
    private val entryDao: EntryDao,
    private val photoDao: PhotoDao,
    private val imageStorageManager: ImageStorageManager
) : EntryRepository {

    override fun observeEntries(): Flow<List<Entry>> =
        entryDao.observeAll().map { list ->
            list.map { it.toDomain() }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeEntry(id: Long): Flow<Entry?> {
        return entryDao.observeAll().map { list ->
            list.find { it.id == id }?.toDomain()
        }.flatMapLatest { entry ->
            if (entry == null) flowOf(null)
            else {
                val photos = photoDao.getByEntry(entry.id).map { it.toDomain() }
                flowOf(entry.copy(photos = photos))
            }
        }
    }

    override suspend fun upsert(entry: Entry, photos: List<Photo>): Long {
        val newId = entryDao.upsert(entry.toEntity())
        val targetId = if (entry.id == 0L) newId else entry.id

        photoDao.deleteByEntry(targetId)
        if (photos.isNotEmpty()) {
            photoDao.upsertAll(photos.map { it.copy(entryId = targetId).toEntity() })
        }
        return targetId
    }

    override suspend fun delete(entry: Entry) {
        val photos = photoDao.getByEntry(entry.id)
        photos.forEach {
            imageStorageManager.deleteImage(it.uri)
        }
        photoDao.deleteByEntry(entry.id)
        entryDao.deleteById(entry.id)
    }
}