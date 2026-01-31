package com.rmtm.lifelog.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
import com.rmtm.lifelog.feature.settings.SettingsScreen
import com.rmtm.lifelog.feature.timeline.TimelineScreen
import com.rmtm.lifelog.feature.timeline.TimelineViewModel

/**
 * [네비게이션 그래프]
 * 화면 간의 이동 경로를 지도처럼 정의해둔 파일입니다.
 * - '타임라인 -> 작성화면', '타임라인 -> 상세화면' 등의 이동 규칙을 연결합니다.
 */
@Composable
fun LifeLogNavHost() {
    val navController = rememberNavController()
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current as? android.app.Activity

    if (showExitDialog) {
        ExitConfirmationDialog(
            onConfirm = {
                context?.finish()
            },
            onDismiss = { showExitDialog = false }
        )
    }

    NavHost(
        navController = navController,
        startDestination = Routes.TIMELINE,
        enterTransition = { fadeIn(animationSpec = tween(220, delayMillis = 90)) },
        exitTransition = { fadeOut(animationSpec = tween(90)) }
    ) {
        composable(Routes.TIMELINE) {
            BackHandler {
                showExitDialog = true
            }

            val vm: TimelineViewModel = hiltViewModel()
            TimelineScreen(
                state = vm.state,
                onAdd = { navController.navigate(Routes.EDITOR) },
                onEntryClick = { entry ->
                    navController.navigate(Routes.detail(entry.id))
                },
                onSortChange = vm::onSortOrderChanged,
                onDateSelect = vm::selectDate,
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onToggleHeaderExpansion = vm::toggleHeaderExpansion
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
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

@Composable
fun ExitConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("앱 종료") },
        text = { Text("앱을 종료하시겠습니까?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("종료")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}