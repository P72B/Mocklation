package de.p72b.mocklation.ui.model.requirements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
            viewModel::onContinueClicked
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
    onContinueClicked: () -> Unit
) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (items.isDeveloperOptionsEnabled) {
                Text(text = stringResource(id = R.string.developer_options_enabled_requirements))
            } else {
                Text(text = stringResource(id = R.string.developer_options_disabled_requirements))
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (items.isSelectedMockLocationApp) {
                Text(text = stringResource(id = R.string.developer_options_selected_mock_location_app_requirements))
            } else {
                Text(text = stringResource(id = R.string.developer_options_unselected_mock_location_app_requirements))
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (items.isAccessToFineLocationGranted) {
                Text(text = stringResource(id = R.string.fine_location_granted_requirements))
            } else {
                Text(text = stringResource(id = R.string.fine_location_permission_missing_requirements))
                IconButton(onClick = {
                    onRequestFineLocationPermissionClicked()
                }) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = stringResource(R.string.content_description_add_fine_location_permission)
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (items.isAccessToBackgroundLocationGranted) {
                Text(text = stringResource(id = R.string.background_location_granted_requirements))
            } else {
                Text(text = stringResource(id = R.string.background_location_permission_missing_requirements))
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (items.isAllowedToShowNotification) {
                Text(text = stringResource(id = R.string.enabled_show_notification_requirements))
            } else {
                Text(text = stringResource(id = R.string.disabled_show_notification_requirements))
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