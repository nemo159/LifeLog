package com.rmtm.lifelog.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 날짜 표시 유틸
 * - DB에는 epochDay(Long)로 저장하지만,
 *   UI에서는 사람이 읽기 쉬운 문자열로 변환해야 하므로 확장 함수를 제공합니다.
 */
private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun Long.toLocalDateString(): String =
    LocalDate.ofEpochDay(this).format(formatter)
