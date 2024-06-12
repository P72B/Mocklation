package de.p72b.mocklation.ui.model.simulation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
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
            onPauseSimulation = viewModel::pauseSimulation,
            modifier
        )

        SimulationUIState.StoppedSimulation -> StoppedSimulationScreen(
            onRunSimulation = viewModel::runSimulation,
            modifier
        )

        SimulationUIState.PausedSimulation -> PausedSimulationScreen(
            onStopSimulation = viewModel::stopSimulation,
            onResumeSimulation = viewModel::resumeSimulation,
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
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = { onRunSimulation() }) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.round_play_arrow_24),
                    contentDescription = stringResource(id = R.string.play)
                )
            }
        }
    }
}

@Composable
internal fun RunningSimulationScreen(
    onStopSimulation: () -> Unit,
    onPauseSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = { onPauseSimulation() }) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.round_pause_24),
                    contentDescription = stringResource(id = R.string.pause)
                )
            }
            IconButton(onClick = { onStopSimulation() }) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.round_stop_24),
                    contentDescription = stringResource(id = R.string.stop)
                )
            }
        }
    }
}


@Composable
internal fun PausedSimulationScreen(
    onStopSimulation: () -> Unit,
    onResumeSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = { onResumeSimulation() }) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.resume_24dp_fill1_wght400_grad0_opsz24),
                    contentDescription = stringResource(id = R.string.pause)
                )
            }
            IconButton(onClick = { onStopSimulation() }) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.round_stop_24),
                    contentDescription = stringResource(id = R.string.stop)
                )
            }
        }
    }
}