package de.p72b.mocklation.ui.model.simulation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.p72b.mocklation.R

@Composable
fun SimulationPage(modifier: Modifier, viewModel: SimulationViewModel = koinViewModel()) {
    val items by viewModel.uiState.collectAsStateWithLifecycle()
    when (items) {
        SimulationUIState.Loading -> LoadingSimulationScreen(modifier)
        SimulationUIState.RunningSimulation -> RunningSimulationScreen(
            onStopSimulation = viewModel::stopSimulation,
            modifier
        )
        SimulationUIState.StoppedSimulation -> StoppedSimulationScreen(
            onRunSimulation = viewModel::runSimulation,
            modifier
        )
        is SimulationUIState.Error -> TODO()
        is SimulationUIState.Success -> TODO()
    }
}

@Composable
internal fun LoadingSimulationScreen(
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = stringResource(id = R.string.loading))
        }
    }
}

@Composable
internal fun StoppedSimulationScreen(
    onRunSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(400.dp))
            Button(modifier = Modifier.width(96.dp), onClick = { onRunSimulation() }) {
                Text(stringResource(id = R.string.play))
            }
        }
    }
}

@Composable
internal fun RunningSimulationScreen(
    onStopSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(modifier = Modifier.width(96.dp), onClick = { onStopSimulation() }) {
                Text(stringResource(id = R.string.stop))
            }
        }
    }
}