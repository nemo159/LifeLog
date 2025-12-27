/**
 * settings.gradle.kts
 * - 플러그인/의존성 리포지토리를 설정합니다.
 * - Gradle Kotlin DSL 최신 방식으로 구성합니다.
 */
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "LifeLog"
include(":app")