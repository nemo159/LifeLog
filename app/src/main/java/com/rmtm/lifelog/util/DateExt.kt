package com.rmtm.lifelog.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * [날짜 변환 도구]
 * 숫자로 저장된 날짜를 "2024-01-01" 같은 글자로 바꿔주는 도구입니다.
 * - 화면에 날짜를 보여줄 때 사용합니다.
 */
private val localDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val localDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault())

fun Long.toLocalDateString(): String =
    LocalDate.ofEpochDay(this).format(localDateFormatter)

fun Long.toLocalDateTimeString(): String =
    Instant.ofEpochMilli(this).let { localDateTimeFormatter.format(it) }
