package com.rmtm.lifelog.navigation

/**
 * [화면 주소록]
 * 앱의 각 화면을 구분하는 '이름'들을 모아둔 파일입니다.
 * - "timeline", "editor" 처럼 각 화면의 고유한 주소를 상수로 관리합니다.
 */
object Routes {
    const val TIMELINE = "timeline" // 기록 목록 화면
    const val EDITOR = "editor"     // 기록 작성 화면
    const val DETAIL = "detail/{entryId}" // 기록 상세 화면

    fun detail(entryId: Long) = "detail/$entryId"
}
