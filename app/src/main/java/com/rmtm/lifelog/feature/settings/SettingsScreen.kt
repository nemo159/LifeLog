package com.rmtm.lifelog.feature.settings

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.model.File
import com.rmtm.lifelog.ui.theme.ThemeMode
import com.rmtm.lifelog.ui.theme.ThemeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

/**
 * [설정 화면]
 * 앱의 여러 기능들을 설정하는 화면입니다.
 * - 구글 로그인
 * - 백업/복원
 * - 테마 설정
 * - 앱 버전 정보 표시
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showRestoreSuccessDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmationDialog by remember { mutableStateOf(false) }
    var showBackupConfirmationDialog by remember { mutableStateOf(false) }
    var showRevokeConfirmationDialog by remember { mutableStateOf(false) }
    var showSignOutConfirmationDialog by remember { mutableStateOf(false) }


    // Google 로그인 Intent를 위한 ActivityResultLauncher
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.onSignInResult(account)
        } catch (e: ApiException) {
            viewModel.onSignInResult(null)
        }
    }

    // Google 로그인 옵션 (ID와 기본 프로필, Drive 접근 권한 요청)
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/drive.file"))
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    LaunchedEffect(uiState.backupEvent) {
        when (val event = uiState.backupEvent) {
            is BackupEvent.Success -> {
                Toast.makeText(context, "백업이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                viewModel.consumeBackupEvent()
            }

            is BackupEvent.Failure -> {
                Toast.makeText(context, "백업 실패: ${event.message}", Toast.LENGTH_SHORT).show()
                viewModel.consumeBackupEvent()
            }

            null -> Unit
        }
    }

    LaunchedEffect(uiState.restoreEvent) {
        when (val event = uiState.restoreEvent) {
            is RestoreEvent.Success -> {
                showRestoreSuccessDialog = true
                viewModel.consumeRestoreEvent()
            }

            is RestoreEvent.Failure -> {
                Toast.makeText(context, "복원 실패: ${event.message}", Toast.LENGTH_SHORT).show()
                viewModel.consumeRestoreEvent()
            }

            null -> Unit
        }
    }


    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = themeMode,
            onDismiss = { showThemeDialog = false },
            onConfirm = { mode ->
                themeViewModel.setThemeMode(mode)
                showThemeDialog = false
            }
        )
    }

    if (showRestoreSuccessDialog) {
        RestoreSuccessDialog(
            onConfirm = {
                showRestoreSuccessDialog = false
                (context as? Activity)?.finishAffinity()
            }
        )
    }

    if (showRestoreConfirmationDialog) {
        RestoreConfirmationDialog(
            onConfirm = {
                showRestoreConfirmationDialog = false
                viewModel.listBackupFiles()
            },
            onDismiss = { showRestoreConfirmationDialog = false }
        )
    }

    if (showBackupConfirmationDialog) {
        BackupConfirmationDialog(
            onConfirm = {
                showBackupConfirmationDialog = false
                viewModel.backup()
            },
            onDismiss = { showBackupConfirmationDialog = false }
        )
    }

    if (showRevokeConfirmationDialog) {
        RevokeConfirmationDialog(
            onConfirm = {
                showRevokeConfirmationDialog = false
                googleSignInClient.revokeAccess().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        viewModel.signOut()
                        Toast.makeText(context, "연동이 해제되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "연동 해제에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { showRevokeConfirmationDialog = false }
        )
    }

    if (showSignOutConfirmationDialog) {
        SignOutConfirmationDialog(
            onConfirm = {
                showSignOutConfirmationDialog = false
                googleSignInClient.signOut().addOnCompleteListener {
                    viewModel.signOut()
                    Toast.makeText(context, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showSignOutConfirmationDialog = false }
        )
    }

    uiState.backupFiles?.let { files ->
        BackupFilesDialog(
            files = files,
            onDismiss = { viewModel.clearBackupFiles() },
            onFileSelected = { fileId, fileName ->
                viewModel.restore(fileId, fileName)
            }
        )
    }

    if (uiState.showPermissionAlertFor != null) {
        PermissionNeededDialog(
            onDismiss = { viewModel.setPendingAction(null) },
            onConfirm = {
                viewModel.setPendingAction(null)
                signInLauncher.launch(googleSignInClient.signInIntent)
            }
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 계정 섹션
                SettingsSection(title = "계정") {
                    if (uiState.signedInUser == null) {
                        Button(
                            onClick = { signInLauncher.launch(googleSignInClient.signInIntent) },
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = "Google 로그인 아이콘",
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Google 계정으로 로그인")
                        }
                    } else {
                        UserInfoCard(
                            user = uiState.signedInUser!!,
                            onSignOut = { showSignOutConfirmationDialog = true },
                            onRevoke = { showRevokeConfirmationDialog = true }
                        )
                    }
                }

                if (uiState.signedInUser != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    // 데이터 섹션
                    SettingsSection(title = "데이터") {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    val account = GoogleSignIn.getLastSignedInAccount(context)
                                    val hasPermission = account != null && GoogleSignIn.hasPermissions(account, Scope("https://www.googleapis.com/auth/drive.file"))
                                    if (hasPermission) {
                                        showBackupConfirmationDialog = true
                                    } else {
                                        viewModel.setPendingAction(PendingAction.BACKUP)
                                    }
                                },
                                enabled = !uiState.isLoading,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("백업")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val account = GoogleSignIn.getLastSignedInAccount(context)
                                    val hasPermission = account != null && GoogleSignIn.hasPermissions(account, Scope("https://www.googleapis.com/auth/drive.file"))
                                    if (hasPermission) {
                                        showRestoreConfirmationDialog = true
                                    } else {
                                        viewModel.setPendingAction(PendingAction.RESTORE)
                                    }
                                },
                                enabled = !uiState.isLoading,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("복원")
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // 앱 설정 섹션
                SettingsSection(title = "앱 설정") {
                    SettingItem(
                        title = "테마 설정",
                        value = when (themeMode) {
                            ThemeMode.LIGHT -> "라이트"
                            ThemeMode.DARK -> "다크"
                            ThemeMode.SYSTEM -> "시스템 설정"
                        },
                        onClick = { showThemeDialog = true }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // 정보 섹션
                SettingsSection(title = "정보") {
                    Text("앱 버전: ${uiState.appVersionName}")
                }
            }
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun PermissionNeededDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("권한 필요") },
        text = { Text("Google 드라이브 접근 권한을 허용해야 이용할 수 있습니다.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}


@Composable
private fun RestoreSuccessDialog(onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* 사용자가 외부를 클릭해도 닫히지 않음 */ },
        title = { Text("복원 완료") },
        text = { Text("복원이 완료되었습니다.\n확인 버튼을 누른 후 앱을 다시 시작해주세요.") },
        confirmButton = {
            TextButton(onClick = { exitProcess(0) }) {
                Text("확인")
            }
        }
    )
}

