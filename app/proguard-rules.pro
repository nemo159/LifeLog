# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

####################################
# 기본 설정
####################################
# 경고 무시 (라이브러리 내부 경고로 빌드 깨지는 것 방지)
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**

####################################
# Room 관련
####################################
# Entity / DAO / Database는 난독화해도 되지만,
# 리플렉션/검증에서 문제 생기는 경우를 대비해 keep
-keep class androidx.room.** { *; }

# 네 앱의 Entity 패키지 (권장)
-keep class com.rmtm.lifelog.core.model.** { *; }

####################################
# Hilt / Dagger
####################################
# Hilt가 생성한 클래스 보호
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Hilt Worker
-keep class androidx.hilt.work.** { *; }

####################################
# WorkManager
####################################
-keep class androidx.work.** { *; }

####################################
# Coroutine (경고 방지)
####################################
-dontwarn kotlinx.coroutines.**

#####################################
## Google Play Services & Auth
#####################################
#-keep class com.google.android.gms.auth.api.signin.** { *; }
#-keep class com.google.android.gms.common.api.** { *; }
#
#####################################
## Google Drive API Specific
#####################################
## 드라이브 API의 모델 클래스들이 난독화되면 JSON 파싱 시 'Key'를 찾지 못합니다.
#-keep class com.google.api.services.drive.model.** { *; }
#-keep class com.google.api.services.drive.** { *; }
#
## Google API Client Library 내부의 자원 보호
#-keep class com.google.api.client.json.** { *; }
#-keep class com.google.api.client.extensions.android.** { *; }
#
#####################################
## Jackson / Gson (드라이브 API가 사용하는 파서)
#####################################
## 만약 Jackson을 사용한다면
#-keep class com.fasterxml.jackson.** { *; }
#-dontwarn com.fasterxml.jackson.**
#
## 만약 Gson을 사용한다면
#-keep class com.google.gson.** { *; }
#-keep class com.google.api.client.json.gson.** { *; }

# 1. Google API Client 및 HTTP Transport 보호
-keep class com.google.api.client.** { *; }
-keep interface com.google.api.client.** { *; }

# 2. Google Drive 서비스 모델 클래스 (데이터 매핑용)
# 이 부분이 난독화되면 API 응답 값을 클래스 필드에 담지 못합니다.
-keep class com.google.api.services.drive.** { *; }
-keep interface com.google.api.services.drive.** { *; }

# 3. 데이터 처리를 위한 내부 라이브러리 (Guava 등)
-keep class com.google.common.base.** { *; }
-keep class com.google.common.collect.** { *; }

# 4. JSON 파싱 관련 (Jackson/Gson)
# 드라이브 API 응답의 JSON 키 값을 유지하기 위해 필수입니다.
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault
-keepclassmembers class * {
    @com.google.api.client.util.Key <fields>;
    @com.google.api.client.util.Value <fields>;
}

# 5. 기타 구글 인증 관련
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }
-dontwarn com.google.api.client.**
-dontwarn com.google.android.gms.**

####################################
# Apache HTTP Client (Google API 종속성)
####################################
# Android에 포함되지 않은 Java SE 클래스에 대한 경고를 무시합니다.
# R8 빌드 오류(Missing classes)를 해결합니다.
-dontwarn org.apache.http.**
-dontwarn javax.naming.**
-dontwarn org.ietf.jgss.**