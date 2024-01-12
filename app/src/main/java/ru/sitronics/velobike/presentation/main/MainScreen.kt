package ru.sitronics.velobike.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.sitronics.velobike.R
import ru.sitronics.velobike.presentation.auth.LoginScreen
import ru.sitronics.velobike.presentation.help.HelpScreen
import ru.sitronics.velobike.presentation.history.HistoryScreen
import ru.sitronics.velobike.presentation.map.MapScreen
import ru.sitronics.velobike.presentation.profile.ProfileScreen
import ru.sitronics.velobike.ui.theme.VelobikeTheme

sealed class AppScreen {
    object Map : AppScreen()
    object History : AppScreen()
    object Profile : AppScreen()
    object Help : AppScreen()
}

@Composable
fun MainScreen(mainViewModel: MainViewModel = viewModel()) {
    val mainUiState by mainViewModel.mainUiState.collectAsStateWithLifecycle()

    when (mainUiState ) {
        is MainUiState.Splash -> SplashScreen()
        is MainUiState.Login -> LoginScreen { mainViewModel.handleIntent(MainIntent.Logged) }
        else -> MainScreenInt()
    }
}

@Composable
fun MainScreenInt() {
    var screen: AppScreen by remember { mutableStateOf(AppScreen.Map) }

    Scaffold(
        bottomBar = { MainBottomBar { screen = it } },
    ) { contentPadding ->
        when (screen) {
            is AppScreen.Map -> MapScreen(contentPadding)
            is AppScreen.History -> HistoryScreen(contentPadding)
            is AppScreen.Profile -> ProfileScreen(contentPadding)
            is AppScreen.Help -> HelpScreen(contentPadding)
        }
    }
}

@Composable
fun MainBottomBar(onAction: (AppScreen) -> Unit) {
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onAction(AppScreen.Map) }) {
                Icon(painterResource(R.drawable.map), contentDescription = "")
            }
            IconButton(onClick = { onAction(AppScreen.History) }) {
                Icon(painterResource(R.drawable.bicycle), contentDescription = "")
            }
            IconButton(onClick = { onAction(AppScreen.Profile) }) {
                Icon(painterResource(R.drawable.profile), contentDescription = "")
            }
            IconButton(onClick = { onAction(AppScreen.Help) }) {
                Icon(painterResource(R.drawable.dots_menu), contentDescription = "")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    VelobikeTheme {
        MainScreenInt()
    }
}
