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
 * [저장소 구현체]
 * 이 파일은 `EntryRepository`에 정의된 기능들을 실제로 수행하는 곳입니다.
 * - 데이터베이스(DAO)와 이미지 관리자 등을 사용해 실제로 데이터를 저장하고 불러옵니다.
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

        // Get current photos associated with this entry from DB
        val existingPhotoEntities = photoDao.getByEntry(targetId)
        val existingPhotoUrisInDb = existingPhotoEntities.map { it.uri }.toSet()

        // URIs of photos that should be in the DB after this upsert
        val incomingPhotoUris = photos.map { it.uri }.toSet()

        // 1. Delete photos that are in DB but not in incoming photos (removed by user)
        val photosToDelete = existingPhotoEntities.filter { it.uri !in incomingPhotoUris }
        photosToDelete.forEach {
            imageStorageManager.deleteImage(it.uri) // Delete actual file
            photoDao.delete(it) // Delete from DB
        }

        // 2. Add/update photos that are in incoming photos but not in DB (newly added/modified)
        val photosToUpsert = photos.filter { it.uri !in existingPhotoUrisInDb }
        if (photosToUpsert.isNotEmpty()) {
            photoDao.upsertAll(photosToUpsert.map { it.copy(entryId = targetId).toEntity() })
        }
        return targetId
    }

    override suspend fun getEntryById(id: Long): Entry? {
        val entryEntity = entryDao.getById(id)
        return entryEntity?.toDomain()?.let { domainEntry ->
            val photoEntities = photoDao.getByEntry(id)
            domainEntry.copy(photos = photoEntities.map { it.toDomain() })
        }
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