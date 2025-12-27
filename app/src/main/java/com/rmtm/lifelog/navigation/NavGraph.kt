package com.rmtm.lifelog.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rmtm.lifelog.feature.editor.EditorScreen
import com.rmtm.lifelog.feature.editor.EditorViewModel
import com.rmtm.lifelog.feature.timeline.TimelineScreen
import com.rmtm.lifelog.feature.timeline.TimelineViewModel

/**
 * [LifeLogNavHost]
 * - 앱 내 내비게이션 그래프입니다.
 * - MVP 단계에서는 "타임라인(목록)"과 "에디터(작성)"만 구성합니다.
 * - 상세 화면(Detail)은 다음 단계에 추가해도 됩니다.
 */
@Composable
fun LifeLogNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.TIMELINE
    ) {
        composable(Routes.TIMELINE) {
            val vm: TimelineViewModel = hiltViewModel()
            TimelineScreen(
                state = vm.state,
                onAdd = { navController.navigate(Routes.EDITOR) }
            )
        }

        composable(Routes.EDITOR) {
            val vm: EditorViewModel = hiltViewModel()
            EditorScreen(
                state = vm.state,
                onSave = { vm.save(it) { navController.popBackStack() } },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
