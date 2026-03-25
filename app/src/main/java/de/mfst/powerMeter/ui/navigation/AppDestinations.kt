package de.mfst.powerMeter.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    OVERVIEW("Overview", Icons.Default.BarChart),
    READINGS("Readings", Icons.Default.EditNote),
    BILL("Bill", Icons.Default.Receipt),
    PROVIDER("Provider", Icons.Default.Business),
}
