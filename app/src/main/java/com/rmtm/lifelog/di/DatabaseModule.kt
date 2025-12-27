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
 * [DatabaseModule]
 * - Room DB와 DAO를 Hilt로 주입받을 수 있게 제공합니다.
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
