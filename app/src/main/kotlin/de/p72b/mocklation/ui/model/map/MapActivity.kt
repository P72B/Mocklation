package de.p72b.mocklation.ui.model.map

import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.DragState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import de.p72b.mocklation.data.Node
import de.p72b.mocklation.ui.model.collection.CollectionUIState
import de.p72b.mocklation.ui.model.collection.DataCollectionScreen
import de.p72b.mocklation.ui.model.collection.EmptyCollectionScreen
import de.p72b.mocklation.ui.model.collection.LoadingCollectionScreen
import de.p72b.mocklation.ui.model.requirements.RequirementsUIState
import de.p72b.mocklation.util.Logger
import org.koin.androidx.compose.koinViewModel


val singapore = LatLng(1.3588227, 103.8742114)
val defaultCameraPosition = CameraPosition.fromLatLngZoom(singapore, 11f)

class MapActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            var isMapLoaded by remember { mutableStateOf(false) }
            val cameraPositionState = rememberCameraPositionState {
                position = defaultCameraPosition
            }

            Box(Modifier.fillMaxSize()) {
                GoogleMapView(
                    cameraPositionState = cameraPositionState,
                    onMapLoaded = {
                        isMapLoaded = true
                    },
                )
                if (!isMapLoaded) {
                    AnimatedVisibility(
                        modifier = Modifier
                            .matchParentSize(),
                        visible = !isMapLoaded,
                        enter = EnterTransition.None,
                        exit = fadeOut()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .wrapContentSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleMapView(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = koinViewModel(),
    cameraPositionState: CameraPositionState = rememberCameraPositionState(),
    onMapLoaded: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    val markerData = mutableListOf<Pair<Node, MarkerState>>()
    val items by viewModel.uiState.collectAsStateWithLifecycle()
    when (items) {
        is MapUIState.FeatureData -> {
            val status = items as MapUIState.FeatureData
            markerData.clear()
            status.feature.nodes.forEach {
                val latLng = LatLng(it.geometry.latLng.latitude, it.geometry.latLng.longitude)
                markerData.add(Pair(it, rememberMarkerState(position = latLng)))
            }
        }
    }

    val singaporeState = rememberMarkerState(position = singapore)
    var circleCenter by remember { mutableStateOf(singapore) }
    if (singaporeState.dragState == DragState.END) {
        circleCenter = singaporeState.position
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
            Logger.d(msg = "${it.title} was clicked")
            false
        }
        markerData.forEach {
            Marker(
                state = it.second,
                onClick = markerClick,
                draggable = true,
            )
        }
        content()
    }
}
