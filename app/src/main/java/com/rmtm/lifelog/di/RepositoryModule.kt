package com.rmtm.lifelog.di

import com.rmtm.lifelog.data.repository.DefaultEntryRepository
import com.rmtm.lifelog.data.repository.EntryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * [DI 모듈: 저장소 연결]
 * 이 파일은 `EntryRepository`를 요청했을 때 실제로 어떤 구현체(`DefaultEntryRepository`)를 줄지 연결해줍니다.
 * - 인터페이스와 구현을 연결하는 고리 역할을 합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEntryRepository(impl: DefaultEntryRepository): EntryRepository
}
