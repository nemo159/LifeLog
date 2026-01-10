package com.rmtm.lifelog.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * [데이터베이스 모델: 사진 테이블]
 * 이 파일은 스마트폰 내부 저장소(Room DB)에 저장되는 '사진' 정보를 정의합니다.
 * - 'photos'라는 이름의 테이블로 저장됩니다.
 * - 어떤 일기(entryId)에 포함된 사진인지 연결 정보를 가지고 있습니다.
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
