package de.p72b.mocklation.ui.model.requirements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
fun RequirementsPage(modifier: Modifier = Modifier, viewModel: RequirementsViewModel = koinViewModel()) {
    val items by viewModel.uiState.collectAsStateWithLifecycle()
    when (items) {
        RequirementsUIState.Verifying -> VerifyingScreen(modifier)
        is RequirementsUIState.Status -> StatusScreen(modifier, items as RequirementsUIState.Status)
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
fun StatusScreen(modifier: Modifier = Modifier, items: RequirementsUIState.Status) {
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
    }
}