package com.rmtm.lifelog.feature.settings

import android.app.Application
import android.content.Intent
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.android.gms.auth.UserRecoverableAuthException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.api.services.drive.model.File
import com.rmtm.lifelog.BuildConfig
import com.rmtm.lifelog.data.local.db.AppDatabase
import com.rmtm.lifelog.data.remote.GoogleDriveService
import com.rmtm.lifelog.util.ZipManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File as JavaFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


/**
 * [설정 화면 뷰모델]
 * 설정 화면의 상태와 비즈니스 로직을 관리합니다.
 * - 구글 로그인/로그아웃 처리 (Credential Manager)
 * - 테마 변경
 * - 백업/복원 로직 호출
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val app: Application,
    private val googleDriveService: GoogleDriveService,
    private val appDatabase: AppDatabase,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_PHOTO = stringPreferencesKey("user_photo")
        private val KEY_USER_ID_TOKEN = stringPreferencesKey("user_id_token")
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkCurrentUser()
        loadAppVersion()
    }

    /**
     * 앱의 버전 정보를 불러옵니다.
     */
    private fun loadAppVersion() {
        _uiState.update { it.copy(appVersionName = BuildConfig.VERSION_NAME) }
    }

    /**
     * 앱 시작 시 현재 로그인된 사용자가 있는지 확인합니다.
     */
    private fun checkCurrentUser() {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            val email = prefs[KEY_USER_EMAIL]
            if (email != null) {
                _uiState.update {
                    it.copy(
                        signedInUser = SignedInUser(
                            displayName = prefs[KEY_USER_NAME],
                            email = email,
                            photoUrl = prefs[KEY_USER_PHOTO],
                            idToken = prefs[KEY_USER_ID_TOKEN]
                        )
                    )
                }
            }
        }
    }

    /**
     * 로그인 성공 시 사용자 정보를 저장합니다.
     */
    fun onSignInSuccess(user: SignedInUser) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_USER_NAME] = user.displayName ?: ""
                prefs[KEY_USER_EMAIL] = user.email ?: ""
                prefs[KEY_USER_PHOTO] = user.photoUrl ?: ""
                prefs[KEY_USER_ID_TOKEN] = user.idToken ?: ""
            }
            _uiState.update { it.copy(signedInUser = user) }
        }
    }

    /**
     * 로그아웃을 처리합니다.
     */
    fun signOut() {
        viewModelScope.launch {
            dataStore.edit { it.clear() }
            _uiState.update { it.copy(signedInUser = null) }
        }
    }

    /**
     * 구글 계정 연동을 완전히 해제합니다.
     */
    fun revokeAccess(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val email = _uiState.value.signedInUser?.email
            if (email.isNullOrEmpty()) {
                signOut()
                onComplete(true)
                return@launch
            }

            val result = withContext(Dispatchers.IO) {
                try {
                    // 1. 드라이브 API 등에서 사용하는 실제 서비스 토큰(Access Token) 획득
                    val credential = com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential.usingOAuth2(
                        app, listOf("https://www.googleapis.com/auth/drive.file")
                    ).also { it.selectedAccount = android.accounts.Account(email, "com.google") }

                    val accessToken = credential.token // 이 과정에서 네트워크 통신 발생

                    if (accessToken.isNullOrEmpty()) {
                        android.util.Log.e("SettingsViewModel", "Failed to get access token for revoke")
                        return@withContext false
                    }

                    // 2. 획득한 Access Token으로 구글 서버에 연동 해제 요청
                    val url = java.net.URL("https://oauth2.googleapis.com/revoke?token=$accessToken")
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    
                    val responseCode = connection.responseCode
                    android.util.Log.d("SettingsViewModel", "Server revoke response: $responseCode")
                    
                    responseCode == 200
                } catch (e: Exception) {
                    android.util.Log.e("SettingsViewModel", "Critical revoke error: ${e.message}")
                    e.printStackTrace()
                    false
                }
            }
            
            // 3. 성공 여부와 상관없이 앱 내 세션 삭제 (사용자가 다시 로그인하게 함)
            signOut()
            onComplete(result)
        }
    }

    /**
     * 데이터베이스와 사진을 포함한 전체 데이터를 구글 드라이브에 백업합니다.
     */
    fun backup() {
        viewModelScope.launch {
            val user = _uiState.value.signedInUser
            if (user?.email == null) {
                _uiState.update { it.copy(backupEvent = BackupEvent.Failure("로그인 정보가 없습니다.")) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            try {
                withContext(Dispatchers.IO) {
                    val dbPath = app.getDatabasePath(AppDatabase.DATABASE_NAME)
                    if (!dbPath.exists()) {
                        throw Exception("백업할 데이터가 없습니다. 먼저 기록을 작성해주세요.")
                    }

                    // DB 관련 파일(-wal, -shm 포함) 모두 가져오기
                    val dbDir = dbPath.parentFile
                    val dbFiles = dbDir?.listFiles { _, name ->
                        name.startsWith(AppDatabase.DATABASE_NAME)
                    }?.toList() ?: emptyList()

                    val imagesDir = JavaFile(app.filesDir, "images")
                    val sourcesToZip = dbFiles + listOfNotNull(if (imagesDir.exists()) imagesDir else null)

                    if (sourcesToZip.isEmpty()) {
                        throw Exception("백업할 파일이 없습니다.")
                    }

                    // 임시 zip 파일 생성
                    val timestamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(Date())
                    val backupFileName = "lifelog-backup_${timestamp}.zip"
                    val backupZipFile = JavaFile(app.cacheDir, backupFileName)

                    ZipManager.zip(sourcesToZip, backupZipFile.absolutePath)

                    // 구글 드라이브에 업로드
                    val result = googleDriveService.uploadBackup(user.email, backupZipFile)

                    // 임시 파일 삭제
                    backupZipFile.delete()

                    if (result.isFailure) {
                        throw result.exceptionOrNull() ?: Exception("알 수 없는 오류")
                    }
                }
                _uiState.update { it.copy(isLoading = false, backupEvent = BackupEvent.Success) }
            } catch (e: Exception) {
                val cause = e.cause
                if (e is UserRecoverableAuthIOException) {
                    _uiState.update { it.copy(isLoading = false, permissionIntent = e.intent) }
                } else if (cause is UserRecoverableAuthException) {
                    _uiState.update { it.copy(isLoading = false, permissionIntent = cause.intent) }
                } else {
                    _uiState.update { it.copy(isLoading = false, backupEvent = BackupEvent.Failure(e.message ?: "알 수 없는 오류")) }
                }
            }
        }
    }

    /**
     * 권한 요청 이벤트를 소비합니다.
     */
    fun consumePermissionRequest() {
        _uiState.update { it.copy(permissionIntent = null) }
    }


    /**
     * 백업 이벤트를 소비(확인) 처리합니다.
     */
    fun consumeBackupEvent() {
        _uiState.update { it.copy(backupEvent = null) }
    }

    /**
     * Google Drive에서 백업 파일 목록을 가져옵니다.
     */
    fun listBackupFiles() {
        viewModelScope.launch {
            val user = _uiState.value.signedInUser
            if (user?.email == null) {
                _uiState.update { it.copy(restoreEvent = RestoreEvent.Failure("로그인 정보가 없습니다.")) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            val result = googleDriveService.getBackupFiles(user.email)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        backupFiles = result.getOrNull()
                    )
                }
            } else {
                val e = result.exceptionOrNull()
                if (e is UserRecoverableAuthIOException) {
                    _uiState.update { it.copy(isLoading = false, permissionIntent = e.intent) }
                } else {
                    _uiState.update { it.copy(isLoading = false, restoreEvent = RestoreEvent.Failure(e?.message ?: "목록 로드 실패")) }
                }
            }
        }
    }

    /**
     * 백업 파일 목록을 UI에서 닫을 때 호출합니다.
     */
    fun clearBackupFiles() {
        _uiState.update { it.copy(backupFiles = null) }
    }

    /**
     * 권한 요청이 필요한 액션을 설정합니다.
     */
    fun setPendingAction(action: PendingAction?) {
        _uiState.update { it.copy(showPermissionAlertFor = action) }
    }

    /**
     * 선택된 파일을 복원합니다.
     */
    fun restore(fileId: String, backupFileName: String) {
        viewModelScope.launch {
            val user = _uiState.value.signedInUser
            if (user?.email == null) {
                _uiState.update { it.copy(restoreEvent = RestoreEvent.Failure("로그인 정보가 없습니다.")) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, backupFiles = null) }
            try {
                withContext(Dispatchers.IO) {
                    val tempZipFile = JavaFile(app.cacheDir, backupFileName)
                    val tempUnzipDir = JavaFile(app.cacheDir, "restore_temp")

                    // 1. 드라이브에서 임시 zip 파일 다운로드
                    googleDriveService.downloadBackup(user.email, fileId, tempZipFile).getOrThrow()

                    // 2. 임시 폴더에 압축 해제
                    if (tempUnzipDir.exists()) tempUnzipDir.deleteRecursively()
                    tempUnzipDir.mkdirs()
                    ZipManager.unzip(tempZipFile.absolutePath, tempUnzipDir)

                    // 3. DB 닫기 (파일 접근 해제)
                    appDatabase.close()

                    // 4. 기존 데이터 정리
                    val dbFile = app.getDatabasePath(AppDatabase.DATABASE_NAME)
                    JavaFile(dbFile.path).delete()
                    JavaFile(dbFile.path + "-wal").delete()
                    JavaFile(dbFile.path + "-shm").delete()

                    val imagesDir = JavaFile(app.filesDir, "images")
                    if (imagesDir.exists()) {
                        imagesDir.deleteRecursively()
                    }
                    imagesDir.mkdirs()

                    // 5. 압축 해제된 모든 파일/폴더를 복사
                    val unzippedContents = tempUnzipDir.listFiles() ?: emptyArray()
                    var dbFileFound = false
                    for (unzippedFile in unzippedContents) {
                        if (unzippedFile.name.startsWith(AppDatabase.DATABASE_NAME)) {
                            unzippedFile.copyTo(JavaFile(dbFile.parentFile, unzippedFile.name), true)
                            if(unzippedFile.name == AppDatabase.DATABASE_NAME) dbFileFound = true
                        } else if (unzippedFile.isDirectory && unzippedFile.name == "images") {
                            unzippedFile.copyRecursively(imagesDir, true)
                        }
                    }

                    if (!dbFileFound) {
                        throw Exception("백업 파일에 데이터베이스 파일이 없습니다.")
                    }

                    // 6. 임시 파일 삭제
                    tempZipFile.delete()
                    tempUnzipDir.deleteRecursively()
                }
                _uiState.update { it.copy(isLoading = false, restoreEvent = RestoreEvent.Success) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, restoreEvent = RestoreEvent.Failure(e.message ?: "알 수 없는 오류")) }
            }
        }
    }


    /**
     * 복원 이벤트를 소비(확인) 처리합니다.
     */
    fun consumeRestoreEvent() {
        _uiState.update { it.copy(restoreEvent = null) }
    }
}

