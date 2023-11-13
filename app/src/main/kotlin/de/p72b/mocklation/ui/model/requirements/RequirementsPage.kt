package de.p72b.mocklation.ui.model.requirements

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.p72b.mocklation.R
import org.koin.androidx.compose.koinViewModel


@Composable
fun RequirementsPage(
    modifier: Modifier = Modifier,
    viewModel: RequirementsViewModel = koinViewModel()
) {
    val items by viewModel.uiState.collectAsStateWithLifecycle()
    when (items) {
        RequirementsUIState.Verifying -> VerifyingScreen(modifier)
        is RequirementsUIState.Status -> StatusScreen(
            modifier,
            items as RequirementsUIState.Status,
            viewModel::onRequestFineLocationPermissionClicked,
            viewModel::onRequestBackgroundLocationPermissionClicked,
            viewModel::onRequestNotificationPermissionClicked,
            viewModel::onGoToSettingsClicked,
            viewModel::onContinueClicked
        )
    }
}

@Composable
fun station(
    modifier: Modifier = Modifier,
    rowHeight: Dp,
    isAnyPreviousNotAvailable: Boolean = false,
    isAvailable: Boolean = false,
    isDestination: Boolean = false,
) {
    var pathEffect: PathEffect? = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    val stationRadius = 14.dp
    val strokeWidth = 2.dp
    var fillColor = Color.Gray
    var strokeColor = Color.Green
    if (isAvailable) {
        if (isAnyPreviousNotAvailable.not()) {
            pathEffect = null
        }
        fillColor = Color.Green
    }
    if (pathEffect != null) {
        strokeColor = Color.Gray
    }
    Canvas(modifier = modifier) {
        val height = rowHeight + 24.dp
        var circleRadius = stationRadius / 2
        if (isDestination.not()) {
            drawLine(
                start = Offset(0f, 0f),
                end = Offset(0f, height.toPx()),
                color = strokeColor,
                strokeWidth = strokeWidth.toPx(),
                pathEffect = pathEffect,
            )
        }
        if (isAnyPreviousNotAvailable) {
            circleRadius -= strokeWidth
            drawCircle(
                center = Offset(0f, 0f),
                color = Color.Gray,
                radius = circleRadius.toPx() + strokeWidth.toPx()
            )
        }
        drawCircle(
            center = Offset(0f, 0f),
            color = fillColor,
            radius = circleRadius.toPx()
        )
    }
}

@Composable
fun VerifyingScreen(modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = stringResource(id = R.string.verify_requirements))
        }
    }
}

