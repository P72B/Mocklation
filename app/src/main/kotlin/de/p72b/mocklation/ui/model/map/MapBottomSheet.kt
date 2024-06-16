package de.p72b.mocklation.ui.model.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.p72b.mocklation.R
import de.p72b.mocklation.data.Node
import de.p72b.mocklation.util.roundTo
import org.koin.androidx.compose.koinViewModel

@Composable
fun MapBottomSheet(
    viewModel: MapViewModel = koinViewModel(),
) {
    val items by viewModel.uiState.collectAsStateWithLifecycle()
    when (items) {
        is MapUIState.FeatureDataUpdate -> {
            val status = items as MapUIState.FeatureDataUpdate
            status.selectedId.let { selectedId ->
                status.feature.nodes.find {
                    it.id == selectedId
                }?.let {
                    DetailsView(
                        statisticsViewData = status.statisticsViewData,
                        node = it,
                        onSaveClicked = viewModel::onSaveClicked
                    )
                }
            }
        }

        else -> {

        }
    }
}

@Composable
fun DetailsView(
    statisticsViewData: MapUIState.StatisticsViewData,
    node: Node,
    onSaveClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            .height(200.dp)
    ) {
        StatisticsView(statisticsViewData)
        NodeListView(node)
        Button(
            onClick = {
                onSaveClicked()
            }, modifier = Modifier
                .align(alignment = Alignment.End)
        ) {
            Text(
                text = stringResource(id = R.string.button_save)
            )
        }
    }
}

@Composable
fun StatisticsView(
    statisticsViewData: MapUIState.StatisticsViewData,
) {
    Text(
        text = "${statisticsViewData.totalTravelTime} (${statisticsViewData.pathLength})"
    )
    Text(
        text = statisticsViewData.avgSpeed
    )
}

@Composable
fun NodeListView(
    node: Node,
) {
    Text(
        text = "${node.geometry.latitude.roundTo(6)} / ${
            node.geometry.longitude.roundTo(6)
        }"
    )
}