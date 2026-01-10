package com.rmtm.lifelog.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rmtm.lifelog.feature.detail.DetailScreen
import com.rmtm.lifelog.feature.detail.DetailViewModel
import com.rmtm.lifelog.feature.editor.EditorScreen
import com.rmtm.lifelog.feature.editor.EditorViewModel
import com.rmtm.lifelog.feature.timeline.TimelineScreen
import com.rmtm.lifelog.feature.timeline.TimelineViewModel

/**
 * [LifeLogNavHost]
 * - 앱 내 내비게이션 그래프입니다.
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
                onAdd = { navController.navigate(Routes.EDITOR) },
                onEntryClick = { entry ->
                    navController.navigate(Routes.detail(entry.id))
                },
                onToggleSort = vm::toggleSortOrder
            )
        }

        composable(Routes.EDITOR) {
            val vm: EditorViewModel = hiltViewModel()
            EditorScreen(
                state = vm.state,
                onMoodChanged = vm::onMoodChanged,
                onNoteChanged = vm::onNoteChanged,
                onPhotosSelected = vm::onPhotosSelected,
                onSave = { vm.save { navController.popBackStack() } },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) {
            val vm: DetailViewModel = hiltViewModel()
            DetailScreen(
                state = vm.state,
                onBack = { navController.popBackStack() },
                onDelete = { vm.delete { navController.popBackStack() } }
            )
        }
    }
}