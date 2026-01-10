package com.rmtm.lifelog.core.model

/**
 * [Entry]
 * - 앱의 “기록 1건”을 표현하는 도메인 모델입니다.
 * - UI와 비즈니스 로직에서는 이 모델을 사용하고,
 *   DB 저장용 Entity와는 Mapper로 변환하도록 분리합니다.
 *
 * @param dateEpochDay 날짜를 LocalDate.toEpochDay() 형태로 저장 (시간대 이슈 최소화)
 * @param mood 기분 점수(1~5). MVP에서는 단순 정수로 사용
 * @param note 메모(일기/회고) 본문
 * @param aiSummary AI 요약 결과(옵션). 추후 룰 기반/온디바이스 모델 적용 시 저장
 * @param moodScore 감정 점수(옵션). 텍스트 분석 결과를 저장할 수 있는 확장 슬롯
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
