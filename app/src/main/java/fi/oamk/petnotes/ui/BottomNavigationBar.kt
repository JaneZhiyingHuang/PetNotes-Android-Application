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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import fi.oamk.petnotes.R

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar(containerColor = Color(0xFFEFEFEF)) {
        val items = listOf(
            "Home" to R.drawable.baseline_home_24, // Reference your drawable XML
            "Notes" to R.drawable.baseline_speaker_notes_24,
            "Map" to R.drawable.baseline_map_24,
            "Setting" to R.drawable.baseline_settings_24
        )

        items.forEach { (label, iconRes) ->
            NavigationBarItem(
                selected = false,
                onClick = {
                    // Handle navigation logic here
                    when (label) {
                        "Home" -> navController.navigate("home") // Navigate to home screen
                        "Notes" -> navController.navigate("notes") // Navigate to notes screen
                        "Map" -> navController.navigate("map") // Navigate to map screen
                        "Setting" -> navController.navigate("setting") // Navigate to setting screen
                    }
                },
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = label,
                          // Correctly using the Modifier.size
                        )
                    // Space between icon and text
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    }
}
