package fi.oamk.petnotes.ui

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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import fi.oamk.petnotes.viewmodel.AddNewPetViewModel
import fi.oamk.petnotes.viewmodel.HomeScreenViewModel
import fi.oamk.petnotes.R
import fi.oamk.petnotes.ui.theme.InputColor
import java.util.*
import android.app.DatePickerDialog
import android.net.Uri
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.storage.FirebaseStorage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import fi.oamk.petnotes.ui.theme.CardBG
import fi.oamk.petnotes.ui.theme.LightYellow
import fi.oamk.petnotes.ui.theme.PrimaryColor
import fi.oamk.petnotes.ui.theme.SecondaryColor
import kotlinx.coroutines.launch
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.window.DialogProperties
import fi.oamk.petnotes.ui.theme.Default
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewPetScreen(
    navController: NavController,
    petId: String? = null,
    homeScreenViewModel: HomeScreenViewModel
) {
    val addnewPetViewModel: AddNewPetViewModel = viewModel()

    var petName by remember { mutableStateOf("") }
    var petBreed by remember { mutableStateOf("") }
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

    // for upload image to firebase storage : img -> url
    var imageUrl by remember { mutableStateOf<Uri?>(null) }
    // for fetch image from firebase storage: url -> img
    var petImageUri by remember { mutableStateOf("") }

    // edit mode-> fetch datas from homescreenviewmodel with petId
    if (petId != null) {
        LaunchedEffect(petId) {
            val pets = homeScreenViewModel.fetchPets()
            val pet = pets.find { it.id == petId }
            pet?.let {
                petName = it.name
                petGender = it.gender
                petSpecie = it.specie
                petDateOfBirth = it.dateOfBirth
                petBreed = it.breed
                petMedicalCondition = it.medicalCondition
                petMicrochipNumber = it.microchipNumber
                petInsuranceCompany = it.insuranceCompany
                petInsuranceNumber = it.insuranceNumber
                petImageUri = it.petImageUri
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUrl = uri
    }

    Scaffold(
        topBar = {
            Surface(
//                shape = RoundedCornerShape(bottomStart = 15.dp, bottomEnd = 15.dp),
            ) {
                TopAppBar(
                    title = {
                            Text(
                                stringResource(R.string.add_a_new_pet),
                                fontWeight = FontWeight.Bold,
                                color = SecondaryColor,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(start = 95.dp)
                            )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryColor)
                )
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CardBG)
                .padding(paddingValues)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(18.dp))

            // avatar
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(107.dp)
                        .clip(CircleShape)
                        .background(InputColor)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    imageUrl?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Pet Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop                        )
                    } ?: if (petImageUri.isNotEmpty()) {  //show current avatar
                        Image(
                            painter = rememberAsyncImagePainter(petImageUri),
                            contentDescription = "Existing Pet Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.add_a_photo_24px),
                            contentDescription = "Upload Avatar",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Reusable labeled text fields
            LabeledTextField(
                label = stringResource(R.string.pet_name),
                value = petName,
                onValueChange = { petName = it }
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Select gender
            Text(
                text = stringResource(R.string.gender),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .padding(start = 73.dp)
            )
            Row(
                modifier = Modifier
                    .padding(start = 69.dp)
                    .width(280.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
            ) {
                Button(
                    onClick = { petGender = "male" },
                    modifier = Modifier
                        .height(35.dp)
                        .weight(1f),
                    contentPadding = PaddingValues(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (petGender.lowercase() == "male") LightYellow else InputColor,
                        contentColor = Color.Black
                    )
                ) {
                    Text(context.getString(R.string.male), fontSize = 14.sp)
                }
                Button(
                    onClick = { petGender = "female" },
                    modifier = Modifier
                        .height(35.dp)
                        .weight(1f),
                    contentPadding = PaddingValues(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (petGender.lowercase() == "female") LightYellow else InputColor,
                        contentColor = Color.Black
                    )
                ) {
                    Text(context.getString(R.string.female), fontSize = 14.sp)
                }
                Button(
                    onClick = { petGender = "other" },
                    modifier = Modifier
                        .height(35.dp)
                        .weight(1f),
                    contentPadding = PaddingValues(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (petGender.lowercase() == "other") LightYellow else InputColor,
                        contentColor = Color.Black
                    )
                ) {
                    Text(context.getString(R.string.other), fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            LabeledTextField(
                label = stringResource(R.string.specie),
                value = petSpecie,
                onValueChange = { petSpecie = it }
            )
            Spacer(modifier = Modifier.height(12.dp))

            DatePickerField(
                label = stringResource(R.string.date_of_birth),
                date = petDateOfBirth,
                onDateSelected = { petDateOfBirth = it }
            )

            LabeledTextField(
                label = stringResource(R.string.breed),
                value = petBreed,
                onValueChange = { petBreed = it }
            )
            Spacer(modifier = Modifier.height(12.dp))

            LabeledTextField(
                label = stringResource(R.string.medical_condition),
                value = petMedicalCondition,
                onValueChange = { petMedicalCondition = it }
            )
            Spacer(modifier = Modifier.height(12.dp))

            LabeledTextField(
                label = stringResource(R.string.microchip_id_number),
                value = petMicrochipNumber,
                onValueChange = { petMicrochipNumber = it }
            )
            Spacer(modifier = Modifier.height(12.dp))

            LabeledTextField(
                label = stringResource(R.string.insurance_company),
                value = petInsuranceCompany,
                onValueChange = { petInsuranceCompany = it }
            )
            Spacer(modifier = Modifier.height(12.dp))

            LabeledTextField(
                label = stringResource(R.string.insurance_number),
                value = petInsuranceNumber,
                onValueChange = { petInsuranceNumber = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (petName.isNotBlank() &&
                        petGender.isNotBlank() &&
                        petSpecie.isNotBlank() &&
                        petDateOfBirth.isNotBlank()
                    ) {
                        isLoading = true

                        if (imageUrl != null) {
                            uploadImageToFirebase(imageUrl!!, onSuccess = { remoteUrl ->
                                petImageUri = remoteUrl
                                coroutineScope.launch {
                                    if (petId != null) {
                                        //edit mode
                                        addnewPetViewModel.updatePet(
                                            petId,
                                            petName,
                                            petGender,
                                            petSpecie,
                                            petDateOfBirth,
                                            petBreed,
                                            petMedicalCondition,
                                            petMicrochipNumber,
                                            petInsuranceCompany,
                                            petInsuranceNumber,
                                            petImageUri
                                        ) {
                                            isLoading = false
                                            navController.popBackStack()
                                        }
                                    } else {
                                        // add new mode
                                        addnewPetViewModel.addPet(
                                            petName,
                                            petGender,
                                            petSpecie,
                                            petDateOfBirth,
                                            petBreed,
                                            petMedicalCondition,
                                            petMicrochipNumber,
                                            petInsuranceCompany,
                                            petInsuranceNumber,
                                            petImageUri
                                        ) {
                                            isLoading = false
                                            navController.popBackStack()
                                        }
                                    }
                                }
                            }, onFailure = { exception ->
                                isLoading = false
                                Toast.makeText(context, "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                            })
                        } else {
                            // update without new image
                            if (petId != null && petImageUri.isNotEmpty()) {
                                coroutineScope.launch {
                                    addnewPetViewModel.updatePet(
                                        petId,
                                        petName,
                                        petGender,
                                        petSpecie,
                                        petDateOfBirth,
                                        petBreed,
                                        petMedicalCondition,
                                        petMicrochipNumber,
                                        petInsuranceCompany,
                                        petInsuranceNumber,
                                        petImageUri
                                    ) {
                                        isLoading = false
                                        navController.popBackStack()
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.please_fill_all_mandatory_fields_and_upload_avatar),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.please_fill_all_mandatory_fields_and_upload_avatar),
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
                    containerColor = if (isLoading) InputColor else Color.Black,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    text = if (isLoading) stringResource(R.string.saving) else if (petId != null) stringResource(
                        R.string.update_pet
                    ) else stringResource(R.string.add_new_pet)
                )
            }
        }
    }
}

// style management of InputFields
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
                .defaultMinSize(minHeight = 56.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .background(InputColor, RoundedCornerShape(40.dp)),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
            shape = RoundedCornerShape(40.dp),
            singleLine = false,
            maxLines = Int.MAX_VALUE,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SecondaryColor,
                unfocusedBorderColor = Color.White
            )
        )
    }
}

// DoB calendar datepicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    date: String,
    onDateSelected: (String) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    // State to hold the picked date in millis
    val pickerState = rememberDatePickerState()

    Column {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 73.dp, bottom = 1.dp)
                .fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .width(280.dp)
                .height(56.dp)
                .padding(top = 3.dp)
                .background(InputColor, RoundedCornerShape(40.dp))
                .clickable { showPicker = true }
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.ifEmpty { stringResource(R.string.select_date) },
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.width(400.dp),
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        // format to dd/MM/yyyy
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        sdf.timeZone = TimeZone.getDefault()
                        onDateSelected(sdf.format(Date(millis)))
                    }
                    showPicker = false
                }) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            },
            text = {
                DatePicker(
                    state = pickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor               = Default,
//                        titleContentColor            = MaterialTheme.colorScheme.primary,
//                        headlineContentColor         = MaterialTheme.colorScheme.onSurface,
//                        selectedDayContainerColor    = MaterialTheme.colorScheme.secondary,
//                        selectedDayContentColor      = MaterialTheme.colorScheme.onSecondary,
//                        todayDateBorderColor         = MaterialTheme.colorScheme.primary,
//                        todayContentColor            = MaterialTheme.colorScheme.primary
                    )
                )
            }
        )
    }
}
// handle avatar photo upload logic:
// to fire storage-> get imageUrl -> store Url in firebase -> use Url to fetch image
fun uploadImageToFirebase(uri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference
    val fileRef = storageRef.child("pet_images/${UUID.randomUUID()}.jpg")
    fileRef.putFile(uri)
        .addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                onSuccess(downloadUri.toString())
            }
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}
