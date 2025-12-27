package com.rmtm.lifelog.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * [EntryEntity]
 * - Room DB에 저장되는 테이블 모델입니다.
 * - 정렬/조회 성능을 위해 dateEpochDay, createdAt에 인덱스를 둡니다.
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
