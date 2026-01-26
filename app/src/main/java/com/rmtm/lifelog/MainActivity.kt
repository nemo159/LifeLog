package com.rmtm.lifelog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rmtm.lifelog.navigation.LifeLogNavHost
import com.rmtm.lifelog.ui.theme.LifeLogTheme
import com.rmtm.lifelog.ui.theme.ThemeMode
import com.rmtm.lifelog.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * [메인 화면: 메인 액티비티]
 * 이 파일은 앱의 유일한 화면(Activity)입니다.
 * - 여기서 네비게이션(화면 이동) 설정을 불러오고 앱을 시작합니다.
 * - Compose를 사용하여 UI를 그립니다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
            val isDarkTheme = shouldUseDarkTheme(themeMode)

            LifeLogTheme(darkTheme = isDarkTheme) {
                LifeLogNavHost()
            }
        }
    }
}

@Composable
private fun shouldUseDarkTheme(themeMode: ThemeMode): Boolean {
    return when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
}
