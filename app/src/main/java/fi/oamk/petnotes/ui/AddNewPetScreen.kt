package fi.oamk.petnotes.ui

import android.renderscript.ScriptGroup.Input
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import fi.oamk.petnotes.viewmodel.AddNewPetViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import fi.oamk.petnotes.ui.theme.ButtonColor
import fi.oamk.petnotes.ui.theme.InputColor
import fi.oamk.petnotes.R

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.material3.*

import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewPetScreen(navController: NavController) {
    val addnewPetViewModel: AddNewPetViewModel = viewModel()  // Provide the viewModel

    var petName by remember { mutableStateOf("") }
    var petBreed by remember { mutableStateOf("") }
    var petAge by remember { mutableStateOf("") }
    var petGender by remember { mutableStateOf("") }
    var petSpecie by remember { mutableStateOf("") }
    var petDateOfBirth by remember { mutableStateOf("") }
    var petMedicalCondition by remember { mutableStateOf("") }
    var petMicrochipNumber by remember { mutableStateOf("") }
    var petInsuranceCompany by remember { mutableStateOf("") }
    var petInsuranceNumber by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current // Access context in Compose

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFEFEFEF))
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(107.dp)
                        .clip(CircleShape)
                        .background(InputColor) // Placeholder color if no image is uploaded
                        .clickable {

                        },
                    contentAlignment = Alignment.Center // Center the icon inside the circle
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.add_a_photo_24px),
                        contentDescription = "Upload Avatar",
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Reusable labeled text fields
            LabeledTextField(
                label = "Pet Name",
                value = petName,
                onValueChange = { petName = it }
            )
            Spacer(modifier = Modifier.height(12.dp))

            //Select gender
            Text(
                text = "Gender",
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .padding(start = 73.dp)
            )
            Row(
                modifier = Modifier
                    .padding(start = 50.dp)
                    .width(280.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
            ) {
                Button(
                    onClick = { petGender = "Male" },
                    modifier = Modifier
                        .height(35.dp)
                        .weight(1f),
                    contentPadding = PaddingValues(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (petGender == "Male") ButtonColor else InputColor,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Male",
                        fontSize = 14.sp )
                }
                Button(
                    onClick = { petGender = "Female" },
                    modifier = Modifier
                        .height(35.dp)
                        .weight(1f),
                    contentPadding = PaddingValues(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (petGender == "Female") ButtonColor else InputColor,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Female",
                        fontSize = 14.sp )
                }
                Button(
                    onClick = { petGender = "Other" },
                    modifier = Modifier
                        .height(35.dp)
                        .weight(1f),
                    contentPadding = PaddingValues(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (petGender == "Other") ButtonColor else InputColor,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Other",
                        fontSize = 14.sp )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            LabeledTextField(
                label = "Specie",
                value = petSpecie,
                onValueChange = { petSpecie = it }
            )
            Spacer(modifier = Modifier.height(12.dp))

//            LabeledTextField(
//                label = "Date of Birth",
//                value = petDateOfBirth,
//                onValueChange = { petDateOfBirth = it }
//            )
//            Spacer(modifier = Modifier.height(12.dp))
//
            DatePickerField(
                label = "Date of Birth",
                date = petDateOfBirth,
                onDateSelected = { petDateOfBirth = it }
            )

            LabeledTextField(
                label = "*Breed",
                value = petBreed,
                onValueChange = { petBreed = it }
            )
            Spacer(modifier = Modifier.height(12.dp))

            LabeledTextField(
                label = "*Medical Condition",
                value = petMedicalCondition,
                onValueChange = { petMedicalCondition = it }
            )
            Spacer(modifier = Modifier.height(12.dp))

            LabeledTextField(
                label = "*Microchip/ID Number",
                value = petMicrochipNumber,
                onValueChange = { petMicrochipNumber = it }
            )
            Spacer(modifier = Modifier.height(12.dp))

            LabeledTextField(
                label = "*Insurance Company",
                value = petInsuranceCompany,
                onValueChange = { petInsuranceCompany = it }
            )
            Spacer(modifier = Modifier.height(12.dp))

            LabeledTextField(
                label = "*Insurance Number",
                value = petInsuranceNumber,
                onValueChange = { petInsuranceNumber = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (petName.isNotBlank() &&
                        petGender.isNotBlank() &&
                        petSpecie.isNotBlank() &&
                        petDateOfBirth.isNotBlank()) {

                        // Convert petAge to Int
                        val petAgeInt = petAge.toIntOrNull()
                        if (petAgeInt != null) {
                            isLoading = true
                            coroutineScope.launch {
                                addnewPetViewModel.addPet(
                                    petName,
                                    petGender,
                                    petSpecie,
                                    petDateOfBirth,
                                    petBreed,
                                    petMedicalCondition,
                                    petMicrochipNumber,
                                    petInsuranceCompany,
                                    petInsuranceNumber
                                ) {
                                    isLoading = false
                                    navController.popBackStack()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Please fill all mandatory fields",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .padding(top = 15.dp, bottom = 50.dp)
                    .width(280.dp)
                    .height(48.dp)
                    .align(Alignment.CenterHorizontally),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLoading) InputColor else ButtonColor,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(if (isLoading) "Saving..." else "Add New Pet")

            }
        }
    }
}

//style management of InputFields
@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 1.dp)
                .padding(start = 73.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .width(280.dp)
                .padding(top = 3.dp)
                .height(56.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .background(InputColor, RoundedCornerShape(40.dp)),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
            shape = RoundedCornerShape(40.dp)
        )
    }
}

@Composable
fun DatePickerField(
    label: String,
    date: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            onDateSelected(selectedDate)
        },
        year, month, day
    )

    Column {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 1.dp)
                .padding(start = 73.dp)
                .fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .width(280.dp)
                .padding(top = 3.dp)
                .height(56.dp)
                .background(
                    color = InputColor,
                    shape = RoundedCornerShape(40.dp)
                )
                .clickable { datePickerDialog.show() }
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.ifEmpty { "Select Date" },
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}
