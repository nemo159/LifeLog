package com.rmtm.lifelog.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import androidx.hilt.work.HiltWorker

/**
 * [백그라운드 작업: 백업]
 * 앱이 꺼져있을 때도 뒤에서 몰래 할 일을 정의합니다.
 * - 나중에 '데이터 백업' 기능을 만들기 위해 준비해둔 파일입니다.
 */
@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // TODO: V2에서 구현
        // 1) lifelog.db 파일 export (Room export or 파일 접근)
        // 2) 사진 캐시 폴더 포함
        // 3) ZIP으로 묶기 + 최근 N개 유지
        return Result.success()
    }
}
