package com.cyberflow.mimolite.lib_private_key.biometric

import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationCallback
import com.cyberflow.mimolite.lib_private_key.PrivateKeyApp
import com.cyberflow.mimolite.lib_private_key.R

abstract class BiometricCallback : AuthenticationCallback() {
    private val tag = javaClass.name

    companion object {
        const val INCORRECT_PASSWORD = "Incorrect password"
        const val ERROR_CODE_BIOMETRIC_FAILED = "-10001"
        const val ERROR_CODE_PASSWORD_FAILED = "-10002"
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        Log.i(
            tag,
            "onAuthenticationError() called with: errorCode = $errorCode, errString = $errString"
        )
    }

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        Log.i(tag, "onAuthenticationSucceeded() called with: result = $result")
    }

    open fun onPasswordSucceeded() {
        Log.e(tag, "onPasswordSucceeded")
    }

    open fun onPasswordFailed() {
        Log.e(tag, "onPasswordFailed: $INCORRECT_PASSWORD")
        Toast.makeText(
            PrivateKeyApp.context,
            R.string.biometric_error_password,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        Log.i(tag, "onAuthenticationFailed() called")
    }
}