package com.rmtm.lifelog.data.local.mapper

import com.rmtm.lifelog.core.model.Entry
import com.rmtm.lifelog.core.model.Photo
import com.rmtm.lifelog.data.local.entity.EntryEntity
import com.rmtm.lifelog.data.local.entity.PhotoEntity

/**
 * Mapper
 * - DB(Entity) <-> 도메인 모델 간 변환을 담당합니다.
 * - UI/비즈니스 로직이 DB 구조에 종속되지 않도록 하는 핵심 레이어입니다.
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
