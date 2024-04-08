package de.p72b.mocklation.ui.model.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.p72b.mocklation.R
import de.p72b.mocklation.data.Feature
import kotlin.reflect.KFunction1

@Composable
fun CollectionPage(
    modifier: Modifier = Modifier,
    viewModel: CollectionViewModel = koinViewModel()
) {
    val items by viewModel.uiState.collectAsStateWithLifecycle()
    when (items) {
        CollectionUIState.Loading -> LoadingCollectionScreen(modifier)
        is CollectionUIState.Data -> {
            val data = (items as CollectionUIState.Data)
            DataCollectionScreen(
                modifier,
                data.items,
                data.selectedItem,
                viewModel::onItemClicked,
                viewModel::onDelete,
            )
        }

        CollectionUIState.Empty -> EmptyCollectionScreen(modifier)
        CollectionUIState.Error -> TODO()
        is CollectionUIState.ShowSimulationCancelDialog -> {
            val data = (items as CollectionUIState.ShowSimulationCancelDialog)
            AlertDialogToCancelOngoingSimulation(
                data.feature,
                viewModel::onDismissToCancelOngoingSimulation,
                viewModel::onConfirmToCancelOngoingSimulation
            )
        }
    }
}

@Composable
internal fun LoadingCollectionScreen(
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
internal fun EmptyCollectionScreen(
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(text = "Empty TODO")
    }
}

@Composable
internal fun DataCollectionScreen(
    modifier: Modifier = Modifier,
    listData: List<Feature>,
    selectedId: String? = null,
    onItemClicked: KFunction1<Feature, Unit>,
    onDelete: KFunction1<Feature, Unit>,
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        listData.forEach { feature ->
            val isSelected = feature.uuid == selectedId
            if (feature.nodes.size == 1) {
                PointCard(
                    feature = feature,
                    isSelected = isSelected,
                    onItemClicked = onItemClicked,
                    onDelete = onDelete,
                )
            } else {
                RouteCard(
                    feature = feature,
                    isSelected = isSelected,
                    onItemClicked = onItemClicked,
                    onDelete = onDelete,
                )
            }
        }
        Spacer(Modifier.size(150.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PointCard(
    modifier: Modifier = Modifier,
    feature: Feature,
    isSelected: Boolean = false,
    onItemClicked: KFunction1<Feature, Unit>,
    onDelete: KFunction1<Feature, Unit>,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = {
            onItemClicked(feature)
        },
    ) {
        Column {
            CheckableProfileCircle(feature.uuid.substring(0, 2), isSelected)
            feature.name?.let {
                Text(text = it)
            }
            feature.nodes.forEach {
                Text(text = "${it.geometry.latLng.latitude} / ${it.geometry.latLng.longitude}")
            }
            ButtonBar(
                modifier = modifier,
                feature = feature,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
internal fun ButtonBar(
    modifier: Modifier = Modifier,
    feature: Feature,
    onDelete: KFunction1<Feature, Unit>,
) {
    IconButton(onClick = { onDelete(feature) }) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.baseline_delete_24),
            contentDescription = stringResource(id = R.string.delete)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RouteCard(
    modifier: Modifier = Modifier,
    feature: Feature,
    isSelected: Boolean = false,
    onItemClicked: KFunction1<Feature, Unit>,
    onDelete: KFunction1<Feature, Unit>,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = {
            onItemClicked(feature)
        },
    ) {
        Column {
            CheckableProfileCircle(feature.uuid.substring(0, 2), isSelected)
            feature.nodes.forEach {
                Text(text = "${it.geometry.latLng.latitude} / ${it.geometry.latLng.longitude}")
            }
            ButtonBar(
                modifier = modifier,
                feature = feature,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
internal fun CheckableProfileCircle(
    title: String,
    isSelected: Boolean = false
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(32.dp)
            .background(
                color = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            )
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "contentDescription",
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                tint = MaterialTheme.colorScheme.onSurface
            )
        } else {
            Text(text = title)
        }
    }
}

@Composable
fun AlertDialogToCancelOngoingSimulation(
    feature: Feature,
    onDismissRequest: () -> Unit,
    onConfirmation: (feature: Feature) -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(
                Icons.Filled.Warning,
                contentDescription = stringResource(id = R.string.content_description_warning)
            )
        },
        title = {
            Text(text = stringResource(id = R.string.dialog_title_cancel_title_ongoing_mock))
        },
        text = {
            Text(text = stringResource(id = R.string.dialog_title_cancel_message_ongoing_mock))
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation(feature)
                }
            ) {
                Text(text = stringResource(id = R.string.cta_ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(id = R.string.cta_cancel))
            }
        }
    )
}