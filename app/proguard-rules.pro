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
