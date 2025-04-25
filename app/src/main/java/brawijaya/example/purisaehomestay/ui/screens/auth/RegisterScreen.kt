package brawijaya.example.purisaehomestay.ui.screens.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.ui.navigation.Screen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isNameError by remember { mutableStateOf(false) }
    var isPhoneNumberError by remember { mutableStateOf(false) }
    var isEmailError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }
    var isConfirmPasswordError by remember { mutableStateOf(false) }

    var nameErrorMessage by remember { mutableStateOf("") }
    var phoneNumberErrorMessage by remember { mutableStateOf("") }
    var emailErrorMessage by remember { mutableStateOf("") }
    var passwordErrorMessage by remember { mutableStateOf("") }
    var confirmPasswordErrorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDarkGreen
                ),
                title = {
                    Text(
                        text = "Daftar",
                        color = PrimaryGold,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier
                            .padding(start = 2.dp)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Profile.route)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = PrimaryGold
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            RegisterContent(
                name = name,
                onNameChange = {
                    name = it
                    isNameError = false
                },
                phoneNumber = phoneNumber,
                onPhoneNumberChange = {
                    phoneNumber = it
                    isPhoneNumberError = false
                },
                email = email,
                onEmailChange = {
                    email = it
                    isEmailError = false
                },
                password = password,
                onPasswordChange = {
                    password = it
                    isPasswordError = false
                },
                confirmPassword = confirmPassword,
                onConfirmPasswordChange = {
                    confirmPassword = it
                    isConfirmPasswordError = false
                },
                isNameError = isNameError,
                isPhoneNumberError = isPhoneNumberError,
                isEmailError = isEmailError,
                isPasswordError = isPasswordError,
                isConfirmPasswordError = isConfirmPasswordError,
                nameErrorMessage = nameErrorMessage,
                phoneNumberErrorMessage = phoneNumberErrorMessage,
                emailErrorMessage = emailErrorMessage,
                passwordErrorMessage = passwordErrorMessage,
                confirmPasswordErrorMessage = confirmPasswordErrorMessage,
                onRegisterClick = {

                    if (name.isEmpty()) {
                        isNameError = true
                        nameErrorMessage = "Nama tidak boleh kosong"
                    } else {
                        isNameError = false
                    }

                    if (phoneNumber.isEmpty()) {
                        isPhoneNumberError = true
                        phoneNumberErrorMessage = "Nomor telepon tidak boleh kosong"
                    } else if (!isValidPhoneNumber(phoneNumber)) {
                        isPhoneNumberError = true
                        phoneNumberErrorMessage = "Format nomor telepon tidak valid"
                    } else {
                        isPhoneNumberError = false
                    }

                    if (email.isEmpty()) {
                        isEmailError = true
                        emailErrorMessage = "Email tidak boleh kosong"
                    } else if (!isValidEmail(email)) {
                        isEmailError = true
                        emailErrorMessage = "Format email tidak valid"
                    } else {
                        isEmailError = false
                    }

                    if (password.isEmpty()) {
                        isPasswordError = true
                        passwordErrorMessage = "Password tidak boleh kosong"
                    } else if (password.length < 6) {
                        isPasswordError = true
                        passwordErrorMessage = "Password minimal 6 karakter"
                    } else {
                        isPasswordError = false
                    }

                    if (confirmPassword.isEmpty()) {
                        isConfirmPasswordError = true
                        confirmPasswordErrorMessage = "Konfirmasi password tidak boleh kosong"
                    } else if (confirmPassword != password) {
                        isConfirmPasswordError = true
                        confirmPasswordErrorMessage = "Password tidak cocok"
                    } else {
                        isConfirmPasswordError = false
                    }

                    if (!isNameError && !isPhoneNumberError &&
                        !isEmailError && !isPasswordError && !isConfirmPasswordError) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    return email.matches(emailRegex.toRegex())
}

private fun isValidPhoneNumber(phone: String): Boolean {
    val phoneRegex = "^[0-9]{10,13}$"
    return phone.matches(phoneRegex.toRegex())
}

@Composable
fun RegisterContent(
    name: String,
    onNameChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    isNameError: Boolean,
    isPhoneNumberError: Boolean,
    isEmailError: Boolean,
    isPasswordError: Boolean,
    isConfirmPasswordError: Boolean,
    nameErrorMessage: String,
    phoneNumberErrorMessage: String,
    emailErrorMessage: String,
    passwordErrorMessage: String,
    confirmPasswordErrorMessage: String,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = {
                Text(
                    "Nama Lengkap",
                    style = MaterialTheme.typography.labelMedium,
                )
                    },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = PrimaryGold.copy(alpha = 0.5f),
                focusedBorderColor = PrimaryGold,
                unfocusedLabelColor = Color.LightGray,
                focusedLabelColor = Color.Black,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(8.dp),
            isError = isNameError,
            supportingText = {
                if (isNameError) {
                    Text(
                        text = nameErrorMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = { 
                Text(
                    "Nomor Telepon",
                    style = MaterialTheme.typography.labelMedium,
                )
                    },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = PrimaryGold.copy(alpha = 0.5f),
                focusedBorderColor = PrimaryGold,
                unfocusedLabelColor = Color.LightGray,
                focusedLabelColor = Color.Black,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(8.dp),
            isError = isPhoneNumberError,
            supportingText = {
                if (isPhoneNumberError) {
                    Text(
                        text = phoneNumberErrorMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Alamat Email", style = MaterialTheme.typography.labelMedium) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = PrimaryGold.copy(alpha = 0.5f),
                focusedBorderColor = PrimaryGold,
                unfocusedLabelColor = Color.LightGray,
                focusedLabelColor = Color.Black,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(8.dp),
            isError = isEmailError,
            supportingText = {
                if (isEmailError) {
                    Text(
                        text = emailErrorMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Buat Password", style = MaterialTheme.typography.labelMedium) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Sembunyikan Password" else "Tampilkan Password",
                        tint = PrimaryGold
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = PrimaryGold.copy(alpha = 0.5f),
                focusedBorderColor = PrimaryGold,
                unfocusedLabelColor = Color.LightGray,
                focusedLabelColor = Color.Black,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(8.dp),
            isError = isPasswordError,
            supportingText = {
                if (isPasswordError) {
                    Text(
                        text = passwordErrorMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Konfirmasi Password", style = MaterialTheme.typography.labelMedium,) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (confirmPasswordVisible) "Sembunyikan Password" else "Tampilkan Password",
                        tint = PrimaryGold
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = PrimaryGold.copy(alpha = 0.5f),
                focusedBorderColor = PrimaryGold,
                unfocusedLabelColor = Color.LightGray,
                focusedLabelColor = Color.Black,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(8.dp),
            isError = isConfirmPasswordError,
            supportingText = {
                if (isConfirmPasswordError) {
                    Text(
                        text = confirmPasswordErrorMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRegisterClick,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Daftar",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sudah punya akun?",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            TextButton(
                onClick = onLoginClick
            ) {
                Text(
                    text = "Masuk",
                    color = PrimaryGold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}