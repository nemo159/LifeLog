package com.rmtm.lifelog.core.model

/**
 * [도메인 모델: 사진]
 * 이 파일은 일기에 첨부된 '사진' 정보를 정의합니다.
 * - 사진의 위치(URI)와 썸네일 경로 등을 담고 있습니다.
 */
data class Photo(
    val id: Long = 0L,
    val entryId: Long,
    val uri: String,
    val thumbPath: String? = null
)
