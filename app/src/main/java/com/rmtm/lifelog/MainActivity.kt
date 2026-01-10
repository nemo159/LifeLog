package com.rmtm.lifelog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rmtm.lifelog.navigation.LifeLogNavHost
import dagger.hilt.android.AndroidEntryPoint

/**
 * [메인 화면: 메인 액티비티]
 * 이 파일은 앱의 유일한 화면(Activity)입니다.
 * - 여기서 네비게이션(화면 이동) 설정을 불러오고 앱을 시작합니다.
 * - Compose를 사용하여 UI를 그립니다.
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
