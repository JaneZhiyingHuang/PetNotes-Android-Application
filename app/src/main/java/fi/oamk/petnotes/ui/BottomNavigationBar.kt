package fi.oamk.petnotes.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import fi.oamk.petnotes.R
import fi.oamk.petnotes.model.NavigationItem

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar(containerColor = Color(0xFFEFEFEF)) {
        val items = listOf(
            NavigationItem("home", R.string.home, R.drawable.baseline_home_24),
            NavigationItem("notes", R.string.notes, R.drawable.baseline_speaker_notes_24),
            NavigationItem("map", R.string.map, R.drawable.baseline_map_24),
            NavigationItem("setting", R.string.setting, R.drawable.baseline_settings_24)
        )

        items.forEach { item ->
            NavigationBarItem(
                selected = false,
                onClick = {
                    navController.navigate(item.route)
                },
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = item.iconResId),
                            contentDescription = stringResource(item.labelResId),
                          // Correctly using the Modifier.size
                        )
                    // Space between icon and text
                        Text(
                            text = stringResource(item.labelResId),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    }
}
