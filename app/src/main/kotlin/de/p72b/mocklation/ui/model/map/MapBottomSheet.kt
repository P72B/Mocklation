package de.p72b.mocklation.ui.model.map

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.p72b.mocklation.R
import de.p72b.mocklation.data.MockFeature
import de.p72b.mocklation.data.Node
import de.p72b.mocklation.ui.model.collection.ButtonBar
import de.p72b.mocklation.ui.model.collection.CheckableProfileCircle
import de.p72b.mocklation.ui.model.collection.PointCard
import de.p72b.mocklation.ui.model.collection.RouteCard
import de.p72b.mocklation.util.StatisticsViewData
import de.p72b.mocklation.util.roundTo
import org.koin.androidx.compose.koinViewModel
import kotlin.reflect.KFunction1

@Composable
fun MapBottomSheet(
    viewModel: MapViewModel = koinViewModel(),
) {
    val items by viewModel.uiState.collectAsStateWithLifecycle()
    when (items) {
        is MapUIState.FeatureDataUpdate -> {
            val status = items as MapUIState.FeatureDataUpdate
            DetailsView(
                statisticsViewData = status.statisticsViewData,
                nodes = status.feature.nodes,
                onSaveClicked = viewModel::onSaveClicked,
                selectedNodeId = status.selectedId,
                onItemClicked = viewModel::onNodeListItemClicked,
                onDelete = viewModel::onDeleteClicked,
                onIsTunnelCheckedChange = viewModel::onIsTunnelCheckedChange,
            )
        }

        else -> {

        }
    }
}

@Composable
fun DetailsView(
    modifier: Modifier = Modifier,
    statisticsViewData: StatisticsViewData,
    nodes: List<Node>,
    selectedNodeId: Int? = null,
    onSaveClicked: () -> Unit,
    onItemClicked: KFunction1<Node, Unit>,
    onDelete: KFunction1<Node, Unit>,
    onIsTunnelCheckedChange : KFunction1<Pair<Node, Boolean>, Unit>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            .height(200.dp)
    ) {
        StatisticsView(statisticsViewData)
        NodeListView(
            modifier = modifier,
            nodes = nodes,
            selectedNodeId = selectedNodeId,
            onItemClicked = onItemClicked,
            onDelete = onDelete,
            onIsTunnelCheckedChange = onIsTunnelCheckedChange,
        )
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
    statisticsViewData: StatisticsViewData,
) {
    Text(
        text = "${statisticsViewData.totalTravelTime} (${statisticsViewData.pathLength}) ${statisticsViewData.avgSpeed}"
    )
}

@Composable
fun NodeListView(
    modifier: Modifier = Modifier,
    nodes: List<Node>,
    selectedNodeId: Int? = null,
    onItemClicked: KFunction1<Node, Unit>,
    onDelete: KFunction1<Node, Unit>,
    onIsTunnelCheckedChange : KFunction1<Pair<Node, Boolean>, Unit>,
) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        nodes.forEach { node ->
            NodeCard(
                modifier = modifier,
                node = node,
                isSelected = selectedNodeId == node.id,
                onItemClicked = onItemClicked,
                onDelete = onDelete,
                onIsTunnelCheckedChange = onIsTunnelCheckedChange,
            )
        }
        Spacer(Modifier.size(16.dp))
    }
}

@Composable
internal fun NodeCard(
    modifier: Modifier = Modifier,
    node: Node,
    isSelected: Boolean = false,
    onItemClicked: KFunction1<Node, Unit>,
    onDelete: KFunction1<Node, Unit>,
    onIsTunnelCheckedChange : KFunction1<Pair<Node, Boolean>, Unit>,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = {
            if (isSelected.not()) {
                onItemClicked(node)
            }
        },
    ) {
        Column {
            CheckableProfileCircle(node.id.toString(), isSelected)
            Text(
                text = "${node.geometry.latitude.roundTo(6)} / ${
                    node.geometry.longitude.roundTo(6)
                }"
            )
            Row(
                    verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(id = R.string.checkbox_tunnel)
                )
                Checkbox(
                    checked = node.isTunnel,
                    onCheckedChange = {
                        onIsTunnelCheckedChange(Pair(node, it))
                    }
                )
            }
            NodeButtonBar(
                node = node,
                onDelete = onDelete,
            )
        }
    }
}


@Composable
internal fun NodeButtonBar(
    node: Node,
    onDelete: KFunction1<Node, Unit>,
) {
    IconButton(onClick = { onDelete(node) }) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.baseline_delete_24),
            contentDescription = stringResource(id = R.string.delete)
        )
    }
}