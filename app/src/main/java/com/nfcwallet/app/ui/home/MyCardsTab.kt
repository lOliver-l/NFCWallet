package com.nfcwallet.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfcwallet.app.data.local.NfcCardEntity
import com.nfcwallet.app.data.mapper.toInfo
import com.nfcwallet.app.ui.components.WalletCard
import com.nfcwallet.app.ui.details.TechnicalDetailsSheet
import com.nfcwallet.app.viewmodel.NfcCardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCardsTab(viewModel: NfcCardViewModel) {
    val savedCards by viewModel.savedCards.collectAsStateWithLifecycle()
    var selectedCard by remember { mutableStateOf<NfcCardEntity?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "My Cards", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (savedCards.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No saved cards yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(savedCards) { card ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCard = card }
                        ) {
                            Text(
                                text = card.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            )
                            WalletCard(cardInfo = card.toInfo())
                        }
                    }
                }
            }

            selectedCard?.let { card ->
                TechnicalDetailsSheet(
                    cardInfo = card.toInfo(),
                    onDismiss = { selectedCard = null },
                    onDelete = {
                        viewModel.deleteCard(card)
                        selectedCard = null
                    }
                )
            }
        }
    }
}
