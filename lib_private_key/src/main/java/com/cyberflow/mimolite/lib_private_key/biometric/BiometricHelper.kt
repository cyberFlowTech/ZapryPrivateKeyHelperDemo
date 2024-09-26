package com.cyberflow.mimolite.lib_private_key.biometric

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.annotation.UiThread
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.cyberflow.mimolite.lib_private_key.PrivateKeyLib
import com.cyberflow.mimolite.lib_private_key.PrivateKeyMgr
import com.cyberflow.mimolite.lib_private_key.R
import com.cyberflow.mimolite.lib_private_key.SETTING_KEY_ENABLE_LAUNCH_VERIFY
import com.cyberflow.mimolite.lib_private_key.SETTING_KEY_PAY_VERIFY
import com.cyberflow.mimolite.lib_private_key.SP_WALLET
import com.cyberflow.mimolite.lib_private_key.dialog.CommonConfirmDialog


object BiometricHelper {
    private const val TAG = "BiometricHelper"

    private val userId: String get() = PrivateKeyLib.userId

    private val sp
        get() = PrivateKeyLib.context.getSharedPreferences(
            SP_WALLET,
            Context.MODE_PRIVATE
        )

    enum class VerifyType(val value: Int) {
        NOT_SETTING(-1),
        PASSWORD(3),
        BIOMETRICS(1);

        companion object {
            fun of(value: Int): VerifyType {
                return values().find {
                    it.value == value
                } ?: NOT_SETTING
            }
        }
    }

    private const val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG

