package com.rmtm.lifelog.feature.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
                Toast.makeText(context, "복원이 완료되었습니다. 앱을 재시작하세요.", Toast.LENGTH_LONG).show()
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

    uiState.backupFiles?.let { files ->
        BackupFilesDialog(
            files = files,
            onDismiss = { viewModel.clearBackupFiles() },
            onFileSelected = { fileId ->
                viewModel.restore(fileId)
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
                            onSignOut = {
                                googleSignInClient.signOut().addOnCompleteListener {
                                    viewModel.signOut()
                                }
                            }
                        )
                    }
                }

                if (uiState.signedInUser != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    // 데이터 섹션
                    SettingsSection(title = "데이터") {
                        Button(
                            onClick = {
                                val account = GoogleSignIn.getLastSignedInAccount(context)
                                if (account != null) {
                                    val driveScope =
                                        Scope("https://www.googleapis.com/auth/drive.file")
                                    val hasPermission =
                                        GoogleSignIn.hasPermissions(account, driveScope)
                                    if (hasPermission) {
                                        viewModel.backup()
                                    } else {
                                        signInLauncher.launch(googleSignInClient.signInIntent)
                                    }
                                }
                            },
                            enabled = !uiState.isLoading
                        ) {
                            Text("백업")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val account = GoogleSignIn.getLastSignedInAccount(context)
                                if (account != null) {
                                    val driveScope =
                                        Scope("https://www.googleapis.com/auth/drive.file")
                                    val hasPermission =
                                        GoogleSignIn.hasPermissions(account, driveScope)
                                    if (hasPermission) {
                                        viewModel.listBackupFiles()
                                    } else {
                                        signInLauncher.launch(googleSignInClient.signInIntent)
                                    }
                                }
                            },
                            enabled = !uiState.isLoading
                        ) {
                            Text("복원")
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
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun BackupFilesDialog(
    files: List<File>,
    onDismiss: () -> Unit,
    onFileSelected: (String) -> Unit
) {
    val format = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

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
                            .clickable { onFileSelected(file.id) }
                            .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "수정된 날짜: ${format.format(Date(file.modifiedTime.value))}",
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
private fun UserInfoCard(user: SignedInUser, onSignOut: () -> Unit) {
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
            Button(onClick = onSignOut) {
                Text("로그아웃")
            }
        }
    }
}
