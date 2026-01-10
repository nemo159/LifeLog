/**
 * [프로젝트 설정 파일]
 * 이 파일은 프로젝트의 구성을 담당합니다.
 * - 'app' 모듈을 포함시킵니다.
 * - 플러그인과 라이브러리를 어디서 다운로드할지(Repositories) 설정합니다.
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