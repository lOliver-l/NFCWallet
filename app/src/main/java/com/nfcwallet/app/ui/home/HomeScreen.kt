package com.nfcwallet.app.ui.home

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.nfcwallet.app.nfc.model.NfcCardInfo
import com.nfcwallet.app.security.SecurityPreferences
import com.nfcwallet.app.ui.NfcUiState
import com.nfcwallet.app.ui.components.AddCardOptionsSheet
import com.nfcwallet.app.ui.components.AppleWalletHeader
import com.nfcwallet.app.ui.components.CustomizeCardDialog
import com.nfcwallet.app.ui.components.NfcScanSheet
import com.nfcwallet.app.ui.components.PillButton
import com.nfcwallet.app.ui.components.QrScanSheet
import com.nfcwallet.app.ui.components.RoundedSection
import com.nfcwallet.app.ui.components.ScanningState
import com.nfcwallet.app.ui.components.WalletCard
import com.nfcwallet.app.ui.details.TechnicalDetailsSheet
import com.nfcwallet.app.ui.settings.SecuritySettingsTab
import com.nfcwallet.app.viewmodel.NfcCardViewModel

enum class WalletScreenState {
    Cards,
    Settings
}

// Single-section Apple Wallet Container with Slide Physics Page Transitions (No Flashing)
@Composable
fun MainScreen(
    uiState: NfcUiState,
    viewModel: NfcCardViewModel,
    securityPreferences: SecurityPreferences,
    biometricStatusMessage: String,
    onRequestAuth: (String, () -> Unit) -> Unit,
    onThemeChanged: (String) -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf(WalletScreenState.Cards) }
    var showAddCardOptionsSheet by remember { mutableStateOf(false) }
    var showNfcScanSheet by remember { mutableStateOf(false) }
    var showQrScanSheet by remember { mutableStateOf(false) }
    var detectedQrCardInfo by remember { mutableStateOf<NfcCardInfo?>(null) }
    var showCustomizeFromNfc by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is NfcUiState.TagDetected) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showNfcScanSheet = true
        }
    }

    // Force surface background color immediately to avoid white flashes
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                val springSpec = spring<IntOffset>(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioNoBouncy
                )

                if (targetState == WalletScreenState.Settings) {
                    // Slide Settings in from the right, slide Cards out to the left
                    slideInHorizontally(animationSpec = springSpec) { fullWidth -> fullWidth } togetherWith
                            slideOutHorizontally(animationSpec = springSpec) { fullWidth -> -fullWidth }
                } else {
                    // Slide Cards in from the left, slide Settings out to the right
                    slideInHorizontally(animationSpec = springSpec) { fullWidth -> -fullWidth } togetherWith
                            slideOutHorizontally(animationSpec = springSpec) { fullWidth -> fullWidth }
                }
            },
            label = "iOSSlideTransition"
        ) { targetScreen ->
            when (targetScreen) {
                WalletScreenState.Cards -> MyCardsTab(
                    uiState = uiState,
                    viewModel = viewModel,
                    isHideSensitiveInfo = securityPreferences.isHideSensitiveInfo,
                    onRequestAuth = onRequestAuth,
                    onOpenAddCardOptions = { showAddCardOptionsSheet = true },
                    onNavigateToSettings = { currentScreen = WalletScreenState.Settings }
                )
                WalletScreenState.Settings -> SecuritySettingsTab(
                    securityPreferences = securityPreferences,
                    biometricStatusMessage = biometricStatusMessage,
                    onRequestAuthForToggle = { _, onResult ->
                        onRequestAuth("Authenticate to enable app lock") {
                            onResult(true)
                        }
                    },
                    onThemeChanged = onThemeChanged,
                    onBackToCards = { currentScreen = WalletScreenState.Cards }
                )
            }
        }

        // Add Card Options Popup
        if (showAddCardOptionsSheet) {
            AddCardOptionsSheet(
                onDismiss = { showAddCardOptionsSheet = false },
                onSelectNfc = {
                    showNfcScanSheet = true
                },
                onSelectQr = {
                    showQrScanSheet = true
                }
            )
        }

        // Popup NFC Scan Sheet
        if (showNfcScanSheet) {
            NfcScanSheet(
                uiState = uiState,
                isHideSensitiveInfo = securityPreferences.isHideSensitiveInfo,
                onDismiss = { showNfcScanSheet = false },
                onSaveCardClick = {
                    showNfcScanSheet = false
                    showCustomizeFromNfc = true
                },
                onRequestAuth = onRequestAuth
            )
        }

        // Save NFC Card Customization Dialog
        if (showCustomizeFromNfc && uiState is NfcUiState.TagDetected) {
            CustomizeCardDialog(
                cardInfo = uiState.cardInfo,
                onDismiss = { showCustomizeFromNfc = false },
                onSave = { name, category, iconName, colorHex, isFavorite ->
                    viewModel.saveCard(
                        cardInfo = uiState.cardInfo,
                        name = name,
                        category = category,
                        iconName = iconName,
                        colorHex = colorHex,
                        isFavorite = isFavorite
                    )
                    showCustomizeFromNfc = false
                }
            )
        }

        // Popup QR Scan Sheet
        if (showQrScanSheet) {
            QrScanSheet(
                onDismiss = { showQrScanSheet = false },
                onQrDetected = { cardInfo ->
                    detectedQrCardInfo = cardInfo
                }
            )
        }

        // Save QR Pass Customization Dialog
        detectedQrCardInfo?.let { qrCardInfo ->
            CustomizeCardDialog(
                cardInfo = qrCardInfo,
                initialName = "QR Pass",
                initialCategory = "Access",
                initialIconName = "badge",
                initialColorHex = "violet",
                onDismiss = { detectedQrCardInfo = null },
                onSave = { name, category, iconName, colorHex, isFavorite ->
                    viewModel.saveCard(
                        cardInfo = qrCardInfo,
                        name = name,
                        category = category,
                        iconName = iconName,
                        colorHex = colorHex,
                        isFavorite = isFavorite
                    )
                    detectedQrCardInfo = null
                }
            )
        }
    }
}

