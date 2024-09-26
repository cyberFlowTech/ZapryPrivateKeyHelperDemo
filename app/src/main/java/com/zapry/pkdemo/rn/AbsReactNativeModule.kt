package com.zapry.pkdemo.rn

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.cyberflow.mimolite.lib_private_key.biometric.BiometricCallback
import com.cyberflow.mimolite.lib_private_key.biometric.BiometricHelper
import com.cyberflow.mimolite.lib_private_key.PrivateKeyMgr
import com.cyberflow.mimolite.lib_private_key.dialog.AuthDialog
import com.cyberflow.mimolite.lib_private_key.ext.IoScope
import com.cyberflow.mimolite.lib_private_key.model.PayAuth
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.gson.reflect.TypeToken
import com.zapry.pkdemo.R
import com.zapry.pkdemo.TEST_USER_ID
import com.zapry.pkdemo.dialog.SettingAlertDialog
import com.zapry.pkdemo.event.FlowEvent
import com.zapry.pkdemo.event.FlowEventManager
import com.zapry.pkdemo.model.MultiWalletInfo
import com.zapry.pkdemo.model.RNWalletInfo
import com.zapry.pkdemo.util.GsonUtils
import kotlinx.coroutines.launch

/**
 * Module
 *
 * RN-Android两端都定义同名的module, NativeModule.getName()
 */
