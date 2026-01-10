package com.rmtm.lifelog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rmtm.lifelog.data.local.entity.PhotoEntity

/**
 * [데이터 접근 객체(DAO): 사진]
 * 이 파일은 데이터베이스에서 사진 데이터를 조작하는 명령어를 정의합니다.
 * - 특정 일기에 포함된 사진들을 불러오거나 저장/삭제합니다.
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
