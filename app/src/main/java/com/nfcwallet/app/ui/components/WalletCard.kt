package com.nfcwallet.app.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfcwallet.app.nfc.model.NfcCardInfo
import com.nfcwallet.app.ui.theme.CardPalette

// Minimalist Digital Pass Wallet Card with Breathing/Pulsing NFC indicator
@Composable
fun WalletCard(
    cardInfo: NfcCardInfo,
    cardName: String? = null,
    customCategory: String = "Other",
    customIconName: String = "card",
    customColorHex: String = "blue",
    isFavorite: Boolean = false,
    isMasked: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cardCornerRadius = 10.dp

    val solidColor = CardPalette.getColor(customColorHex)
    val cardIcon = CardPalette.getIcon(customIconName)

    val cardTitle = cardName ?: if (cardInfo.hasMifareClassic) {
        "MIFARE ${cardInfo.mifareType} ${if (cardInfo.size > 0) "${cardInfo.size / 1024}K" else ""}".trim()
    } else {
        "NFC Smart Card"
    }

    val cardSubtitle = if (cardName != null) {
        "$customCategory • ${if (cardInfo.hasMifareClassic) "MIFARE ${cardInfo.mifareType}" else "NFC Tag"}"
    } else customCategory

    // Subtle Breathing LED Animation for the NFC Pass indicator
    val infiniteTransition = rememberInfiniteTransition(label = "nfc_pulse")
    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_alpha"
    )
    val indicatorColor by infiniteTransition.animateColor(
        initialValue = Color.White.copy(alpha = 0.10f),
        targetValue = Color.White.copy(alpha = 0.25f),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "indicator_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(solidColor)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(cardCornerRadius)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Row with Large Title and Large Circular Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = cardTitle,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                            if (isFavorite) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Favorite",
                                    tint = Color(0xFFFCD34D),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cardSubtitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Large Circular Icon Container
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = cardIcon,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = Color.White
                        )
                    }
                }

                // Bottom Contactless Indicator Badge with Breathing Animation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = indicatorColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "(((",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White.copy(alpha = waveAlpha)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "NFC PASS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = waveAlpha)
                            )
                        }
                    }
                }
            }
        }
    }
}
