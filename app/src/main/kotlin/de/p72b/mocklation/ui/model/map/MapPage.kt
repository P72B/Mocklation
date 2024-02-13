package de.p72b.mocklation.ui.model.map

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import de.p72b.mocklation.data.Node
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

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
    val scope = rememberCoroutineScope()
    val markerData = mutableListOf<Pair<Node, MarkerState>>()
    val items by viewModel.uiState.collectAsStateWithLifecycle()
    when (items) {
        is MapUIState.FeatureData -> {
            val status = items as MapUIState.FeatureData
            //markerData.clear()
            status.feature.nodes.forEach {
                val latLng = LatLng(it.geometry.latLng.latitude, it.geometry.latLng.longitude)
                markerData.add(Pair(it, rememberMarkerState(position = latLng)))
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

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = uiSettings,
        onMapLoaded = onMapLoaded,
        onMapLongClick = {
            viewModel.onMapLongClick(it.latitude, it.longitude)
        }
    ) {
        val markerClick: (Marker) -> Boolean = {
            viewModel.onNodeClicked(it.tag as Node)
            false
        }
        markerData.forEach {
            Marker(
                tag = it.first,
                state = it.second,
                onClick = markerClick,
                draggable = true,
            )
        }
        content()
    }
}