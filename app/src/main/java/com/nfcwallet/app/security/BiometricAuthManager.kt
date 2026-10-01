package com.nfcwallet.app.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

// Manages BiometricPrompt authentication calls and hardware availability checks
class BiometricAuthManager(private val activity: FragmentActivity) {

    private val biometricManager = BiometricManager.from(activity)

    private val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    // Checks if biometric or device credentials (PIN/Pattern/Password) are available
    fun canAuthenticate(): Boolean {
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    // Returns user-friendly status message for device biometrics
    fun getBiometricStatusMessage(): String {
        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Biometrics or Device Lock available"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Device has no biometric hardware"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Biometric hardware is currently unavailable"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No biometrics or PIN configured in Android Settings"
            else -> "Biometric authentication not supported"
        }
    }

    // Launches BiometricPrompt and handles callbacks
    fun authenticate(
        title: String = "Unlock NFC Wallet",
        subtitle: String = "Authenticate to continue",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!canAuthenticate()) {
            onError(getBiometricStatusMessage())
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // Ignore silent user cancellations
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Authentication failed. Please try again.")
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        biometricPrompt.authenticate(promptInfo)
    }
}
