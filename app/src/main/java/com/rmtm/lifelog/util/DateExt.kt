package com.rmtm.lifelog.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * [날짜 변환 도구]
 * 숫자로 저장된 날짜를 "2024-01-01" 같은 글자로 바꿔주는 도구입니다.
 * - 화면에 날짜를 보여줄 때 사용합니다.
 */
private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun Long.toLocalDateString(): String =
    LocalDate.ofEpochDay(this).format(formatter)