    fun checkBiometric(
        context: Context,
        authenticator: Int = authenticators,
        needAlertSet: Boolean = false
    ): Boolean {
        return when (BiometricManager.from(context).canAuthenticate(authenticator)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "App can authenticate using biometrics.")
                true
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.e(TAG, "No biometric features available on this device.")
                false
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.e(TAG, "Biometric features are currently unavailable.")
                false
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.e(TAG, "checkBiometric: BIOMETRIC_ERROR_NONE_ENROLLED")
                // Prompts the user to create credentials that your app accepts.
                if (needAlertSet) {
                    alertSetBiometric(context)
                }
                false
            }

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Log.e(TAG, "checkBiometric: BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED")
                false
            }

            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                Log.e(TAG, "checkBiometric: BIOMETRIC_ERROR_UNSUPPORTED")
                false
            }

            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                Log.e(TAG, "checkBiometric: BIOMETRIC_STATUS_UNKNOWN")
                false
            }

            else -> {
                false
            }
        }
    }

    fun alertSetBiometric(context: Context) {
        if (context !is FragmentActivity) {
            return
        }
        CommonConfirmDialog.Builder()
            .setTitle(R.string.common_tip)
            .setSubTitle(R.string.biometric_fingerprint_unset)
            .setConfirm(R.string.biometric_go_settings) {
                navigateToSystemFingerprintSettings(context)
            }
            .build()
            .show(context.supportFragmentManager, TAG)
    }

    private fun navigateToSystemFingerprintSettings(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_SETTINGS
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            // FIXME:
//            unLockBackground()
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "navigateToSystemFingerprintSettings: ${e.message}")
//            lockBackground()
        }
    }


    fun getVerifyType(): VerifyType {
        return if (userId.isNullOrEmpty()) {
            VerifyType.NOT_SETTING
        } else {
            sp.getInt(userId + SETTING_KEY_PAY_VERIFY, -1).run {
                VerifyType.of(this).run {
                    when (this) {
                        VerifyType.NOT_SETTING -> this
                        VerifyType.PASSWORD -> this
                        VerifyType.BIOMETRICS -> {
                            // 生物识别需要检测是否可用
                            if (!checkBiometric(PrivateKeyLib.context)) {

                                val payPwd = userId?.let {
                                    PrivateKeyMgr.getPayPassword(it)
                                }

                                if (payPwd.isNullOrEmpty()) {
                                    Log.e(TAG, "initView: ")
                                    VerifyType.NOT_SETTING
                                } else {
                                    VerifyType.PASSWORD
                                }
                            } else {
                                this
                            }
                        }
                    }
                }
            }
        }.apply {
            Log.i(TAG, "getVerifyType() returned: $this")
        }
    }

    fun setVerifyType(type: VerifyType) {
        if (userId.isNullOrEmpty()) {
            return
        } else {
            sp.edit().putInt(userId + SETTING_KEY_PAY_VERIFY, type.value).apply()
        }
    }

    fun getLaunchVerify(): Boolean {
        return if (userId.isNullOrEmpty()) {
            false
        } else {
            sp.getBoolean(userId + SETTING_KEY_ENABLE_LAUNCH_VERIFY, false)
        }
    }

    fun setLaunchVerify(isEnable: Boolean) {
        if (userId.isNullOrEmpty()) {
            return
        } else {
            sp.edit().putBoolean(userId + SETTING_KEY_ENABLE_LAUNCH_VERIFY, isEnable).apply()
        }
    }

    fun promptBiometric(
        activity: FragmentActivity,
        callback: BiometricPrompt.AuthenticationCallback
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt =
            BiometricPrompt(activity, executor, callback)
        activity.runOnUiThread {
            prompt(activity, authenticators, biometricPrompt)
        }
    }

    fun promptBiometric(
        activity: FragmentActivity,
        authenticator: Int,
        callback: BiometricPrompt.AuthenticationCallback
    ) {
        Log.i(
            TAG,
            "promptBiometric() called with: activity = $activity, authenticator = $authenticator, callback = $callback"
        )
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt =
            BiometricPrompt(activity, executor, callback)
        activity.runOnUiThread {
            prompt(activity, authenticator, biometricPrompt)
        }

    }

    fun promptBiometric(
        fragment: Fragment,
        authenticator: Int,
        callback: BiometricPrompt.AuthenticationCallback
    ) {
        val executor = ContextCompat.getMainExecutor(fragment.requireContext())
        val biometricPrompt = BiometricPrompt(fragment, executor, callback)

        prompt(fragment.requireContext(), authenticator, biometricPrompt)
    }

    @UiThread
    private fun prompt(context: Context, authenticator: Int, biometricPrompt: BiometricPrompt) {
        Log.i(
            TAG,
            "prompt() called with: context = $context, authenticator = $authenticator, biometricPrompt = $biometricPrompt"
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.biometric_dialog_title)) // 标题
            .setSubtitle(context.getString(R.string.biometric_dialog_subtitle)) // 副标题
            .setDescription(context.getString(R.string.biometric_dialog_desc)) // 描述
            .setNegativeButtonText(context.getString(R.string.common_cancel)) // 取消按钮
            // 生物识别类
            .setAllowedAuthenticators(authenticator)
            .setConfirmationRequired(false)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun promptStrongBiometric(
        activity: FragmentActivity,
        callback: BiometricPrompt.AuthenticationCallback
    ) {
        Log.i(
            TAG,
            "promptStrongBiometric() called with: activity = $activity, callback = $callback"
        )
        promptBiometric(activity, BiometricManager.Authenticators.BIOMETRIC_STRONG, callback)
    }

    fun promptWeakBiometric(
        activity: FragmentActivity,
        callback: BiometricPrompt.AuthenticationCallback
    ) {
        Log.i(
            TAG,
            "promptWeakBiometric() called with: activity = $activity, callback = $callback"
        )
        promptBiometric(activity, BiometricManager.Authenticators.BIOMETRIC_WEAK, callback)
    }


    /**
     * 检查是否支持强密码生物识别
     * @return Pair<是否支持强密码生物识别，是否已经注册>
     */
    fun checkStrongBiometric(context: Context): Pair<Boolean, Boolean> {
        return when (BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                true to true
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                true to false
            }

            else -> {
                false to false
            }
        }
    }

    /**
     * 检查是否支持弱密码生物识别
     * @return Pair<是否支持弱密码生物识别，是否已经注册>
     */
    fun checkWeakBiometric(context: Context): Pair<Boolean, Boolean> {
        return when (BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                true to true
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                true to false
            }

            else -> {
                false to false
            }
        }
    }

    fun logout() {
        // 退出登陆不清除身份验证配置，再次登陆直接使用,所以只重置锁App的数据就行了
        setLaunchVerify(false)
    }
}