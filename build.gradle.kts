/**
 * build.gradle.kts (root)
 * - 버전 관리를 libs.versions.toml 파일로 중앙화합니다.
 */
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.ksp) apply false
}
