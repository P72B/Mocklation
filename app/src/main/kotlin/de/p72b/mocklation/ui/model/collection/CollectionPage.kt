package de.p72b.mocklation.ui.model.collection

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
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.p72b.mocklation.R

@Composable
fun CollectionPage(modifier: Modifier = Modifier, viewModel: CollectionViewModel = koinViewModel()) {
    val items by viewModel.uiState.collectAsStateWithLifecycle()
    when (items) {
        CollectionUIState.Loading -> LoadingCollectionScreen(modifier)
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