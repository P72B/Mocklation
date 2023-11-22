package de.p72b.mocklation.ui.model.requirements

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.p72b.mocklation.R
import de.p72b.mocklation.ui.theme.appColorScheme
import org.koin.androidx.compose.koinViewModel


@Composable
fun RequirementsPage(
    modifier: Modifier = Modifier, viewModel: RequirementsViewModel = koinViewModel()
) {
    val items by viewModel.uiState.collectAsStateWithLifecycle()
    when (items) {
        RequirementsUIState.Verifying -> VerifyingScreen(modifier)
        is RequirementsUIState.Status -> StatusScreen(
            items as RequirementsUIState.Status,
            viewModel::onRequestFineLocationPermissionClicked,
            viewModel::onRequestBackgroundLocationPermissionClicked,
            viewModel::onRequestNotificationPermissionClicked,
            viewModel::onGoToSettingsClicked,
            viewModel::onContinueClicked
        )
    }
}

val stationRadius = 20.dp
val stationPadding = 32.dp

@Composable
fun station(
    rowHeight: Dp,
    isAnyPreviousNotAvailable: Boolean = false,
    isAvailable: Boolean = false,
    isDestination: Boolean = false,
) {
    var pathEffect: PathEffect? = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    val strokeWidth = 2.dp
    val disabledColor = appColorScheme.outline
    val enabledColor = appColorScheme.primaryContainer
    var fillColor = disabledColor
    var strokeColor = enabledColor
    if (isAvailable) {
        if (isAnyPreviousNotAvailable.not()) {
            pathEffect = null
        }
        fillColor = enabledColor
    }
    if (pathEffect != null) {
        strokeColor = disabledColor
    }
    Canvas(
        modifier = Modifier.padding(end = stationRadius / 2)
    ) {
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
        drawCircle(
            center = Offset(0f, 0f),
            color = disabledColor,
            radius = circleRadius.toPx() + strokeWidth.toPx()
        )
        if (isAvailable) {
            drawCircle(
                center = Offset(0f, 0f),
                color = fillColor,
                radius = circleRadius.toPx()
            )
        } else {
            drawCircle(
                center = Offset(0f, 0f),
                color = appColorScheme.surface,
                radius = circleRadius.toPx()
            )
        }
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
    status: RequirementsUIState.Status,
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
    Column(
        Modifier
            .fillMaxHeight()
            .padding(start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f, false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp, top = 16.dp)
                    .onGloballyPositioned {
                        componentHeightRow1 = with(density) {
                            it.size.height.toDp()
                        }
                    },
            ) {
                Text(
                    style = MaterialTheme.typography.headlineSmall,
                    text = stringResource(id = R.string.title_pre_requirements)
                )
                Spacer(Modifier.size(16.dp))
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = stringResource(id = R.string.content_pre_requirements)
                )
                Spacer(Modifier.size(16.dp))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = stationRadius / 2, bottom = stationPadding)
                    .onGloballyPositioned {
                        componentHeightRow1 = with(density) {
                            it.size.height.toDp()
                        }
                    },
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val height = componentHeightRow1 + ((componentHeightRow2 - componentHeightRow1) / 2)
                setIsLineBroken(status.isDeveloperOptionsEnabled)
                station(
                    rowHeight = height,
                    isAvailable = status.isDeveloperOptionsEnabled,
                )
                Column {
                    if (status.isDeveloperOptionsEnabled) {
                        Text(
                            style = MaterialTheme.typography.bodyLarge,
                            text = stringResource(id = R.string.developer_options_enabled_requirements)
                        )
                    } else {
                        Text(
                            style = MaterialTheme.typography.bodyLarge,
                            text = stringResource(id = R.string.developer_options_disabled_requirements)
                        )
                        OutlinedButton(onClick = { /*TODO*/ }) {
                            Text(stringResource(id = R.string.button_add))
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = stationRadius / 2, bottom = stationPadding)
                    .onGloballyPositioned {
                        componentHeightRow2 = with(density) {
                            it.size.height.toDp()
                        }
                    },
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val height = componentHeightRow2 + ((componentHeightRow3 - componentHeightRow2) / 2)
                setIsLineBroken(status.isDeveloperOptionsEnabled)
                station(
                    rowHeight = height,
                    isAvailable = status.isSelectedMockLocationApp,
                    isAnyPreviousNotAvailable = isLineBroken,
                )
                if (status.isSelectedMockLocationApp) {
                    Text(
                        style = MaterialTheme.typography.bodyLarge,
                        text = stringResource(id = R.string.developer_options_selected_mock_location_app_requirements)
                    )
                } else {
                    Text(
                        style = MaterialTheme.typography.bodyLarge,
                        text = stringResource(id = R.string.developer_options_unselected_mock_location_app_requirements)
                    )
                    TextButton(onClick = { /*TODO*/ }) {
                        Text(stringResource(id = R.string.button_add))
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = stationRadius / 2, bottom = stationPadding)
                    .onGloballyPositioned {
                        componentHeightRow3 = with(density) {
                            it.size.height.toDp()
                        }
                    },
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val height = componentHeightRow3 + ((componentHeightRow4 - componentHeightRow3) / 2)
                setIsLineBroken(status.isSelectedMockLocationApp)
                station(
                    rowHeight = height,
                    isAvailable = status.isAccessToFineLocationGranted,
                    isAnyPreviousNotAvailable = isLineBroken,
                )
                if (status.isAccessToFineLocationGranted) {
                    Text(
                        style = MaterialTheme.typography.bodyLarge,
                        text = stringResource(id = R.string.fine_location_granted_requirements)
                    )
                } else {
                    Text(
                        style = MaterialTheme.typography.bodyLarge,
                        text = stringResource(id = R.string.fine_location_permission_missing_requirements)
                    )
                    IconButton(onClick = {
                        onRequestFineLocationPermissionClicked()
                    }) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = stringResource(R.string.content_description_add_fine_location_permission)
                        )
                    }
                    if (status.isAccessToBackgroundLocationGranted.not() && status.shouldShowDialogRequestLocationPermissionRationale) {
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
                        .padding(start = stationRadius / 2, bottom = stationPadding)
                        .onGloballyPositioned {
                            componentHeightRow4 = with(density) {
                                it.size.height.toDp()
                            }
                        },
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val height =
                        componentHeightRow4 + ((componentHeightRow5 - componentHeightRow4) / 2)
                    setIsLineBroken(status.isAccessToFineLocationGranted)
                    station(
                        rowHeight = height,
                        isAvailable = status.isAccessToBackgroundLocationGranted,
                        isAnyPreviousNotAvailable = isLineBroken,
                    )
                    if (status.isAccessToBackgroundLocationGranted) {
                        Text(
                            style = MaterialTheme.typography.bodyLarge,
                            text = stringResource(id = R.string.background_location_granted_requirements)
                        )
                    } else {
                        Text(
                            style = MaterialTheme.typography.bodyLarge,
                            text = stringResource(id = R.string.background_location_permission_missing_requirements)
                        )
                        IconButton(onClick = {
                            onRequestBackgroundLocationPermissionClicked()
                        }) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = stringResource(R.string.content_description_add_background_location_permission)
                            )
                        }
                    }
                    if (status.isAccessToBackgroundLocationGranted.not() && status.shouldShowDialogRequestBackgroundLocationPermissionRationale) {
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
                        .padding(start = stationRadius / 2, bottom = stationPadding)
                        .onGloballyPositioned {
                            componentHeightRow5 = with(density) {
                                it.size.height.toDp()
                            }
                        },
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    setIsLineBroken(status.isAccessToBackgroundLocationGranted)
                    station(
                        rowHeight = componentHeightRow5,
                        isAvailable = status.isAllowedToShowNotification,
                        isDestination = true,
                        isAnyPreviousNotAvailable = isLineBroken,
                    )
                    if (status.isAllowedToShowNotification) {
                        Text(
                            style = MaterialTheme.typography.bodyLarge,
                            text = stringResource(id = R.string.enabled_show_notification_requirements)
                        )
                    } else {
                        Text(
                            style = MaterialTheme.typography.bodyLarge,
                            text = stringResource(id = R.string.disabled_show_notification_requirements)
                        )
                        IconButton(onClick = {
                            onRequestNotificationPermissionClicked()
                        }) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = stringResource(R.string.content_description_add_notification_permission)
                            )
                        }
                    }
                    if (status.isAllowedToShowNotification.not() && status.shouldShowDialogRequestNotificationPermissionRationale) {
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
        }

        Button(
            onClick = {
                onContinueClicked()
            }, modifier = Modifier
                .padding(bottom = 16.dp)
                .align(alignment = Alignment.End)
        ) {
            Text(
                text = if (status.isReady()) {
                    stringResource(id = R.string.button_continue)
                } else {
                    stringResource(id = R.string.button_back)
                }
            )
        }
    }
}