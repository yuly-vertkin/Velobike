package ru.sitronics.velobike.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.sitronics.velobike.R
import ru.sitronics.velobike.presentation.SimpleDialog
import ru.sitronics.velobike.ui.theme.VelobikeTheme

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = viewModel(),
    onLoginClosed: () -> Unit,
) {
    val loginUiState by loginViewModel.loginUiState.collectAsStateWithLifecycle()
    val onAction: (LoginIntent) -> Unit = { intent -> loginViewModel.handleIntent(intent) }

    when (loginUiState) {
        is LoginUiState.Normal -> {
            val uiState = loginUiState as LoginUiState.Normal
            LoginScreenInt(uiState.login, uiState.password) { intent ->
                onAction(intent)
            }
        }
        is LoginUiState.ShowMessage -> {
            val uiState = loginUiState as LoginUiState.ShowMessage
            ShowMessageDialog(uiState.msg) {
                onAction(LoginIntent.OnMessage)
            }
        }
        is LoginUiState.ShowRegister -> {
            RegisterScreen(onDismiss = { onAction(LoginIntent.OnMessage) }) {
                onAction(LoginIntent.OnRegister(it))
            }
        }
        is LoginUiState.Close -> onLoginClosed()
    }
}

@Composable
fun LoginScreenInt(
    loginStr: String, passwordStr: String,
    onAction: (LoginIntent) -> Unit
) {
    val context = LocalContext.current
    var login by remember { mutableStateOf(loginStr) }
    var password by remember { mutableStateOf(passwordStr) }
    var loading by remember { mutableStateOf(false) }

    Box/*(Modifier.imePadding())*/ {
        Image(
            painter = painterResource(id = R.drawable.login_image),
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
                text = context.getString(R.string.entrance),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp, bottom = 16.dp)
            )

            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = { Text(context.getString(R.string.login)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .width(300.dp)
                    .align(Alignment.CenterHorizontally)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(context.getString(R.string.password)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .width(300.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        Column (
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .background(Color.White)
        ) {
            Button(
                onClick = { loading = true; onAction(LoginIntent.OnLogin(login, password)) },
                enabled = !loading && login.isNotEmpty() && password.isNotEmpty(),
                modifier = Modifier
                    .width(300.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = context.getString(R.string.enter))
            }

            Button(
                onClick = { onAction(LoginIntent.ShowRegister) },
                modifier = Modifier
                    .width(300.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = context.getString(R.string.register))
            }
        }

        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(64.dp)
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
fun ShowMessageDialog(msg: String?, onClick: () -> Unit) {
    val context = LocalContext.current

    SimpleDialog(
        onDismissRequest = { onClick() },
        onConfirmation = { onClick() },
        dialogTitle = context.getString(R.string.warning),
        dialogText = msg ?: context.getString(R.string.warning),
        icon = Icons.Default.Warning
    )
}

@Preview(showBackground = true/*, widthDp = 500, heightDp = 500*/)
@Composable
fun LoginScreenPreview() {
    VelobikeTheme {
        LoginScreenInt("", "") { _ -> }
    }
}
