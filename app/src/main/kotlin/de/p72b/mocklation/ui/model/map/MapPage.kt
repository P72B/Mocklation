package de.p72b.mocklation.ui.model.map

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.DragState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import de.p72b.mocklation.data.Node
import de.p72b.mocklation.util.Logger
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

const val HALF_OFFSET = 0.5f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleMapView(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = koinViewModel(),
    countryCode: String?,
    cameraPositionState: CameraPositionState = rememberCameraPositionState {
        viewModel.getDefaultMapCameraLocation(countryCode).let {
            position = CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), 5f)
        }
    },
    onMapLoaded: () -> Unit = {},
    content: @Composable () -> Unit = {},
    bottomSheetState: SheetState
) {

    Logger.d(msg = "New GoogleMapView")
    val scope = rememberCoroutineScope()
    val markerData = mutableListOf<Pair<Node, MarkerState>>()
    val items by viewModel.uiState.collectAsStateWithLifecycle()
    when (items) {
        is MapUIState.FeatureDataUpdate -> {
            val status = items as MapUIState.FeatureDataUpdate
            //markerData.clear()
            status.feature.nodes.forEach {
                val latLng = LatLng(it.geometry.latitude, it.geometry.longitude)
                val state = rememberMarkerState(
                    key = it.id.toString(),
                    position = latLng
                )
                LaunchedEffect(key1 = state.position) {
                    if (state.dragState == DragState.END) {
                        viewModel.onMarkerDragEnd(it, state.position.latitude, state.position.longitude)
                    }
                }
                markerData.add(Pair(it, state))
            }
            status.selectedId.let {
                scope.launch {
                    if (it == null) {
                        bottomSheetState.hide()
                    } else if (bottomSheetState.isVisible) {
                        bottomSheetState.expand()
                    }
                }
            }
            var cameraUpdate: CameraUpdate? = null
            status.cameraUpdate.bounds?.let {
                val southWest = LatLng(it.southWestLatitude, it.southWestLongitude)
                val northEast = LatLng(it.northEastLatitude, it.northEastLongitude)
                val latLngBounds = LatLngBounds(southWest, northEast)
                cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 100)
            }
            status.cameraUpdate.point?.let {
                val center = LatLng(it.centerLatitude, it.centerLongitude)
                cameraUpdate = CameraUpdateFactory.newLatLng(center)
            }
            cameraUpdate?.let {
                if (status.cameraUpdate.animate) {
                    LaunchedEffect(key1 = it) {
                        cameraPositionState.animate(update = it, durationMs = 500)
                    }
                } else {
                    cameraPositionState.move(it)
                }
            }
        }

        else -> {

        }
    }

    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                compassEnabled = true,
                zoomControlsEnabled = false
            )
        )
    }
    val mapProperties by remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.NORMAL
            )
        )
    }
    Logger.d(msg = "New GoogleMap")

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = uiSettings,
        onMapLoaded = onMapLoaded,
        onMapClick = {
            viewModel.onMapClick(it.latitude, it.longitude)
        },
        onMapLongClick = {
            viewModel.onMapLongClick(it.latitude, it.longitude)
        }
    ) {
        var i = 0
        markerData.forEach {
            Logger.d(msg = "Add marker: ${it.first.id} at ${it.first.geometry.latitude}/${it.first.geometry.longitude}")
            val icon = remember { drawCanvasMarkerIcon(i.toString()) }
            i++
            Marker(
                tag = it.first,
                state = it.second,
                onClick = {
                    viewModel.onNodeClicked(it.tag as Node)
                    false
                },
                icon = icon,
                draggable = true,
                anchor = Offset(HALF_OFFSET, HALF_OFFSET)
            )
        }
        content()
    }
}

private fun drawCanvasMarkerIcon(name: String): BitmapDescriptor {
    val paint = Paint().apply {
        isAntiAlias = true
        color = Color.Red.hashCode()
        style = Paint.Style.FILL
    }
    val bm = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bm)
    canvas.drawCircle(50f, 50f, 20f, paint)
    return BitmapDescriptorFactory.fromBitmap(bm)
}