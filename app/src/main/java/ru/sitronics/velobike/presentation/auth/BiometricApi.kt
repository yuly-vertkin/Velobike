package ru.sitronics.velobike.presentation.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import ru.sitronics.velobike.R

fun biometricAuth(activity: FragmentActivity, onResult: (Boolean) -> Unit) {
    val canAuthenticate = BiometricManager.from(activity)
        .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
    if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
        authenticate(activity, onResult)
    } else {
        onResult(false)
    }
}

private fun authenticate(activity: FragmentActivity, onResult: (Boolean) -> Unit) {
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onResult(true)
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onResult(false)
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onResult(false)
        }
    }
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor, callback)
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(activity.getString(R.string.biometric_auth))
        .setNegativeButtonText(activity.getString(R.string.use_credentials))
        .build()

    biometricPrompt.authenticate(promptInfo)
}
