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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
                viewModel::onItemClicked
            )
        }

        CollectionUIState.Empty -> EmptyCollectionScreen(modifier)
        CollectionUIState.Error -> TODO()
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
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        listData.forEach { feature ->
            val isSelected = feature.uuid == selectedId
            if (feature.nodes.size == 1) {
                PointCard(
                    feature = feature,
                    isSelected = isSelected,
                    onItemClicked = onItemClicked,
                )
            } else {
                RouteCard(
                    feature = feature,
                    isSelected = isSelected,
                    onItemClicked = onItemClicked,
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RouteCard(
    modifier: Modifier = Modifier,
    feature: Feature,
    isSelected: Boolean = false,
    onItemClicked: KFunction1<Feature, Unit>,
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