@Composable
fun StatusScreen(
    modifier: Modifier = Modifier,
    items: RequirementsUIState.Status,
    onRequestFineLocationPermissionClicked: () -> Unit,
    onRequestBackgroundLocationPermissionClicked: () -> Unit,
    onRequestNotificationPermissionClicked: () -> Unit,
    onGoToSettingsClicked: () -> Unit,
    onContinueClicked: () -> Unit
) {
    var componentHeightRow1 by remember { mutableStateOf(0.dp) }
    var componentHeightRow2 by remember { mutableStateOf(0.dp) }
    var componentHeightRow3 by remember { mutableStateOf(0.dp) }
    var componentHeightRow4 by remember { mutableStateOf(0.dp) }
    var componentHeightRow5 by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    var isLineBroken = false

    fun setIsLineBroken(value: Boolean) {
        if (value.not()) {
            isLineBroken = true
        }
    }

    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .onGloballyPositioned {
                    componentHeightRow1 = with(density) {
                        it.size.height.toDp()
                    }
                },
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val height = componentHeightRow1 + ((componentHeightRow2 - componentHeightRow1) / 2)
            setIsLineBroken(items.isDeveloperOptionsEnabled)
            if (items.isDeveloperOptionsEnabled) {
                station(
                    rowHeight = height,
                    isAvailable = true,
                )
                Text(
                    text = stringResource(id = R.string.developer_options_enabled_requirements)
                )
            } else {
                station(
                    rowHeight = height
                )
                Text(
                    text = stringResource(id = R.string.developer_options_disabled_requirements)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .onGloballyPositioned {
                    componentHeightRow2 = with(density) {
                        it.size.height.toDp()
                    }
                },
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val height = componentHeightRow2 + ((componentHeightRow3 - componentHeightRow2) / 2)
            setIsLineBroken(items.isDeveloperOptionsEnabled)
            if (items.isSelectedMockLocationApp) {
                station(
                    rowHeight = height,
                    isAvailable = true,
                    isAnyPreviousNotAvailable = isLineBroken,
                )
                Text(text = stringResource(id = R.string.developer_options_selected_mock_location_app_requirements))
            } else {
                station(
                    rowHeight = height,
                    isAnyPreviousNotAvailable = isLineBroken,
                )
                Text(text = stringResource(id = R.string.developer_options_unselected_mock_location_app_requirements))
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .onGloballyPositioned {
                    componentHeightRow3 = with(density) {
                        it.size.height.toDp()
                    }
                },
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val height = componentHeightRow3 + ((componentHeightRow4 - componentHeightRow3) / 2)
            setIsLineBroken(items.isSelectedMockLocationApp)
            if (items.isAccessToFineLocationGranted) {
                station(
                    rowHeight = height,
                    isAvailable = true,
                    isAnyPreviousNotAvailable = isLineBroken,
                )
                Text(text = stringResource(id = R.string.fine_location_granted_requirements))
            } else {
                station(
                    rowHeight = height,
                    isAnyPreviousNotAvailable = isLineBroken,
                )
                Text(text = stringResource(id = R.string.fine_location_permission_missing_requirements))
                IconButton(onClick = {
                    onRequestFineLocationPermissionClicked()
                }) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = stringResource(R.string.content_description_add_fine_location_permission)
                    )
                }
                if (items.isAccessToBackgroundLocationGranted.not() && items.shouldShowDialogRequestLocationPermissionRationale) {
                    IconButton(onClick = {
                        onGoToSettingsClicked()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = stringResource(R.string.content_go_to_settings)
                        )
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .onGloballyPositioned {
                        componentHeightRow4 = with(density) {
                            it.size.height.toDp()
                        }
                    },
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val height = componentHeightRow4 + ((componentHeightRow5 - componentHeightRow4) / 2)
                setIsLineBroken(items.isAccessToFineLocationGranted)
                if (items.isAccessToBackgroundLocationGranted) {
                    station(
                        rowHeight = height,
                        isAvailable = true,
                        isAnyPreviousNotAvailable = isLineBroken,
                    )
                    Text(text = stringResource(id = R.string.background_location_granted_requirements))
                } else {
                    station(
                        rowHeight = height,
                        isAnyPreviousNotAvailable = isLineBroken,
                    )
                    Text(text = stringResource(id = R.string.background_location_permission_missing_requirements))
                    IconButton(onClick = {
                        onRequestBackgroundLocationPermissionClicked()
                    }) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = stringResource(R.string.content_description_add_background_location_permission)
                        )
                    }
                }
                if (items.isAccessToBackgroundLocationGranted.not() && items.shouldShowDialogRequestBackgroundLocationPermissionRationale) {
                    IconButton(onClick = {
                        onGoToSettingsClicked()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = stringResource(R.string.content_go_to_settings)
                        )
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .onGloballyPositioned {
                        componentHeightRow5 = with(density) {
                            it.size.height.toDp()
                        }
                    },
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                setIsLineBroken(items.isAccessToBackgroundLocationGranted)
                if (items.isAllowedToShowNotification) {
                    station(
                        rowHeight = componentHeightRow5,
                        isAvailable = true,
                        isDestination = true,
                        isAnyPreviousNotAvailable = isLineBroken,
                    )
                    Text(text = stringResource(id = R.string.enabled_show_notification_requirements))
                } else {
                    station(
                        rowHeight = componentHeightRow5,
                        isAnyPreviousNotAvailable = isLineBroken,
                    )
                    Text(text = stringResource(id = R.string.disabled_show_notification_requirements))
                    IconButton(onClick = {
                        onRequestNotificationPermissionClicked()
                    }) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = stringResource(R.string.content_description_add_notification_permission)
                        )
                    }
                }
                if (items.isAllowedToShowNotification.not() && items.shouldShowDialogRequestNotificationPermissionRationale) {
                    IconButton(onClick = {
                        onGoToSettingsClicked()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = stringResource(R.string.content_go_to_settings)
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = {
                onContinueClicked()
            }) {
                Text(text = stringResource(id = R.string.button_continue))
            }
        }
    }
}