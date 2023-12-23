package ru.sitronics.velobike.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.sitronics.velobike.presentation.SimpleBottomSheet
import ru.sitronics.velobike.presentation.SimpleDialog
import ru.sitronics.velobike.presentation.auth.LoginScreen

@Composable
fun MainScreen(mainViewModel: MainViewModel = viewModel()) {
    val mainUiState by mainViewModel.mainUiState.collectAsStateWithLifecycle()

    if (mainUiState is MainUiState.Login) {
        LoginScreen() { mainViewModel.handleIntent(MainIntent.Logged) }
    } else {
        MainScreenInt(mainUiState) { intent -> mainViewModel.handleIntent(intent) }
    }
}

@Composable
fun MainScreenInt(
    uiState: MainUiState,
    onAction: (MainIntent) -> Unit,
) {
    Scaffold(
        bottomBar = { MyBottomAppBar() },
    ) { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding)) {
            MapViewContainer(uiState, onAction)

            MapTopLayerContainer(uiState, onAction)

/*
            if (uiState.dialogState) {
                SimpleDialog(
                    onDismissRequest = { onAction(MainIntent.Dialog(true)) },
                    onConfirmation = { onAction(MainIntent.Dialog(true)) },
                    dialogTitle = "Alert dialog example",
                    dialogText = "This is an example of an alert dialog with buttons.",
                    icon = Icons.Default.Info
                )
            }

            if (uiState.bottomSheetState) {
                SimpleBottomSheet(onDismissRequest = { onAction(MainIntent.BottomSheet) },
                    onClick = { onAction(MainIntent.BottomSheet) })
            }
*/
        }
    }
}

@Composable
fun MyBottomAppBar() {
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* do something */ }) {
                Icon(Icons.Filled.Check, contentDescription = "Localized description")
            }
            IconButton(onClick = { /* do something */ }) {
                Icon(Icons.Filled.Edit, contentDescription = "Localized description")
            }
            IconButton(onClick = { /* do something */ }) {
                Icon(Icons.Filled.Email, contentDescription = "Localized description")
            }
            IconButton(onClick = { /* do something */ }) {
                Icon(Icons.Filled.Info, contentDescription = "Localized description")
            }
        }
    }
}

// Doesn't work because of MapView
/*
@Preview(showBackground = true)
@Composable
fun MainPreview() {
    VelobikeTheme {
        MainScreenInt(MainUiState(), emptyList()) {}
    }
}
*/