/**
 * 설정 화면의 UI 상태
 * @param signedInUser 현재 로그인된 사용자 정보. null이면 로그아웃 상태.
 * @param appVersionName 앱의 버전 이름.
 * @param isLoading 백업/복원 등 오래 걸리는 작업 진행 상태.
 * @param backupEvent 백업 작업의 결과를 나타내는 이벤트.
 * @param restoreEvent 복원 작업의 결과를 나타내는 이벤트.
 * @param backupFiles Google Drive에 있는 백업 파일 목록.
 */
data class SettingsUiState(
    val signedInUser: SignedInUser? = null,
    val appVersionName: String = "",
    val isLoading: Boolean = false,
    val backupEvent: BackupEvent? = null,
    val restoreEvent: RestoreEvent? = null,
    val backupFiles: List<File>? = null,
    val showPermissionAlertFor: PendingAction? = null,
    val permissionIntent: Intent? = null // 추가: 권한 승인을 위한 Intent
)

enum class PendingAction {
    BACKUP, RESTORE
}

/**
 * 백업 작업의 결과를 나타내는 sealed class
 */
sealed class BackupEvent {
    object Success : BackupEvent()
    data class Failure(val message: String) : BackupEvent()
}

/**
 * 복원 작업의 결과를 나타내는 sealed class
 */
sealed class RestoreEvent {
    object Success : RestoreEvent()
    data class Failure(val message: String) : RestoreEvent()
}


/**
 * 로그인된 사용자 정보
 * @param displayName 표시될 이름
 * @param email 이메일
 * @param photoUrl 프로필 사진 URL
 */
data class SignedInUser(
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
    val idToken: String? = null
)
