package ru.sitronics.velobike.presentation.auth

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.auth.AuthData
import ru.sitronics.velobike.domain.auth.AuthManager
import ru.sitronics.velobike.domain.auth.AuthRepository
import ru.sitronics.velobike.ui.theme.VelobikeTheme

@RunWith(MockitoJUnitRunner::class)
class LoginScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Mock
    private val authManager = mock<AuthManager>()
    @Mock
    private val authRepository = mock<AuthRepository>()

    private lateinit var loginVM: LoginViewModel

    @Test
    fun enabledLoginButton() {
        checkLoginButton("l", "p", true)
    }

    @Test
    fun disabledLoginButton() {
        checkLoginButton("", "", false)
    }

    private fun checkLoginButton(login: String, password: String, isEnabled: Boolean) {
        composeTestRule.setContent {
            VelobikeTheme {
                LoginScreenInt(loginStr = login, passwordStr = password, onAction = {})
            }
        }

        var nodeText = composeTestRule.activity.getString(R.string.login)
        composeTestRule.onNodeWithText(nodeText).assertIsDisplayed()

        nodeText = composeTestRule.activity.getString(R.string.enter)
        composeTestRule.onNodeWithText(nodeText).assert(if (isEnabled) isEnabled() else isNotEnabled())
    }

    @Test
    fun openRegisterScreen() {
        Mockito.`when`(authRepository.getData()).thenReturn(AuthData("", ""))

        loginVM = LoginViewModel(authRepository, authManager, composeTestRule.activity)

        composeTestRule.setContent {
            VelobikeTheme {
                LoginScreen(loginVM) {}
            }
        }
        var nodeText = composeTestRule.activity.getString(R.string.register)
        composeTestRule.onNodeWithText(nodeText).performClick()
        nodeText = composeTestRule.activity.getString(R.string.registration)
        composeTestRule.onNodeWithText(nodeText).assertIsDisplayed()
    }
}
