package com.rmtm.lifelog.di

import android.content.Context
import androidx.room.Room
import com.rmtm.lifelog.data.local.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * [DI 모듈: 데이터베이스]
 * 이 파일은 데이터베이스(DB) 생성 방법을 앱 전체에 알려주는 설정 파일입니다.
 * - 어디서든 `AppDatabase`나 `Dao`가 필요하면 이 설정대로 만들어줍니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        // 서버가 없는 로컬 앱이므로, 단일 DB 파일로 충분합니다.
        // 추후 마이그레이션이 필요해지면 migration을 추가하면 됩니다.
        return Room.databaseBuilder(context, AppDatabase::class.java, "lifelog.db")
            .build()
    }

    @Provides
    fun provideEntryDao(db: AppDatabase) = db.entryDao()

    @Provides
    fun providePhotoDao(db: AppDatabase) = db.photoDao()
}
