package com.rmtm.lifelog

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * [LifeLogApplication]
 * - 앱 전역 Application 클래스입니다.
 * - Hilt(의존성 주입)를 사용하려면 @HiltAndroidApp 선언이 반드시 필요합니다.
 * - AndroidManifest.xml의 application android:name 에 등록되어 있어야 합니다.
 */
@HiltAndroidApp
class LifeLogApplication : Application(), Configuration.Provider {

    /**
     * WorkManager가 Worker를 만들 때 사용할 Factory
     * - Hilt가 생성한 WorkerFactory를 주입받아 연결합니다.
     */
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    /**
     * WorkManager 설정을 제공
     * - Android 12 이하/이상 상관없이 동일하게 적용됩니다.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}