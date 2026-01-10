package com.rmtm.lifelog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rmtm.lifelog.data.local.entity.EntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * [데이터 접근 객체(DAO): 일기]
 * 이 파일은 데이터베이스에서 일기 데이터를 넣고, 빼고, 지우는 명령어를 정의합니다.
 * - 'SQL'이라는 언어 대신 함수 이름으로 데이터를 조작할 수 있게 해줍니다.
 */
@Dao
interface EntryDao {

    /**
     * 전체 기록을 날짜 내림차순으로 관찰합니다.
     * - Flow를 사용하므로 DB 변경 시 UI가 자동 갱신됩니다.
     */
    @Query("SELECT * FROM entries ORDER BY dateEpochDay DESC, id DESC")
    fun observeAll(): Flow<List<EntryEntity>>

    /** 특정 id의 기록 1건 조회 */
    @Query("SELECT * FROM entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): EntryEntity?

    /**
     * upsert(삽입/갱신)
     * - Room에는 “진짜 upsert”도 있지만, MVP 단계에서는 REPLACE로 단순화합니다.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: EntryEntity): Long

    /** id로 삭제 */
    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
