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
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapOptions

/**
 * MapView 를 Compose 에 그대로 끼워넣는 래퍼.
 *
 * Naver Maps Android SDK 는 Compose 통합이 없어서 MapView 를 직접 lifecycle-aware 하게
 * 다뤄야 한다. 이 래퍼가 그 책임을 캡슐화:
 *   - remember 로 MapView 를 한 번만 만들고 onCreate(null) 호출
 *   - 화면 lifecycle 에 onStart / onResume / onPause / onStop / onDestroy 를 묶어 forward
 *   - getMapAsync 콜백이 떨어지면 onMapReady 를 부르고, 첫 타일 로드 시점에 onMapLoaded
 *
 * 호출자는 onMapReady 에서 NaverMap 레퍼런스를 받아 markers/polylines 를 imperative 로
 * 추가하면 된다 (overlay 들의 .map = naverMap 패턴). 변경되는 데이터(코스 목록, 라이브
 * 트랙 등) 는 LaunchedEffect 안에서 diff 적용.
 */
@Composable
fun NaverMapBox(
    modifier: Modifier = Modifier,
    options: NaverMapOptions = remember { NaverMapOptions().nightModeEnabled(true) },
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
