package com.rmtm.lifelog.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * [데이터베이스 모델: 일기 테이블]
 * 이 파일은 스마트폰 내부 저장소(Room DB)에 실제로 저장되는 '일기'의 형태를 정의합니다.
 * - 'entries'라는 이름의 테이블로 저장됩니다.
 * - 검색을 빠르게 하기 위해 날짜 등에 색인(Index)을 걸어둡니다.
 */
@Entity(
    tableName = "entries",
    indices = [
        Index(value = ["dateEpochDay"]),
        Index(value = ["createdAt"])
    ]
)
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val dateEpochDay: Long,
    val mood: Int,
    val note: String,
    val createdAt: Long,
    val updatedAt: Long,
    val aiSummary: String? = null,
    val moodScore: Float? = null
)
