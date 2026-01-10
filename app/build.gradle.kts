/**
 * [앱 모듈 빌드 설정]
 * 이 파일은 실제 앱(App Module)의 빌드 상세 내용을 담당합니다.
 * - 사용할 라이브러리(Room, Hilt, Compose 등)를 정의합니다.
 * - 안드로이드 버전(SDK) 정보를 설정합니다.
 */
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")

    // ✅ KSP 플러그인 적용
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.rmtm.lifelog"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.rmtm.lifelog"
        minSdk = 26
        targetSdk = 36

        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    /**
     * 자바 컴파일 타겟을 17로 통일합니다.
     * - Android Gradle Plugin 최신 조합에서 JDK 17이 사실상 표준입니다.
     */
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    /**
     * 코틀린 컴파일 타겟도 17로 통일합니다.
     * - Java/Kotlin 타겟이 다르면 컴파일 태스크 검증에서 오류가 발생할 수 있습니다.
     */
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    /**
     * Compose BOM
     * - Compose 관련 의존성 버전을 묶어서 관리합니다.
     */
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))

    // Compose / Material
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation(Compose)
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // ViewModel / Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6") // Flow를 Compose에서 Lifecycle 안전하게 수집

    // 이미지 로딩(URI 기반 사진 미리보기용) - Photo Picker 붙일 때 바로 사용
    implementation("io.coil-kt:coil-compose:2.7.0")

    // -----------------------
    // Room(로컬 DB)
    // -----------------------
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // ✅ Room Compiler: kapt(...) -> ksp(...)
    ksp("androidx.room:room-compiler:2.6.1")

    /**
     * sqlite-jdbc
     * - KSP/Room 검증을 비활성화(room.disableVerification) 할 것이므로
     *   컴파일 타임에 sqlite-jdbc를 강제로 올릴 이유가 크게 줄어듭니다.
     * - 따라서 기본은 "미사용"을 권장합니다.
     */
    // ksp("org.xerial:sqlite-jdbc:3.46.1.0")

    // -----------------------
    // Hilt(DI)
    // -----------------------
    implementation("com.google.dagger:hilt-android:2.51.1")

    // ✅ Hilt Compiler: kapt(...) -> ksp(...)
    ksp("com.google.dagger:hilt-compiler:2.51.1")

    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // -----------------------
    // WorkManager(백업/정리 배치 작업)
    // -----------------------
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Hilt + WorkManager 연동(필수)
    implementation("androidx.hilt:hilt-work:1.2.0")

    // ✅ androidx.hilt compiler도 kapt(...) -> ksp(...)
    // - 여기 의존성이 없으면 @HiltWorker, HiltWorkerFactory 연동에서 빌드/런타임 문제가 납니다.
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // 코루틴(명시적으로 고정 권장) - 이미 어딘가에서 들어올 수 있지만, 지금은 명시가 안전
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}

/**
 * ✅ KSP 옵션 전달 블록
 * - kapt { arguments { ... } } 에서 하던 것을 ksp { arg(...) }로 옮깁니다.
 * - Room 옵션들도 동일하게 전달 가능합니다.
 */
ksp {
    // Room의 컴파일 타임 SQLite 검증 비활성화(Windows 네이티브 로딩 이슈 회피)
    arg("room.disableVerification", "true")

    // 부가 옵션(유지)
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
    arg("room.generateKotlin", "true")

    /**
     * (선택) 스키마 내보내기 경로
     * - 장기적으로는 schema export 켜두는 편이 안전합니다.
     * - 필요 시 아래 주석을 해제하고 경로도 함께 관리하세요.
     *
     * arg("room.schemaLocation", "$projectDir/schemas")
     */
}

/**
 * Kotlin JVM Toolchain
 * - Gradle이 사용할 JDK를 17로 강제 고정합니다.
 * - 환경별로 JDK가 섞여 들어오는 문제를 예방합니다.
 */
kotlin {
    jvmToolchain(17)
}
