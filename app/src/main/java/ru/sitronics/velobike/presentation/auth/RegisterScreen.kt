package ru.sitronics.velobike.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.auth.RegisterData
import ru.sitronics.velobike.tools.BackPressHandler

@Composable
fun RegisterScreen(onDismiss: () -> Unit, onClick: (RegisterData) -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var agree by remember { mutableStateOf(false) }

    Box/*(Modifier.imePadding())*/ {
        Image(
            painter = painterResource(id = R.drawable.register_image),
            contentDescription = "",
            modifier = Modifier.align(Alignment.TopCenter)
        )

        Column (
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(top = 100.dp)
                .background(Color.White)
        ) {
            Text(
                text = stringResource(R.string.registration),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp, bottom = 16.dp)
            )

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text(stringResource(R.string.name)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier
                    .width(300.dp)
                    .align(Alignment.CenterHorizontally)
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text(stringResource(R.string.surname)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier
                    .width(300.dp)
                    .align(Alignment.CenterHorizontally)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(stringResource(R.string.phone)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .width(300.dp)
                    .align(Alignment.CenterHorizontally)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .width(300.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 64.dp),
            ) {
                Checkbox(
                    checked = agree,
                    onCheckedChange = { agree = it }
                )
                Text(
                    text = stringResource(R.string.agree),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }

        Button(
            onClick = {
                onClick(RegisterData(
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phone,
                    email = email,
                ))
            },
            enabled = checkFilling(firstName, lastName, phone, email, agree),
            modifier = Modifier
                .width(300.dp)
                .align(Alignment.BottomCenter)
        ) {
            Text(text = stringResource(R.string.register))
        }
    }

    BackPressHandler(onBackPressed = onDismiss)
}

private fun checkFilling(name: String, surname: String, phone: String,
                         email: String, agree: Boolean) : Boolean =
    name.isNotEmpty() && surname.isNotEmpty() &&
    phone.isNotEmpty() && email.isNotEmpty() && agree
