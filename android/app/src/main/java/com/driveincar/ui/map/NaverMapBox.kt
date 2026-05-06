package com.driveincar.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.naver.maps.geometry.LatLng as NaverLatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapOptions

/**
 * Apex Lines 의 NCP 커스텀 스타일 ID. NCP Console > Maps > Style 에서 발급됨.
 * 버전(20260506193245)은 SDK 가 자동으로 latest 를 가져와서 별도 인자 불필요.
 */
const val NAVER_MAP_CUSTOM_STYLE_ID = "e4752a78-1cac-41ce-938e-43c2e4f075c6"

/**
 * 대한민국 카메라 경계.
 *  - 남: 33.0° (제주 남단 + 마라도 여유)
 *  - 북: 38.7° (휴전선 약간 위)
 *  - 서: 124.6° (백령도 서단)
 *  - 동: 131.9° (독도 동단)
 *
 * Naver Maps SDK 는 한반도 외 타일을 본래 서비스하지 않으므로 영역 제한 = "대한민국만 호출".
 * extent 는 사용자가 카메라를 한반도 밖으로 끌고 가서 빈 화면을 보는 UX 안전망.
 */
val KOREA_BOUNDS: LatLngBounds = LatLngBounds(
    NaverLatLng(33.0, 124.6),
    NaverLatLng(38.7, 131.9),
)

/**
 * MapView 를 Compose 에 그대로 끼워넣는 래퍼.
 *
 * Naver Maps Android SDK 는 Compose 통합이 없어서 MapView 를 직접 lifecycle-aware 하게
 * 다뤄야 한다. 이 래퍼가 그 책임을 캡슐화:
 *   - remember 로 MapView 를 한 번만 만들고 onCreate(null) 호출
 *   - 화면 lifecycle 에 onStart / onResume / onPause / onStop / onDestroy 를 묶어 forward
 *   - getMapAsync 콜백이 떨어지면 onMapReady 를 부르고, 첫 타일 로드 시점에 onMapLoaded
 *   - 모든 NaverMap 인스턴스에 커스텀 스타일 ID 와 대한민국 extent 를 일괄 적용
 *
 * 호출자는 onMapReady 에서 NaverMap 레퍼런스를 받아 markers/polylines 를 imperative 로
 * 추가하면 된다.
 */
@Composable
fun NaverMapBox(
    modifier: Modifier = Modifier,
    options: NaverMapOptions = remember {
        // 커스텀 스타일이 다크/라이트 정책을 결정 — nightModeEnabled 는 명시 X
        NaverMapOptions().customStyleId(NAVER_MAP_CUSTOM_STYLE_ID)
    },
    /** 카메라 경계 + min/max zoom 을 고정. false 면 호출자가 직접 제어. */
    restrictToKorea: Boolean = true,
    onMapReady: (NaverMap) -> Unit = {},
    onMapLoaded: () -> Unit = {},
) {
    val context = LocalContext.current
    val mapView = remember(options) {
        MapView(context, options).also { it.onCreate(null) }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var ready by remember { mutableStateOf<NaverMap?>(null) }
    LaunchedEffect(mapView) {
        mapView.getMapAsync { map ->
            if (restrictToKorea) {
                map.extent = KOREA_BOUNDS
                map.minZoom = 6.0
                map.maxZoom = 18.0
            }
            ready = map
            onMapReady(map)
            map.addOnLoadListener { onMapLoaded() }
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
    )
}
