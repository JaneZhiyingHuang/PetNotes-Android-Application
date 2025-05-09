package fi.oamk.petnotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fi.oamk.petnotes.model.Pet
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.ui.unit.DpOffset
import fi.oamk.petnotes.ui.theme.PrimaryColor
import fi.oamk.petnotes.ui.theme.SecondaryColor


@Composable
fun SelectedPetDropdown(
    pets: List<Pet>,
    selectedPet: Pet?,
    onPetSelected: (Pet) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.Center)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 40.dp, top = 4.dp)
                .wrapContentWidth(Alignment.Start),
            verticalAlignment = Alignment.CenterVertically

        ) {
            Icon(
                imageVector = Icons.Outlined.Pets,
                contentDescription = "Pet Icon",
                modifier = Modifier.size(24.dp),
                tint = SecondaryColor
            )
            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = selectedPet?.name ?: "Select Pet",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select Pet")
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 40.dp, y = 0.dp),
            modifier = Modifier.background(PrimaryColor)

        ) {
            pets.forEach { pet ->
                DropdownMenuItem(
                    text = { Text(pet.name) },
                    onClick = {
                        onPetSelected(pet)
                        expanded = false
                    }
                )
            }
        }
    }
}
