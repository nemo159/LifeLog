package com.rmtm.lifelog.feature.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.rmtm.lifelog.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private val app: Application
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
 */
data class SettingsUiState(
    val signedInUser: SignedInUser? = null,
    val appVersionName: String = "",
    val isLoading: Boolean = false
)

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
