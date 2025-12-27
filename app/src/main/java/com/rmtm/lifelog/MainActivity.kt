package com.rmtm.lifelog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rmtm.lifelog.navigation.LifeLogNavHost
import dagger.hilt.android.AndroidEntryPoint

/**
 * [MainActivity]
 * - 앱의 진입점(런처 Activity) 입니다.
 * - Compose 기반 UI 루트를 구성합니다.
 * - Hilt를 사용하므로 @AndroidEntryPoint가 필요합니다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // MVP 단계에서는 별도 테마 파일 없이 Material3 기본 테마를 사용합니다.
            // 필요 시 typography/colorScheme 등을 추가로 구성하면 됩니다.
            LifeLogNavHost()
        }
    }
}
