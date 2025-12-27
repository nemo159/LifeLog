package com.rmtm.lifelog.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import androidx.hilt.work.HiltWorker

/**
 * [BackupWorker]
 * - 자동 백업/정리 같은 백그라운드 작업은
 *   targetSdk 36 환경에서도 WorkManager가 가장 안전한 기본값입니다.
 *
 * - MVP 단계에서는 “스텁(빈 구현)”으로 두고,
 *   추후 DB 파일 + 사진 폴더를 ZIP으로 묶어 내부 저장소에 저장하는 방식으로 확장하면 됩니다.
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
