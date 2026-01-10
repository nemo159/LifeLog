package com.rmtm.lifelog

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * [앱의 시작점: 애플리케이션 클래스]
 * 이 클래스는 앱이 실행될 때 가장 먼저 생성되는 객체입니다.
 * - 전역 상태를 관리하고, Hilt 라이브러리를 통해 '의존성 주입' 기능을 활성화합니다.
 * - 백그라운드 작업(WorkManager)을 위한 설정도 여기서 처리합니다.
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