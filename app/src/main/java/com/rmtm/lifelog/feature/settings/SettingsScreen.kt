package com.rmtm.lifelog.feature.settings

import android.app.Activity
import android.util.Log
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.api.services.drive.model.File
import com.rmtm.lifelog.R
import com.rmtm.lifelog.ui.theme.ThemeMode
import com.rmtm.lifelog.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

/**
 * [설정 화면]
 * 앱의 여러 기능들을 설정하는 화면입니다.
 * - 구글 로그인 (Credential Manager)
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
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showRestoreSuccessDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmationDialog by remember { mutableStateOf(false) }
    var showBackupConfirmationDialog by remember { mutableStateOf(false) }
    var showRevokeConfirmationDialog by remember { mutableStateOf(false) }
    var showSignOutConfirmationDialog by remember { mutableStateOf(false) }

    val credentialManager = remember { CredentialManager.create(context) }

    // 추가: 구글 드라이브 권한 승인을 위한 Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 권한이 승인됨. 사용자가 이전에 하려던 동작(백업/복원)을 다시 유도하기 위해 토스트 알림
            Toast.makeText(context, "권한이 승인되었습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "권한 승인이 거부되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Credential Manager를 사용하여 Google 로그인을 수행합니다.
     */
    fun onGoogleSignIn() {
        val serverClientId = context.getString(R.string.google_web_client_id)
        Log.d("SettingsScreen", "Using serverClientId: $serverClientId")
        
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false) // 사용자가 계정을 직접 선택하도록 유도
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.onSignInSuccess(
                        SignedInUser(
                            displayName = googleIdTokenCredential.displayName,
                            email = googleIdTokenCredential.id,
                            photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                            idToken = googleIdTokenCredential.idToken
                        )
                    )
                }
            } catch (e: GetCredentialException) {
                Log.e("SettingsScreen", "Credential Manager Error Type: ${e::class.java.simpleName}")
                Log.e("SettingsScreen", "Error Message: ${e.message}")
                
                val errorMessage = when (e) {
                    is NoCredentialException -> "사용 가능한 계정이 없습니다. 구글 콘솔에서 '웹 클라이언트 ID'를 사용했는지, SHA-1이 등록되었는지 확인하세요."
                    is GetCredentialCancellationException -> "로그인이 취소되었습니다."
                    else -> "로그인 실패 (${e::class.java.simpleName}): ${e.message}"
                }
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(uiState.permissionIntent) {
        uiState.permissionIntent?.let { intent ->
            permissionLauncher.launch(intent)
            viewModel.consumePermissionRequest()
        }
    }

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
                viewModel.revokeAccess { success ->
                    // 이제 revokeAccess는 항상 true를 반환하지만, 서버측 결과는 로그로만 남김
                    Toast.makeText(context, "계정 연동이 해제되었습니다.", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showRevokeConfirmationDialog = false }
        )
    }

    if (showSignOutConfirmationDialog) {
        SignOutConfirmationDialog(
            onConfirm = {
                showSignOutConfirmationDialog = false
                viewModel.signOut()
                Toast.makeText(context, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
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
                            onClick = { onGoogleSignIn() },
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
                                onClick = { showBackupConfirmationDialog = true },
                                enabled = !uiState.isLoading,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("백업")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { showRestoreConfirmationDialog = true },
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽: 사용자 정보 영역 (남은 공간 모두 차지)
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = "프로필 사진",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(image = Icons.Default.AccountCircle)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = user.displayName ?: "이름 없음",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email ?: "이메일 없음",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 오른쪽: 버튼 영역 (세로로 배치)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(100.dp) // 버튼 영역 너비 제한
            ) {
                Button(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("로그아웃", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onRevoke,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("연동해제", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}