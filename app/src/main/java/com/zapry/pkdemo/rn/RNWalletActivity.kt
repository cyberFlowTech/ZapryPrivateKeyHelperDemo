package com.zapry.pkdemo.rn

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import com.cyberflow.mimolite.lib_private_key.biometric.BiometricHelper
import com.cyberflow.mimolite.lib_private_key.PrivateKeyMgr
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.bridge.Promise
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.zapry.pkdemo.SP_DEMO
import com.zapry.pkdemo.SP_KEY_CHAIN_CODES
import com.zapry.pkdemo.TEST_USER_ID
import com.zapry.pkdemo.app.IWindowActionApi
import com.zapry.pkdemo.app.WindowActionDelegateImpl
import com.zapry.pkdemo.constant.LoginConstant
import com.zapry.pkdemo.databinding.ActivityRnWalletBinding
import com.zapry.pkdemo.ext.versionCode
import com.zapry.pkdemo.model.MultiWalletInfo


open class RNWalletActivity() : ReactActivity(), IWindowActionApi, RNActionInterface {

    private var isLoading: Boolean = true

    private var canBack = true

    private val windowActionDelegateImpl by lazy { WindowActionDelegateImpl.create(this) }

    private val sp
        get() = this.getSharedPreferences(
            SP_DEMO,
            Context.MODE_PRIVATE
        )

    private lateinit var loadingView: ActivityRnWalletBinding
    private lateinit var animatorSet: AnimatorSet

    /**
     * Returns the name of the main component registered from JavaScript. This is used to schedule
     * rendering of the component.
     */
    override fun getMainComponentName(): String {
        return "MimoWalletRnapp"
    }

    /**
     * Returns the instance of the [ReactActivityDelegate]. Here we use a util class [ ] which allows you to easily enable Fabric and Concurrent React
     * (aka React 18) with two boolean flags.
     */
    override fun createReactActivityDelegate(): ReactActivityDelegate {
        return WalletReactActivityDelegate(
            this,
            mainComponentName,  // If you opted-in for the New Architecture, we enable the Fabric Renderer.
            fabricEnabled
        ) {
            createOptions()
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ReactNativeFlipper.initializeFlipper(
            applicationContext,
            reactNativeHost.reactInstanceManager
        )

        // Caused by: java.lang.IllegalStateException: Screen fragments should never be restored.
        // Follow instructions from https://github.com/software-mansion/react-native-screens/issues/17#issuecomment-424704067
        // to properly configure your main activity.
        super.onCreate(null)
        Log.i(TAG, "onCreate() called")

        windowActionDelegateImpl.onCreate(savedInstanceState)
        showLoading()
    }

    override fun onPause() {
        // RN不支持多实例，钱包和DApp一起用分屏容易闪退，但没啥好办法，catch住似乎不影响逻辑
        try {
            super.onPause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        windowActionDelegateImpl.onPause()
    }

    override fun onStop() {
        super.onStop()
        dismissLoading()
    }

    override fun onDestroy() {
        super.onDestroy()
        ReactNativeFlipper.stop()
        reactNativeHost.reactInstanceManager.onHostDestroy(this)
        reactNativeHost.reactInstanceManager.destroy()
        windowActionDelegateImpl.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        Log.i(TAG, "onBackPressed() called")
        if (canBack) {
            super.onBackPressed()
        }
    }

    override fun runOnWindowActive(run: () -> Unit) {
        windowActionDelegateImpl.runOnWindowActive(run)
    }

    val ENV_PROD = "production"
    val ENV_DEV = "development"

    open fun createOptions(bundle: Bundle? = null): Bundle {
        Log.d(TAG, "createOptions: bundle = $bundle")
        val extras = Bundle(bundle ?: intent?.extras ?: Bundle())

        extras.putString("env", ENV_DEV)
        extras.putString("userId", TEST_USER_ID)
        extras.putString(
            "avatar",
            "https://infras-test.s3.amazonaws.com/icon/847711-1719208889668.jpg?size=small&type=avatar"
        )
        extras.putString("nick", "tester")
        extras.putString(LoginConstant.LANGUAGE, "en")
        extras.putString(
            LoginConstant.VERSION_CODE,
            applicationContext.versionCode().toString()

        )
        extras.putString(LoginConstant.API, LoginConstant.api)
        extras.putString(LoginConstant.UUID, "")
        extras.putString(LoginConstant.VERSION, "")
        extras.putString(
            LoginConstant.USER_ID,
            TEST_USER_ID
        )
        extras.putString("baseUrl", "")
        extras.putString("securityKey", LoginConstant.security_key)
        extras.putBoolean("newcomer", true)

        extras.putInt("authType", BiometricHelper.getVerifyType().value)
        return extras.apply {
            Log.d(TAG, "createOptions() returned: $this")
        }
    }

    private fun showLoading() {
        val testShowLoading = false
        if (testShowLoading) {
            loadingView = ActivityRnWalletBinding.inflate(LayoutInflater.from(this))
            setContentView(loadingView.root)
        } else {
            val content = (findViewById<View>(android.R.id.content) as? ViewGroup) ?: return
            loadingView = ActivityRnWalletBinding.inflate(LayoutInflater.from(this), content, true)
        }

        AnimatorSet().also {
            val duration = 1000L
            it.duration = duration
            it.playTogether(
                ObjectAnimator.ofFloat(
                    loadingView.clCenter,
                    View.TRANSLATION_Y,
                    0F,
                    -15f
                ),
                ObjectAnimator.ofFloat(loadingView.clCenter, View.ALPHA, 0F, 1F)
            )
            animatorSet = it
        }.start()
    }

    @UiThread
    override fun dismissLoading() {
        isLoading = false
        if (::animatorSet.isInitialized) {
            animatorSet.cancel()
        }
        if (::loadingView.isInitialized) {
            val root = loadingView.root
            (root.parent as? ViewGroup)?.removeView(root)
        }
    }


    override fun setMultiWalletInfo(walletInfo: MultiWalletInfo) {
        val chainCodes = mutableSetOf<String>()
        walletInfo.wallet.forEach { info ->
            PrivateKeyMgr.savePrivateKeyByChain(TEST_USER_ID, info.key, info.value.privateKey)
            chainCodes.add(info.key)
        }
        PrivateKeyMgr.saveMnemonic(TEST_USER_ID, walletInfo.mnemonic)
        sp.edit().putStringSet(SP_KEY_CHAIN_CODES, chainCodes).apply()
    }

    override fun otherAction(action: String, params: String?, promise: Promise) {
        Log.d(TAG, "otherAction: action = $action, params = $params")
        when (action) {
            "goBackGestureEnabled" -> {
                promise.resolve(null)
            }

            else -> super.otherAction(action, params, promise)
        }
    }

    companion object {
        private const val TAG = "RNWalletActivity"
    }
}