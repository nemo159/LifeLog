package com.rmtm.lifelog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rmtm.lifelog.data.local.entity.PhotoEntity

/**
 * [PhotoDao]
 * - 사진은 Entry에 종속되는 데이터이므로, entryId 기반 조회/삭제가 핵심입니다.
 */
@Dao
interface PhotoDao {

    /** 특정 Entry에 속한 사진 목록 조회 */
    @Query("SELECT * FROM photos WHERE entryId = :entryId ORDER BY id ASC")
    suspend fun getByEntry(entryId: Long): List<PhotoEntity>

    /** 사진 목록 일괄 저장 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<PhotoEntity>)

    /** Entry에 속한 사진 전체 삭제 (Entry 삭제 전/후 정합성 유지 목적) */
    @Query("DELETE FROM photos WHERE entryId = :entryId")
    suspend fun deleteByEntry(entryId: Long)
}