@Composable
private fun BackupConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("백업 확인") },
        text = { Text("백업하시겠습니까?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
private fun RestoreConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("복원 경고") },
        text = { Text("백업하지 않은 데이터는 복원 시 삭제됩니다. 계속하시겠습니까?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
private fun RevokeConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("계정 연동 해제") },
        text = { Text("계정 연동을 해제하시겠습니까? 앱의 구글 드라이브 접근 권한이 삭제됩니다.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
private fun SignOutConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("로그아웃") },
        text = { Text("로그아웃하시겠습니까?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
private fun BackupFilesDialog(
    files: List<File>,
    onDismiss: () -> Unit,
    onFileSelected: (fileId: String, fileName: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("복원할 파일 선택") },
        text = {
            if (files.isEmpty()) {
                Text("백업 파일이 없습니다.")
            } else {
                LazyColumn {
                    items(files.sortedByDescending { it.modifiedTime.value }) { file ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFileSelected(file.id, file.name) }
                            .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}


@Composable
private fun SettingItem(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * 테마 선택 다이얼로그
 */
@Composable
private fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onDismiss: () -> Unit,
    onConfirm: (ThemeMode) -> Unit
) {
    var selectedTheme by remember { mutableStateOf(currentTheme) }
    val themes = listOf(
        ThemeMode.LIGHT to "라이트",
        ThemeMode.DARK to "다크",
        ThemeMode.SYSTEM to "시스템 설정"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("테마 선택") },
        text = {
            Column {
                themes.forEach { (theme, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (theme == selectedTheme),
                                onClick = { selectedTheme = theme },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == selectedTheme),
                            onClick = null // Row의 onClick으로 처리
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedTheme) }) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}


/**
 * 설정의 각 섹션을 구분하기 위한 Composable
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

/**
 * 로그인된 사용자 정보를 표시하는 카드 Composable
 */
@Composable
private fun UserInfoCard(user: SignedInUser, onSignOut: () -> Unit, onRevoke: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = "프로필 사진",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(image = Icons.Default.AccountCircle)
                )
                Spacer(modifier = Modifier.size(16.dp))
                Column {
                    Text(user.displayName ?: "이름 없음", fontWeight = FontWeight.Bold)
                    Text(user.email ?: "이메일 없음", style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Button(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("로그아웃")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onRevoke,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("연동해제")
                }
            }
        }
    }
}