@Composable
fun ScanTab(
    uiState: NfcUiState,
    viewModel: NfcCardViewModel,
    isHideSensitiveInfo: Boolean,
    onRequestAuth: (String, () -> Unit) -> Unit,
    onBackToCards: () -> Unit
) {
    var showTechnicalDetails by remember { mutableStateOf(false) }
    var showCustomizeDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(uiState) {
        if (uiState is NfcUiState.TagDetected) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Scaffold(
        topBar = {
            AppleWalletHeader(
                title = "Scanner",
                onBackClick = onBackToCards
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is NfcUiState.Checking -> LoadingState("Checking NFC status...")
                is NfcUiState.NotSupported -> ErrorState("NFC hardware is not supported on this device.")
                is NfcUiState.Disabled -> ErrorState("NFC is disabled. Please turn on NFC in Android Settings.")
                is NfcUiState.Waiting -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ScanningState()
                    }
                    PillButton(
                        text = "Cancel & Back to Cards",
                        onClick = onBackToCards,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .height(52.dp)
                    )
                }
                is NfcUiState.TagDetected -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        WalletCard(
                            cardInfo = uiState.cardInfo,
                            isMasked = isHideSensitiveInfo
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        InfoChipCard(
                            technology = uiState.cardInfo.techList.firstOrNull() ?: "Unknown",
                            size = "${uiState.cardInfo.size}B",
                            sectors = uiState.cardInfo.sectorCount.toString(),
                            blocks = uiState.cardInfo.blockCount.toString()
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        PillButton(
                            text = "Save Card to Wallet",
                            onClick = { showCustomizeDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showTechnicalDetails = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "Technical Specification",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    if (showTechnicalDetails) {
                        TechnicalDetailsSheet(
                            cardInfo = uiState.cardInfo,
                            isHideSensitiveInfo = isHideSensitiveInfo,
                            onDismiss = { showTechnicalDetails = false },
                            onRequestAuth = onRequestAuth
                        )
                    }

                    if (showCustomizeDialog) {
                        CustomizeCardDialog(
                            cardInfo = uiState.cardInfo,
                            onDismiss = { showCustomizeDialog = false },
                            onSave = { name, category, iconName, colorHex, isFavorite ->
                                viewModel.saveCard(
                                    cardInfo = uiState.cardInfo,
                                    name = name,
                                    category = category,
                                    iconName = iconName,
                                    colorHex = colorHex,
                                    isFavorite = isFavorite
                                )
                                showCustomizeDialog = false
                                onBackToCards()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(32.dp)
        )
    }
}

@Composable
fun InfoChipCard(technology: String, size: String, sectors: String, blocks: String) {
    RoundedSection(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(label = "Primary Tech", value = technology)
                InfoItem(label = "Memory Size", value = size)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(label = "Sectors Count", value = sectors)
                InfoItem(label = "Blocks Count", value = blocks)
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
