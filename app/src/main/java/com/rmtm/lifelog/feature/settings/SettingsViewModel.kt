package com.rmtm.lifelog.feature.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.drive.model.File
import com.rmtm.lifelog.BuildConfig
import com.rmtm.lifelog.data.local.db.AppDatabase
import com.rmtm.lifelog.data.remote.GoogleDriveService
import com.rmtm.lifelog.util.ZipManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
 * - 구글 로그인/로그아웃 처리
 * - 테마 변경
 * - 백업/복원 로직 호출
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val app: Application,
    private val googleDriveService: GoogleDriveService,
    private val appDatabase: AppDatabase
) : ViewModel() {

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
            val account = GoogleSignIn.getLastSignedInAccount(app)
            if (account != null) {
                _uiState.update {
                    it.copy(signedInUser = account.toSignedInUser())
                }
            }
        }
    }

    /**
     * Google 로그인 결과를 처리합니다.
     * @param account 성공한 경우의 GoogleSignInAccount, 실패 시 null.
     */
    fun onSignInResult(account: GoogleSignInAccount?) {
        if (account == null) {
            // 로그인 실패/취소 시 특별한 처리는 하지 않음
            return
        }
        _uiState.update {
            it.copy(signedInUser = account.toSignedInUser())
        }
    }

    /**
     * Google 로그아웃을 처리합니다.
     */
    fun signOut() {
        _uiState.update {
            it.copy(signedInUser = null)
        }
    }

    /**
     * 데이터베이스와 사진을 포함한 전체 데이터를 구글 드라이브에 백업합니다.
     */
    fun backup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val account = GoogleSignIn.getLastSignedInAccount(app)
            if (account == null) {
                _uiState.update { it.copy(isLoading = false, backupEvent = BackupEvent.Failure("로그인 정보가 없습니다.")) }
                return@launch
            }

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
                    val result = googleDriveService.uploadBackup(account, backupZipFile)

                    // 임시 파일 삭제
                    backupZipFile.delete()

                    if (result.isFailure) {
                        throw result.exceptionOrNull() ?: Exception("알 수 없는 오류")
                    }
                }
                _uiState.update { it.copy(isLoading = false, backupEvent = BackupEvent.Success) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, backupEvent = BackupEvent.Failure(e.message ?: "알 수 없는 오류")) }
            }
        }
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
            _uiState.update { it.copy(isLoading = true) }
            val account = GoogleSignIn.getLastSignedInAccount(app)
            if (account == null) {
                _uiState.update { it.copy(isLoading = false, restoreEvent = RestoreEvent.Failure("로그인 정보가 없습니다.")) }
                return@launch
            }
            val result = googleDriveService.getBackupFiles(account)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    backupFiles = result.getOrNull()
                )
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
     * 선택된 파일을 복원합니다.
     */
    fun restore(fileId: String, backupFileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, backupFiles = null) }
            val account = GoogleSignIn.getLastSignedInAccount(app)
            if (account == null) {
                _uiState.update { it.copy(isLoading = false, restoreEvent = RestoreEvent.Failure("로그인 정보가 없습니다.")) }
                return@launch
            }

            try {
                withContext(Dispatchers.IO) {
                    val tempZipFile = JavaFile(app.cacheDir, backupFileName)
                    val tempUnzipDir = JavaFile(app.cacheDir, "restore_temp")

                    // 1. 드라이브에서 임시 zip 파일 다운로드
                    googleDriveService.downloadBackup(account, fileId, tempZipFile).getOrThrow()

                    // 2. 임시 폴더에 압축 해제
                    if (tempUnzipDir.exists()) tempUnzipDir.deleteRecursively()
                    tempUnzipDir.mkdirs()
                    ZipManager.unzip(tempZipFile.absolutePath, tempUnzipDir)

                    // 3. DB 닫기 (파일 접근 해제)
                    appDatabase.close()

                    // 4. 기존 데이터 디렉터리 전체 삭제
                    val dbDir = app.getDatabasePath(AppDatabase.DATABASE_NAME).parentFile
                    val imagesDir = JavaFile(app.filesDir, "images")
                    dbDir?.deleteRecursively()
                    imagesDir.deleteRecursively()
                    dbDir?.mkdirs()
                    imagesDir.mkdirs()

                    // 5. 압축 해제된 모든 파일/폴더를 올바른 위치로 복사
                    val unzippedContents = tempUnzipDir.listFiles() ?: emptyArray()
                    var dbFileFound = false
                    for (unzippedFile in unzippedContents) {
                        if (unzippedFile.name.startsWith(AppDatabase.DATABASE_NAME)) {
                            unzippedFile.copyTo(JavaFile(dbDir, unzippedFile.name), true)
                            if(unzippedFile.name == AppDatabase.DATABASE_NAME) dbFileFound = true
                        } else if (unzippedFile.isDirectory && unzippedFile.name == "images") {
                            unzippedFile.copyRecursively(imagesDir, true)
                        }
                    }

                    if (!dbFileFound) {
                        throw Exception("백업 파일에 데이터베이스 파일이 없습니다.")
                    }

                    // 6. 임시 파일/폴더 삭제
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


    private fun GoogleSignInAccount.toSignedInUser() = SignedInUser(
        displayName = this.displayName,
        email = this.email,
        photoUrl = this.photoUrl?.toString()
    )
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
    val backupFiles: List<File>? = null
)

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
    val photoUrl: String?
)
