package com.nfcwallet.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfcwallet.app.R
import com.nfcwallet.app.data.local.NfcCardEntity
import com.nfcwallet.app.data.mapper.toInfo
import com.nfcwallet.app.ui.NfcUiState
import com.nfcwallet.app.ui.components.CircularIconButton
import com.nfcwallet.app.ui.components.CustomizeCardDialog
import com.nfcwallet.app.ui.components.EmptyWalletState
import com.nfcwallet.app.ui.components.NfcDisabledBanner
import com.nfcwallet.app.ui.components.WalletCard
import com.nfcwallet.app.ui.details.TechnicalDetailsSheet
import com.nfcwallet.app.viewmodel.NfcCardViewModel

// Clean Cards view with a large Hero Banner and elegant minimalist structure
@Composable
fun MyCardsTab(
    uiState: NfcUiState,
    viewModel: NfcCardViewModel,
    isHideSensitiveInfo: Boolean,
    onRequestAuth: (String, () -> Unit) -> Unit,
    onOpenAddCardOptions: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val savedCards by viewModel.savedCards.collectAsStateWithLifecycle()
    var selectedCard by remember { mutableStateOf<NfcCardEntity?>(null) }
    var cardToCustomize by remember { mutableStateOf<NfcCardEntity?>(null) }
    val haptic = LocalHapticFeedback.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (savedCards.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onOpenAddCardOptions,
                    shape = RoundedCornerShape(50),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Card",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add card",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Banner when NFC is turned off
            AnimatedVisibility(
                visible = uiState is NfcUiState.Disabled,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                NfcDisabledBanner()
            }

            // Minimalist Header Row (Logo on left, Settings on right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "NFC Wallet Logo",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )

                CircularIconButton(
                    icon = Icons.Default.Settings,
                    contentDescription = "Settings",
                    onClick = onNavigateToSettings,
                    size = 44.dp,
                    iconSize = 22.dp
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp, start = 20.dp, end = 20.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Big Hero Banner (Title + Beautiful Icon)
                    item {
                        CardsHeroBanner(cardCount = savedCards.size)
                    }

                    if (savedCards.isEmpty()) {
                        item {
                            EmptyWalletState(onAddCardClick = onOpenAddCardOptions)
                        }
                    } else {
                        items(savedCards, key = { it.id }) { card ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedCard = card
                                    }
                            ) {
                                WalletCard(
                                    cardInfo = card.toInfo(),
                                    cardName = card.name,
                                    customCategory = card.category,
                                    customIconName = card.iconName,
                                    customColorHex = card.colorHex,
                                    isFavorite = card.isFavorite,
                                    isMasked = isHideSensitiveInfo
                                )
                            }
                        }
                    }
                }

                // Technical Details Sheet
                selectedCard?.let { card ->
                    TechnicalDetailsSheet(
                        cardInfo = card.toInfo(),
                        isHideSensitiveInfo = isHideSensitiveInfo,
                        onDismiss = { selectedCard = null },
                        onRequestAuth = onRequestAuth,
                        onEditCustomization = {
                            onRequestAuth("Authenticate to customize card") {
                                cardToCustomize = card
                                selectedCard = null
                            }
                        },
                        onDelete = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.deleteCard(card)
                            selectedCard = null
                        }
                    )
                }

                // Customize Card Dialog
                cardToCustomize?.let { card ->
                    CustomizeCardDialog(
                        initialName = card.name,
                        initialCategory = card.category,
                        initialIconName = card.iconName,
                        initialColorHex = card.colorHex,
                        initialIsFavorite = card.isFavorite,
                        cardInfo = card.toInfo(),
                        onDismiss = { cardToCustomize = null },
                        onSave = { name, category, iconName, colorHex, isFavorite ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val updatedCard = card.copy(
                                name = name,
                                category = category,
                                iconName = iconName,
                                colorHex = colorHex,
                                isFavorite = isFavorite
                            )
                            viewModel.updateCardEntity(updatedCard)
                            cardToCustomize = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CardsHeroBanner(cardCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, top = 8.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Cards",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (cardCount == 0) "Your digital wallet is empty" else "$cardCount secure passes",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
