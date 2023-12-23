package ru.sitronics.velobike.presentation.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.sitronics.velobike.presentation.SimpleDialog
import ru.sitronics.velobike.ui.theme.VelobikeTheme

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = viewModel(),
    onLoginClosed: () -> Unit,
) {
    val loginUiState by loginViewModel.loginUiState.collectAsStateWithLifecycle()

    when (loginUiState) {
        is LoginUiState.Normal -> {
            val uiState = loginUiState as LoginUiState.Normal
            LoginScreenInt(uiState.login, uiState.password) { login, password ->
                loginViewModel.handleIntent(LoginIntent.OnLogin(login, password))
            }
        }
        is LoginUiState.Error -> {
            val uiState = loginUiState as LoginUiState.Error
            ShowErrorDialog(uiState.error) {
                loginViewModel.handleIntent(LoginIntent.OnLoginError)
            }
        }
        is LoginUiState.Close -> onLoginClosed()
    }
}

@Composable
fun LoginScreenInt(
    loginStr: String, passwordStr: String,
    onButtonClick: (String, String) -> Unit
) {
    var login by remember { mutableStateOf(loginStr) }
    var password by remember { mutableStateOf(passwordStr) }

    Box/*(Modifier.imePadding())*/ {
        Text(
            text = "Title",
            modifier = Modifier.align(Alignment.TopCenter)
        )

        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Login") },
            modifier = Modifier
                .width(300.dp)
                .align(Alignment.Center)
                .offset(y = (-30).dp),
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier
                .width(300.dp)
                .align(Alignment.Center)
                .offset(y = 30.dp),
            )

        Button(
            onClick = { onButtonClick(login, password) },
            modifier = Modifier
                .width(300.dp)
                .align(Alignment.BottomCenter)
        ) {
            Text(text = "Enter")
        }
    }
}

@Composable
fun ShowErrorDialog(error: String?, onClick: () -> Unit) {
    SimpleDialog(
        onDismissRequest = { onClick() },
        onConfirmation = { onClick() },
        dialogTitle = "Error",
        dialogText = error ?: "Error",
        icon = Icons.Default.Warning
    )

}

@Preview(showBackground = true, widthDp = 500, heightDp = 500)
@Composable
fun LoginScreenPreview() {
    VelobikeTheme {
        LoginScreenInt("", "") { _, _ -> }
    }
}
