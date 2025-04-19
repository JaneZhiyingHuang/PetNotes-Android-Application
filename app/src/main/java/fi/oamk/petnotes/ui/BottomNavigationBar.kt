package fi.oamk.petnotes.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import fi.oamk.petnotes.R
import fi.oamk.petnotes.model.NavigationItem
import fi.oamk.petnotes.ui.theme.LineColor
import fi.oamk.petnotes.ui.theme.PrimaryColor
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import fi.oamk.petnotes.ui.theme.InputColor
import fi.oamk.petnotes.ui.theme.LightRed
import fi.oamk.petnotes.ui.theme.SecondaryColor

@Composable
fun BottomNavigationBar(navController: NavController) {
    Column {
        HorizontalDivider(thickness = 1.dp, color = LineColor)

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        NavigationBar(containerColor = PrimaryColor) {
            val items = listOf(
                NavigationItem("home", R.string.home, R.drawable.baseline_home_24),
                NavigationItem("notes", R.string.notes, R.drawable.import_contacts_24px),
                NavigationItem("map", R.string.map, R.drawable.explore_24px),
                NavigationItem("setting", R.string.setting, R.drawable.baseline_settings_24)
            )

            items.forEach { item ->
                val selected = currentRoute == item.route

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            navController.navigate(item.route)
                        }
                    },
                    icon = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = item.iconResId),
                                contentDescription = stringResource(item.labelResId),
                            )
                            Text(
                                text = stringResource(item.labelResId),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SecondaryColor,
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = SecondaryColor,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
