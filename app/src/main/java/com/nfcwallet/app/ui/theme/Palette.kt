package com.nfcwallet.app.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class ColorOption(val id: String, val name: String, val color: Color)
data class IconOption(val id: String, val name: String, val icon: ImageVector)

object CardPalette {

    val categories = listOf("Work", "Home", "Access", "Transport", "Education", "Gym", "Other")

    val colorOptions = listOf(
        ColorOption("blue", "Blue", Color(0xFF1D4ED8)),
        ColorOption("violet", "Violet", Color(0xFF7C3AED)),
        ColorOption("emerald", "Emerald", Color(0xFF059669)),
        ColorOption("red", "Red", Color(0xFFDC2626)),
        ColorOption("amber", "Amber", Color(0xFFD97706)),
        ColorOption("teal", "Teal", Color(0xFF0D9488)),
        ColorOption("rose", "Rose", Color(0xFFE11D48)),
        ColorOption("graphite", "Graphite", Color(0xFF1F2937)),
        ColorOption("navy", "Navy", Color(0xFF0F172A))
    )

    val iconOptions = listOf(
        IconOption("card", "Card", Icons.Default.List),
        IconOption("work", "Work", Icons.Default.Build),
        IconOption("home", "Home", Icons.Default.Home),
        IconOption("key", "Key", Icons.Default.Lock),
        IconOption("badge", "Badge", Icons.Default.AccountBox),
        IconOption("transport", "Transport", Icons.Default.Place),
        IconOption("education", "Education", Icons.Default.Info),
        IconOption("gym", "Gym", Icons.Default.Star)
    )

    fun getColor(colorId: String): Color {
        if (colorId.startsWith("#")) {
            return try {
                Color(android.graphics.Color.parseColor(colorId))
            } catch (e: Exception) {
                Color(0xFF1D4ED8)
            }
        }
        return colorOptions.find { it.id == colorId }?.color ?: Color(0xFF1D4ED8)
    }

    fun getIcon(iconId: String): ImageVector {
        return iconOptions.find { it.id == iconId }?.icon ?: Icons.Default.List
    }
}
