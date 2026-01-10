package com.rmtm.lifelog.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rmtm.lifelog.data.local.dao.EntryDao
import com.rmtm.lifelog.data.local.dao.PhotoDao
import com.rmtm.lifelog.data.local.entity.EntryEntity
import com.rmtm.lifelog.data.local.entity.PhotoEntity

/**
 * [데이터베이스 설정]
 * 이 파일은 앱 전체의 데이터베이스를 구성하는 설정 파일입니다.
 * - 어떤 테이블(일기, 사진)이 있는지, 어떤 DAO를 통해 접근할지 정의합니다.
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
