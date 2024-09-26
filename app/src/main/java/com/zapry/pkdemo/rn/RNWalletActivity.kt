package com.zapry.pkdemo.rn

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cyberflow.mimolite.lib_private_key.PrivateKeyLib
import com.cyberflow.mimolite.lib_private_key.biometric.BiometricHelper
import com.cyberflow.mimolite.lib_private_key.PrivateKeyMgr
import com.cyberflow.mimolite.lib_private_key.SP_WALLET
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
import com.zapry.pkdemo.model.RNWalletInfo
import com.zapry.pkdemo.util.SystemUIUtil
import kotlinx.coroutines.launch


open class RNWalletActivity() : ReactActivity(), IWindowActionApi, RNActionInterface {

    private var isLoading: Boolean = true

    private var canBack = true

    private val windowActionDelegateImpl by lazy { WindowActionDelegateImpl.create(this) }

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
//        super.attachBaseContext(RNBaseContext(newBase))
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
//        val walletUnbindFlow = CmdManager.instance.cmdFlow
//            .filter {
//                it.javaClass.name == CmdWalletUnbind::class.java.name
//            }
//            .shareIn(lifecycleScope, SharingStarted.Lazily)

        // 解绑钱包只在Activity活动时接收
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                walletUnbindFlow.collect {
//                    Log.i(TAG, "receive unbind wallet")
//                    Web3Manager.getInstance().removeWallet()
//
//                    MimoReactNativeModule.getInstance()?.emitEventToJsModule(JSEventName.WalletJostle)
//
//                    CommonConfirmDialog.Builder()
//                        .setTitle(R.string.common_tip)
//                        .setSubTitle(R.string.wallet_unbind_rn_tips_content)
//                        .setCancel(null as String?)
//                        .setConfirm(R.string.wallet_confirm_add)
//                        .build()
//                        .show(supportFragmentManager, "wallet_unbind")
//                }
            }
        }
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
        Log.d(TAG, "onDestroy: ====")
//        MimoReactNativeModule.getInstance()?.emitEventToJsModule(JSEventName.WalletExit)
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

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        reactInstanceManager.recreateReactContextInBackground()
//    }

    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == PermissionUtils.REQUEST_SCAN_QR_CODE) {
//            if (resultCode == Activity.RESULT_OK) {
//                val content = PermissionUtils.getCodedContent(data)
//                Log.d(TAG, "onActivityResult: content = $content")
//
//                val isAddress = AppLinkStrategy.ReactNativeAddress.matches(content)
//
//                if (isAddress) {
//                    scanPromise?.resolve(content)
//                } else {
//                    AppLinkManager.instance.handle(this, content)
//                }
//            } else {
//                Log.d(TAG, "onActivityResult: cancel")
//                scanPromise?.reject(Exception("Cancel"))
//            }
//            scanPromise = null
//        }
//    }

    override fun runOnWindowActive(run: () -> Unit) {
        windowActionDelegateImpl.runOnWindowActive(run)
    }

    val ENV_PROD = "production"
    val ENV_DEV = "development"

    open fun createOptions(bundle: Bundle? = null): Bundle {
        Log.d(TAG, "createOptions: bundle = $bundle")
        val extras = Bundle(bundle ?: intent?.extras ?: Bundle())
//        val env = if (isProductionStage()) {
//            RedEnvelopeApiRepository.ENV_PROD
//        } else {
//            RedEnvelopeApiRepository.ENV_DEV
//        }
        extras.putString("env", ENV_DEV)

//        if (extras.getString("screen") == "Transfer") {
//            if (!extras.getBoolean("isScan", false)) {
//                // 非扫码转账需要传递 chainCode
//                extras.putString("chainCode", ChainService.DEFAULT_CHAIN_CODE)
//            } else {
//                extras.remove("isScan")
//            }
//        }

//        val address = viewModel.getWallet()?.address
//        if (!address.isNullOrEmpty()) {
//            extras.putString("address", address)
//        }

//        PersonalUserInfoManager.instance.userInfoFlow.value.also { userInfo ->
        extras.putString("userId", TEST_USER_ID)
        extras.putString(
            "avatar",
            "https://infras-test.s3.amazonaws.com/icon/847711-1719208889668.jpg?size=small&type=avatar"
        )
        extras.putString("nick", "tester")
//        }

        extras.putString(LoginConstant.LANGUAGE, "en")
//        extras.putStringArray(
//            "supportLangs",
//            LanguageUtil.getSupportLanguages().toTypedArray()
//        )

        extras.putString(
            LoginConstant.VERSION_CODE,
            applicationContext.versionCode().toString()

        )

//        val codes = viewModel.getChainCodes()
//        Log.d(TAG, "createOptions: ids = $codes, size = ${codes.size}")
//
//        if (codes.isNotEmpty()) {
//            Bundle().apply {
//                codes.forEach {
//                    putString(it.key, it.value)
//                }
//                extras.putBundle("multiAddress", this)
//            }
//        }

        extras.putString(LoginConstant.API, LoginConstant.api)
//        extras.putString(
//            LoginConstant.SESSID,
//            PersonalUserInfoManager.instance.userInfoFlow.value.sessionId
//        )
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
//            runCachingWithLog {
            animatorSet.cancel()
//            }
        }
        if (::loadingView.isInitialized) {
            val root = loadingView.root
            (root.parent as? ViewGroup)?.removeView(root)
        }
    }


    override fun setWalletInfo(walletInfo: RNWalletInfo) {
        PrivateKeyMgr.saveMnemonic(TEST_USER_ID, walletInfo.mnemonic)
//        viewModel.saveWallet(walletInfo)
        setResult(RESULT_OK)
    }

    override fun getWalletInfo(promise: Promise) {
//        val info = viewModel.getWallet()
//        if (info == null) {
//            promise.reject(Exception("Wallet info is empty"))
//        } else {
//            val map = Arguments.createMap().apply {
//                putString("address", info.address)
//                putString("mnemonic", info.mnemonic)
//                putString("privateKey", info.privateKey)
//            }
//            promise.resolve(map)
//        }
    }

    override fun removePrivateKey(promise: Promise) {
//        viewModel.removePrivateKey()
//        promise.resolve(null)
    }

    private var scanPromise: Promise? = null

    //    @UiThread
//    override fun scan(promise: Promise) {
//        scanPromise = promise
//        PermissionUtils.checkCameraPermission(this)
//    }

    private val sp
        get() = this.getSharedPreferences(
            SP_DEMO,
            Context.MODE_PRIVATE
        )

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

    /**
     * 是否启用返回
     */
//    private fun setBackGestureEnable(params: String?) {
//        Log.i(TAG, "setBackGestureEnable() called with: params = $params")
//        GsonUtils.fromJson<Map<String, String>>(
//            params,
//            object : TypeToken<Map<String, String>>() {}.type
//        )?.apply {
//            canBack = try {
//                // true 可以返回，false 不可以返回
//                get("enabled").toBoolean()
//            } catch (e: Exception) {
//                e.printStackTrace()
//                true // 异常可以返回
//            }.apply {
//                Log.i(TAG, "setBackGestureEnable() called canBack is $this")
//            }
//        }
//    }

//    override fun getWalletByChainCode(chainCode: String) = viewModel.getWalletByChainCode(chainCode)

    override fun getWalletByChainCode(chainCode: String): Map<String, String> {
        TODO("Not yet implemented")
    }

    companion object {
        private const val TAG = "RNWalletActivity"
    }
}