abstract class AbsReactNativeModule(context: ReactApplicationContext) :
    ReactContextBaseJavaModule(context) {
    companion object {
        private const val ACTION_CLOSE = "close"

        /**
         * Wallet
         */
        // 设置钱包信息
        private const val ACTION_SET_MULTI_WALLET_INFO = "setMultiWalletInfo"
        // 获取钱包-通过chainCode
        private const val ACTION_GET_WALLET_BY_CHAIN_CODE = "getWalletByChainCode"
        //设置支付授权
        private const val ACTION_SET_PAY_AUTH = "setPayAuth"
        //请求支付授权
        private const val ACTION_REQUEST_PQY_AUTH = "requestPayAuth"
        private const val ACTION_SAVE_IMAGE = "saveImage"

        // 关闭加载框
        private const val ACTION_REMOVE_LOADING = "removeLoadingView"
    }

    private val tag = name


    /****************  RN -> Android callback start *****************/

    open fun action(action: String, params: String?, promise: Promise) {
        Log.d(
            tag,
            "action() called with: action $action, params = $params, promise = $promise"
        )
        val a = currentActivity ?: return
        when (action) {
            ACTION_CLOSE -> close()
            ACTION_REMOVE_LOADING -> removeLoadingView(promise)
            ACTION_SET_MULTI_WALLET_INFO -> setMultiWalletInfo(params, promise)
            ACTION_GET_WALLET_BY_CHAIN_CODE -> getWalletByChainCode(params, promise)
            ACTION_SET_PAY_AUTH -> setPayAuth(params, promise)
            ACTION_REQUEST_PQY_AUTH -> requestPayAuth(params, promise)
        }
    }

    private fun close() {
        Log.i(tag, "close() called")
        currentActivity?.finish()
    }

    private fun saveImage(params: String?, promise: Promise) {
        Log.i(tag, "saveImage() called with: path = $params")
    }

    /**
     * 关闭加载框
     */
    private fun removeLoadingView(promise: Promise) {
        Log.i(tag, "removeLoadingView() called with: promise = $promise")
        if (currentActivity is RNActionInterface) {
            val rnActionInterface = currentActivity as RNActionInterface
            (currentActivity as Activity).runOnUiThread {
                rnActionInterface.dismissLoading()
            }
        }
        promise.resolve(null)
    }

    private fun getWalletByChainCode(params: String?, promise: Promise) {
        Log.d(tag, "getWalletByChainCode() called with: params = $params, promise = $promise")

        GsonUtils.fromJson<Map<String, String>>(
            params,
            object : TypeToken<Map<String, String>>() {}.type
        )?.get("chainCode")?.apply {
            if (currentActivity is RNActionInterface) {
                val rnActionInterface = currentActivity as RNActionInterface

                val result = loadWalletMap(rnActionInterface, this)
                promise.resolve(result)
            }
        }
    }

    private fun loadWalletMap(
        rnActionInterface: RNActionInterface,
        chainCode: String
    ): WritableMap {
        return Arguments.createMap().apply {
            Log.d(tag, "loadWalletMap() returned: $this")
        }
    }

    private fun setMultiWalletInfo(params: String?, promise: Promise) {
        Log.d(tag, "setMultiWalletInfo() called with: params = $params, promise = $promise")
        val walletInfo = GsonUtils.fromJson(params, MultiWalletInfo::class.java)
        if (walletInfo == null) {
            promise.reject(Exception("Invalid wallet info"))
        } else if (currentActivity is RNActionInterface) {
            val rnActionInterface = currentActivity as RNActionInterface
            (currentActivity as Activity).runOnUiThread {
                rnActionInterface.setMultiWalletInfo(walletInfo)
            }
            promise.resolve(null)
        } else {
            promise.reject(Exception("Invalid activity"))
        }
    }

    private fun requestPayAuth(params: String?, promise: Promise) {
        Log.d(tag, "requestPayAuth() called with: params = $params, promise = $promise")
        if (currentActivity !is FragmentActivity) {
            promise.reject(Throwable("currentActivity not FragmentActivity"))
            return
        }

        val activity = currentActivity as FragmentActivity
        GsonUtils.fromJson(params, PayAuth::class.java)?.apply {
            val callback = object : BiometricCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    val error = errString.toString()
                    promise.reject(errorCode.toString(), error, Throwable(error))
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    responsePayAuth(this@apply, promise)
                }

                override fun onPasswordSucceeded() {
                    responsePayAuth(this@apply, promise)
                }

                override fun onPasswordFailed() {
                    super.onPasswordFailed()
                    promise.reject(
                        ERROR_CODE_PASSWORD_FAILED,
                        INCORRECT_PASSWORD,
                        Throwable(INCORRECT_PASSWORD)
                    )
                }

                override fun onAuthenticationFailed() {
                    val message = currentActivity?.getString(R.string.verification_failed_tip)
                        ?: "authentication failed"
                    promise.reject(ERROR_CODE_BIOMETRIC_FAILED, message, Throwable(message))
                }
            }

            when (payType) {
                PayAuth.UNBIND, PayAuth.MNEMONIC, PayAuth.NEW_WALLET, PayAuth.SWITCH_AUTH_VERIFY, PayAuth.ADD_CHAIN -> {
                    justBiometric(activity, this, callback)
                }

                PayAuth.TRANSFER, PayAuth.CONTRACT_TRADING, PayAuth.SIGN, PayAuth.SIGN_EIP712 -> {
                    activity.runOnUiThread {
                        try {
                            AuthDialog.createBuilder(activity, this, callback)
                                .show(activity.supportFragmentManager)
                        } catch (e: Exception) {
                            Log.e(tag, e.stackTraceToString())
                            promise.reject(Throwable(e.message))
                        }
                    }
                }

                else -> {}
            }
        }
    }

    /**
     * 仅弹生物识别
     */
    private fun justBiometric(
        activity: FragmentActivity,
        payAuth: PayAuth,
        callback: BiometricCallback
    ) {
        when (BiometricHelper.getVerifyType()) {
            BiometricHelper.VerifyType.NOT_SETTING -> {
                activity.runOnUiThread {
                    SettingAlertDialog(activity).show()
                }
            }

            BiometricHelper.VerifyType.PASSWORD -> {
                val payPwd = TEST_USER_ID?.let { PrivateKeyMgr.getPayPassword(it) }
                if (payPwd == payAuth.data?.payPassword) {
                    callback.onPasswordSucceeded()
                } else {
                    callback.onPasswordFailed()
                }
            }

            BiometricHelper.VerifyType.BIOMETRICS -> {
                BiometricHelper.promptBiometric(activity, callback)
            }
        }
    }

    private fun responsePayAuth(auth: PayAuth, promise: Promise) {
        Log.i(tag, "responsePayAuth() called with: auth = $auth, promise = $promise")
        currentActivity?.setResult(Activity.RESULT_OK)
        when (auth.payType) {
            PayAuth.TRANSFER, PayAuth.CONTRACT_TRADING, PayAuth.SIGN, PayAuth.SIGN_EIP712, PayAuth.ADD_CHAIN -> {
                if (currentActivity is RNActionInterface) {
                    loadWalletMap(
                        currentActivity as RNActionInterface,
                        auth.data?.chainCode ?: "0"
                    ).apply {
                        promise.resolve(this)
                    }

                    if (auth.payType == PayAuth.CONTRACT_TRADING) {
                        IoScope.launch {
                            FlowEventManager.instance.fireEvent(FlowEvent.ShowLoadingEvent)
                        }
                    }
                }
            }

            PayAuth.MNEMONIC -> {
                if (currentActivity is RNActionInterface) {
                    Arguments.createMap().apply {
                        val mnemonic = PrivateKeyMgr.getMnemonic(TEST_USER_ID)
                        putString("mnemonic", mnemonic)
                        promise.resolve(this)
                    }
                }
            }

            else -> {
                promise.resolve(null)
            }
        }
    }

    private fun setPayAuth(params: String?, promise: Promise) {
        Log.d(tag, "setPayAuth() called with: params = $params, promise = $promise")
        GsonUtils.fromJson<Map<String, String>>(
            params,
            object : TypeToken<Map<String, String>>() {}.type
        )?.run {
            val type = get("type")?.toIntOrNull() ?: -1
            when (BiometricHelper.VerifyType.of(type)) {
                BiometricHelper.VerifyType.BIOMETRICS -> {
                    if (currentActivity is FragmentActivity) {
                        BiometricHelper.promptBiometric(
                            currentActivity as FragmentActivity,
                            object : BiometricCallback() {
                                override fun onAuthenticationError(
                                    errorCode: Int,
                                    errString: CharSequence
                                ) {
                                    val error = errString.toString()
                                    promise.reject(errorCode.toString(), error, Throwable(error))
                                }

                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    super.onAuthenticationSucceeded(result)
                                    BiometricHelper.setVerifyType(BiometricHelper.VerifyType.BIOMETRICS)
                                    promise.resolve(true)
                                }

                                override fun onPasswordFailed() {
                                    super.onPasswordFailed()
                                    promise.reject(
                                        ERROR_CODE_PASSWORD_FAILED,
                                        INCORRECT_PASSWORD,
                                        Throwable(INCORRECT_PASSWORD)
                                    )
                                }

                                override fun onPasswordSucceeded() {
                                    BiometricHelper.setVerifyType(BiometricHelper.VerifyType.BIOMETRICS)
                                    promise.resolve(true)
                                }

                                override fun onAuthenticationFailed() {
                                    val message =
                                        currentActivity?.getString(R.string.verification_failed_tip)
                                            ?: "authentication failed"
                                    promise.reject(
                                        ERROR_CODE_BIOMETRIC_FAILED,
                                        message,
                                        Throwable(message)
                                    )
                                }
                            })
                    }
                }

                BiometricHelper.VerifyType.PASSWORD -> {
                    BiometricHelper.setVerifyType(BiometricHelper.VerifyType.PASSWORD)
                    get("payPassword")?.apply {
                        TEST_USER_ID?.let { PrivateKeyMgr.savePayPassword(it, this) }
                    }
                    promise.resolve(true)
                }

                else -> {
                    promise.reject(Throwable("unknown pay type $this"))
                }
            }
        }
    }
}