package de.mfst.powerMeter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import de.mfst.powerMeter.ui.bill.BillScreen
import de.mfst.powerMeter.ui.navigation.AppDestinations
import de.mfst.powerMeter.ui.overview.OverviewScreen
import de.mfst.powerMeter.ui.provider.ProviderScreen
import de.mfst.powerMeter.ui.readings.ReadingsScreen
import de.mfst.powerMeter.ui.theme.PowerMeterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PowerMeterTheme {
                PowerMeterApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun PowerMeterApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.OVERVIEW) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = { Icon(it.icon, contentDescription = it.label) },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        when (currentDestination) {
            AppDestinations.OVERVIEW -> OverviewScreen()
            AppDestinations.READINGS -> ReadingsScreen()
            AppDestinations.BILL -> BillScreen()
            AppDestinations.PROVIDER -> ProviderScreen()
        }
    }
}
