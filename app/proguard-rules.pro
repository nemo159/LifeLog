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
# Room Database 구현체와 그 생성자를 유지 (Reflection 대응)
-keep class * extends androidx.room.RoomDatabase {
    <init>(...);
}

# Room 라이브러리 내부 클래스 유지
-keep class androidx.room.** { *; }

# 엔티티 및 DAO 인터페이스 유지
-keep class com.rmtm.lifelog.data.local.entity.** { *; }
-keep class com.rmtm.lifelog.data.local.dao.** { *; }
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
## Google API Client 및 HTTP Transport 보호
#####################################
-keep class com.google.api.client.** { *; }
-keep interface com.google.api.client.** { *; }

#####################################
## Google Drive 서비스 모델 클래스 (데이터 매핑용)
#####################################
# 이 부분이 난독화되면 API 응답 값을 클래스 필드에 담지 못합니다.
-keep class com.google.api.services.drive.** { *; }
-keep interface com.google.api.services.drive.** { *; }

#####################################
## 데이터 처리를 위한 내부 라이브러리 (Guava 등)
#####################################
-keep class com.google.common.base.** { *; }
-keep class com.google.common.collect.** { *; }

#####################################
## JSON 파싱 관련 (Jackson/Gson)
#####################################
# 드라이브 API 응답의 JSON 키 값을 유지하기 위해 필수입니다.
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault
-keepclassmembers class * {
    @com.google.api.client.util.Key <fields>;
    @com.google.api.client.util.Value <fields>;
}

#####################################
## 기타 구글 인증 관련
#####################################
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