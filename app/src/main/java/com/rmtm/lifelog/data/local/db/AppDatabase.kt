package com.rmtm.lifelog.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rmtm.lifelog.data.local.dao.EntryDao
import com.rmtm.lifelog.data.local.dao.PhotoDao
import com.rmtm.lifelog.data.local.entity.EntryEntity
import com.rmtm.lifelog.data.local.entity.PhotoEntity

/**
 * [AppDatabase]
 * - Room DB 루트 클래스입니다.
 * - 엔티티(테이블)와 DAO를 등록합니다.
 */
@Database(
    entities = [EntryEntity::class, PhotoEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
    abstract fun photoDao(): PhotoDao
}
