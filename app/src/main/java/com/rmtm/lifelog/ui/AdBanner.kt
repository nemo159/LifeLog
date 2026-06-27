package com.rmtm.lifelog.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * [Google AdMob 배너 광고 Composable]
 * 앱 화면 하단 등에 배치할 배너 광고 영역을 생성합니다.
 *
 * @param modifier 레이아웃 수정을 위한 Modifier
 * @param adUnitId 배너 광고 단위 ID (기본값은 AdMob 제공 테스트용 배너 ID)
 */
@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-7199100284916551/6167029077"
    // Google AdMob 테스트 배너 ID: ca-app-pub-3940256099942544/6300978111
    // Google AdMob 운영 배너 ID: ca-app-pub-7199100284916551/6167029077
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                setAdUnitId(adUnitId)
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
