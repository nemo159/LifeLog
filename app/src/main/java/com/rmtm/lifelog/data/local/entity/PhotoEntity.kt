package com.rmtm.lifelog.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * [PhotoEntity]
 * - Entry(기록)와 1:N 관계로 저장되는 사진 테이블 모델입니다.
 * - entryId로 조회가 빈번하므로 인덱스를 둡니다.
 */
@Entity(
    tableName = "photos",
    indices = [Index(value = ["entryId"])]
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val entryId: Long,
    val uri: String,
    val thumbPath: String?
)
