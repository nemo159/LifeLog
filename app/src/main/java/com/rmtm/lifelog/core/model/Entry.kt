package com.rmtm.lifelog.core.model

/**
 * [도메인 모델: 일기]
 * 이 파일은 앱에서 다루는 가장 핵심적인 데이터인 '일기(Entry)'를 정의합니다.
 * - 날짜, 기분, 메모 내용, 사진 목록 등의 정보를 담고 있습니다.
 * - 데이터베이스나 화면에서 이 형태의 데이터를 주고받습니다.
 */
data class Entry(
    val id: Long = 0L,
    val dateEpochDay: Long,
    val mood: Int,
    val note: String,
    val photos: List<Photo> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long,
    val aiSummary: String? = null,
    val moodScore: Float? = null
)
