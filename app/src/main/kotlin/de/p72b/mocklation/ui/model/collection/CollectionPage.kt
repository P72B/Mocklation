package de.p72b.mocklation.ui.model.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.p72b.mocklation.R
import de.p72b.mocklation.data.Feature

@Composable
fun CollectionPage(
    modifier: Modifier = Modifier,
    viewModel: CollectionViewModel = koinViewModel()
) {
    val items by viewModel.uiState.collectAsStateWithLifecycle()
    when (items) {
        CollectionUIState.Loading -> LoadingCollectionScreen(modifier)
        is CollectionUIState.Data -> DataCollectionScreen(
            modifier,
            (items as CollectionUIState.Data).items
        )

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
    listData: List<Feature>
) {
    Column {
        listData.forEach { feature ->
            if (feature.nodes.size == 1) {
                PointCard(feature = feature)
            } else {
                RouteCard(feature = feature)
            }
        }
    }
}

@Composable
internal fun PointCard(modifier: Modifier = Modifier, feature: Feature) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column {
            Text(text = feature.uuid)
            feature.name?.let {
                Text(text = it)
            }
            feature.nodes.forEach {
                Text(text = "${it.geometry.latLng.latitude} / ${it.geometry.latLng.longitude}")
            }
        }
    }
}

@Composable
internal fun RouteCard(modifier: Modifier = Modifier, feature: Feature) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column {
            Text(text = feature.uuid)
            feature.nodes.forEach {
                Text(text = "${it.geometry.latLng.latitude} / ${it.geometry.latLng.longitude}")
            }
        }
    }
}