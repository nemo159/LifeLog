/**
 * [프로젝트 루트 빌드 설정]
 * 이 파일은 프로젝트 전체의 공통 빌드 설정을 담당합니다.
 * 모든 모듈에서 사용할 플러그인 버전을 이곳에서 중앙 관리합니다.
 */
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.ksp) apply false
}
