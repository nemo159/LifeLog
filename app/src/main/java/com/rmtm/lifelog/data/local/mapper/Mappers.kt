package com.rmtm.lifelog.data.local.mapper

import com.rmtm.lifelog.core.model.Entry
import com.rmtm.lifelog.core.model.Photo
import com.rmtm.lifelog.data.local.entity.EntryEntity
import com.rmtm.lifelog.data.local.entity.PhotoEntity

/**
 * [데이터 변환기(Mapper)]
 * 이 파일은 서로 다른 형태의 데이터 모델을 변환해주는 역할을 합니다.
 * - 데이터베이스용 모델(Entity) <-> 앱 내부용 모델(Domain) 사이를 변환합니다.
 */

fun EntryEntity.toDomain(): Entry = Entry(
    id = id,
    dateEpochDay = dateEpochDay,
    mood = mood,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
    aiSummary = aiSummary,
    moodScore = moodScore
)

fun Entry.toEntity(): EntryEntity = EntryEntity(
    id = id,
    dateEpochDay = dateEpochDay,
    mood = mood,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
    aiSummary = aiSummary,
    moodScore = moodScore
)

fun PhotoEntity.toDomain(): Photo = Photo(
    id = id,
    entryId = entryId,
    uri = uri,
    thumbPath = thumbPath
)

fun Photo.toEntity(): PhotoEntity = PhotoEntity(
    id = id,
    entryId = entryId,
    uri = uri,
    thumbPath = thumbPath
)
