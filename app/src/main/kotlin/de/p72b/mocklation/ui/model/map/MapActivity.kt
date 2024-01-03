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
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.p72b.mocklation.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
class MapActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val sheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.PartiallyExpanded
            )
            val scaffoldState = rememberBottomSheetScaffoldState(
                bottomSheetState = sheetState
            )
            var isMapLoaded by remember { mutableStateOf(false) }

            AppTheme {
                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetContent = {
                        MapBottomSheet()
                    },
                    sheetPeekHeight = 0.dp
                ) {
                    Box(
                        Modifier.fillMaxSize()
                    ) {
                        GoogleMapView(
                            onMapLoaded = {
                                isMapLoaded = true
                            },
                            bottomSheetState = sheetState
                        )
                        if (isMapLoaded.not()) {
                            AnimatedVisibility(
                                modifier = Modifier
                                    .matchParentSize(),
                                visible = !isMapLoaded,
                                enter = EnterTransition.None,
                                exit = fadeOut()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.background)
                                        .wrapContentSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
