package com.rmtm.lifelog.core.model

/**
 * [Photo]
 * - 기록(Entry)에 첨부되는 사진 정보를 표현합니다.
 * - target 36 정책 관점에서 “전체 미디어 접근 권한”보다는
 *   Photo Picker로 사용자 선택 URI를 받아 저장하는 방식이 안전합니다.
 *
 * @param uri Photo Picker 등으로 받은 URI 문자열
 * @param thumbPath 내부 저장소에 생성한 썸네일 파일 경로(선택)
 */
data class Photo(
    val id: Long = 0L,
    val entryId: Long,
    val uri: String,
    val thumbPath: String? = null
)
