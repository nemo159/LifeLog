package com.rmtm.lifelog.di

import com.rmtm.lifelog.data.repository.DefaultEntryRepository
import com.rmtm.lifelog.data.repository.EntryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * [RepositoryModule]
 * - Repository 인터페이스와 실제 구현체를 바인딩합니다.
 * - 테스트 시 FakeRepository로 교체하기 쉬운 구조입니다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEntryRepository(impl: DefaultEntryRepository): EntryRepository
}
