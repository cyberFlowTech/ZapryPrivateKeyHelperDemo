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
import com.zapry.pkdemo.event.JSEventName
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
        private const val ACTION_SET_WALLET_INFO = "setWalletInfo"
        private const val ACTION_SET_MULTI_WALLET_INFO = "setMultiWalletInfo"

        // 获取钱包信息
        private const val ACTION_GET_WALLET_INFO = "getWalletInfo"

        // 获取钱包-通过chainCode
        private const val ACTION_GET_WALLET_BY_CHAIN_CODE = "getWalletByChainCode"

        // 移除私钥
        private const val ACTION_REMOVE_PRIVATE_KEY = "removePrivateKey"

        //设置支付授权
        private const val ACTION_SET_PAY_AUTH = "setPayAuth"

        //请求支付授权
        private const val ACTION_REQUEST_PQY_AUTH = "requestPayAuth"

        // 用户钱包token变化通知，删除/添加token
        private const val ACTION_TOKEN_SORT_DONE = "tokenSortDone"


        // 打开DApp
        private const val ACTION_TO_D_APP_BROWSER = "toDappBrowser"

        // 扫码
        private const val ACTION_SCAN = "scan"
        private const val ACTION_SAVE_IMAGE = "saveImage"

        // 关闭加载框
        private const val ACTION_REMOVE_LOADING = "removeLoadingView"
        private const val ACTION_RECT_CONTACTS = "rectContacts"
        private const val ACTION_PUT_LOGS = "putLogs"
        private const val ACTION_FEEDBACK_GENERATOR = "FeedbackGenerator"

        // resolvedId
        const val GET_TOKENS_BALANCE = "get_tokens_balance"
        const val APPROVE_FLASH_EXCHANGE = "approve_flash_exchange"
        const val GET_GAS_FLASH_EXCHANGE_APPROVE = "get_gas_flash_exchange_approve"
        const val ID_SIGN_EIP712 = "id_sign_eip712"
        const val SEND_TX = "send_tx"
        const val GET_SEND_TX_INFO = "get_from_tx_info"
        const val GET_RECEIVE_TX_INFO = "get_to_tx_info"

        // FlashExchange / Intent Pay
        const val ACTION_GO_INTENT_PAY_PAGE = "goIntent" //DApp拉起意图支付
        const val ACTION_GO_FILL_UP_PAGE = "goFillUpCode" //DApp->RN->Android->拉起RN FillUp充值页

        /**
         * handler
         */
        private const val WHAT_GET_BALANCE_AGAIN = 0
        private const val SECONDS_60 = 60 * 1000L
    }

    private val tag = name

//    private val chainsMMKV by lazy {
//        MMKV.mmkvWithID(MMAP_ID_WALLET_CHAINS, Context.MODE_PRIVATE)
//    }



    /****************  Android -> RN *****************/
    fun emitEventToJsModule(event: JSEventName, args: ReadableMap? = null) {
        val name = event.name
        Log.i(tag, "emitEventToJsModule(), event=$name args=$args")
        reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(name, args)
    }


    /****************  RN -> Android callback start *****************/

    open fun action(action: String, params: String?, promise: Promise) {
        Log.d(
            tag,
            "action() called with: action $action, params = $params, promise = $promise"
        )
        val a = currentActivity ?: return
        when (action) {
//            ACTION_FEEDBACK_GENERATOR -> a.startVibrate(200L)
            ACTION_CLOSE -> close()
            ACTION_SCAN -> scan(promise)
            ACTION_SAVE_IMAGE -> saveImage(params, promise)
//            ACTION_SET_WALLET_INFO -> setWalletInfo(params, promise)
//            ACTION_GET_WALLET_INFO -> getWalletInfo(promise)
//            ACTION_REMOVE_PRIVATE_KEY -> removePrivateKey(params, promise)
//            ACTION_RECT_CONTACTS -> rectContacts(promise)
            ACTION_REMOVE_LOADING -> removeLoadingView(promise)
//            ACTION_TO_D_APP_BROWSER -> toDappBrowser(params, promise)
            ACTION_SET_MULTI_WALLET_INFO -> setMultiWalletInfo(params, promise)
            ACTION_GET_WALLET_BY_CHAIN_CODE -> getWalletByChainCode(params, promise)
            ACTION_SET_PAY_AUTH -> setPayAuth(params, promise)
            ACTION_REQUEST_PQY_AUTH -> requestPayAuth(params, promise)
//            ACTION_TOKEN_SORT_DONE -> onWalletTokenListChanged(params, promise)
//            ACTION_PUT_LOGS -> putLogs(a, params)
//            ACTION_GO_INTENT_PAY_PAGE -> launchFlashExchange(params)
//            ACTION_GO_FILL_UP_PAGE -> goRNFillUpPage(params)

//            else -> {
//                val h = AbsReactNativeModuleHandler.getHandler(action)
//                if (h != null) {
//                    h.handle(params, promise, a)
//                } else {
//                    if (a is RNActionInterface) {
//                        (a as RNActionInterface).otherAction(action, params, promise)
//                    }
//                }
//                return
//            }
        }
    }

    /**
     * Android侧 module接收RN回调：
     * [MimoRNWeb3Module] 使用 [resolveTask]接收RN回调
     *
     * 其他module使用action()接收RN回调:
     * [MimoReactNativeModule]
     * WalletSdkRNModule
     * MimoWalletRnSDK
     */
    open fun resolveTask(resolvedId: String?, result: String?) {
        Log.d(tag, "resolveTask: $resolvedId $result")

        if (resolvedId.isNullOrEmpty() || result.isNullOrEmpty()) {
            Log.e(tag, "resolveTask nullOrEmpty: $resolvedId $result")
            return
        }

        try {
            when (resolvedId) {
//                GET_TOKENS_BALANCE -> onReceiveBalance(result)
                GET_GAS_FLASH_EXCHANGE_APPROVE -> onReceiveGas(result)
                APPROVE_FLASH_EXCHANGE -> onApproveTxSubmitted(result)
                ID_SIGN_EIP712 -> onEip712Signed(result)
                SEND_TX -> onTxSent(result)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 接收RN异常的回调
     */
    open fun rejectTask(resolvedId: String?, error: String?) {
        Log.d(tag, "rejectTask: $resolvedId $error ")

        if (resolvedId.isNullOrEmpty() || error.isNullOrEmpty()) {
            Log.e(tag, "rejectTask nullOrEmpty: $resolvedId $error")
            return
        }

        when (resolvedId) {
            GET_TOKENS_BALANCE -> onReceiveBalanceFailed(error)
            APPROVE_FLASH_EXCHANGE -> onApproveTxSubmitFailed(error)
            GET_GAS_FLASH_EXCHANGE_APPROVE -> onReceiveGasFailed(error)

        }
    }

    /****************  RN -> Android callback end *****************/


    /****************  业务处理 start *****************/

    /**
     * 跳转到Dapp浏览页
     */
//    private fun toDappBrowser(params: String?, promise: Promise) {
//        Log.i(tag, "toDappBrowser() called with: params = $params, promise = $promise")
//
//        GsonUtils.fromJson<Map<String, String>>(
//            params,
//            object : TypeToken<Map<String, String>>() {}.type
//        )?.run {
//            val bundle = Bundle()
//            for ((key, value) in this) {
//                bundle.putString(key, value)
//            }
//            bundle
//        }?.apply {
//            val dApp = DApp(
//                dappUrl = getString("url", ""),
//                dappLogo = getString("iconUrl", ""),
//                dappName = getString("dappName", "")
//            )
//            val tribeId = getString("tribeId", "")
//            (currentActivity as? FragmentActivity)?.let { a ->
//                ARouter.getInstance().navigation(TribeComponentService::class.java)
//                    .launchDAppBrowser(
//                        a,
//                        app = dApp,
//                        tribeId = tribeId,
//                        ci = null
//                    )
//            }
//
//            //因为这里子豪的params没返回dapp_id,所以我就在这里也上报一次，如果哪天子豪这里加了dapp_id，这里就不需要上报了。因为在WebH5ViewController里已经上报了
////            PointManager.shared.checkReportEnterDApp() dqerror
//        }
//    }
//
//    private fun rectContacts(promise: Promise) {
//        Log.d(tag, "recContacts() called with: promise = $promise")
//        val list = ARouter.getInstance()
//            .navigation(ConversationService::class.java)
//            .getLastContacts()?.map { bundle ->
//                RNContact(
//                    ouid = bundle.getString("ouid"),
//                    avatar = bundle.getString("avatar"),
//                    nick = bundle.getString("nick")
//                )
//            }
//        val toJson = GsonUtils.toJson(list)
//        Log.i(tag, "rectContacts: load contacts is $toJson")
//        if (list.isNullOrEmpty()) {
//            promise.reject(Exception("no contacts"))
//        } else {
//            promise.resolve(toJson)
//        }
//    }
//
//    private fun removePrivateKey(params: String?, promise: Promise) {
//        Log.i(tag, "removePrivateKey() called with: promise = $params")
//        params?.let {
//            val removePrivateKeyParam = GsonUtils.fromJson(
//                params,
//                RNRemovePrivateKeyParam::class.java
//            )
//            if (removePrivateKeyParam?.remove == 0) {
//                //移除privateKey，需要更新我的nft头像
//                val myAvatarUrl =
//                    PersonalUserInfoManager.instance.userInfoFlow.value.avatar.orEmpty()
//                if (checkContainsNFTID(myAvatarUrl)) {
//                    val normalAvatarUrl = getStringBeforeQuestionMark(myAvatarUrl)
//                    SafeGlobalScope.launch {
//                        val r = UserInfoRepository().modifyUserAvatar(normalAvatarUrl)
//                        Log.d("set avatar", "解绑钱包 还原普通头像 == $r")
//                        if (r.isSuccess())
//                            PersonalUserInfoManager.instance.setUserInfo(
//                                personalUserInfo = PersonalUserInfo(
//                                    avatar = normalAvatarUrl
//                                )
//                            )
//                    }
//                }
//            }
//        }
//
//        if (currentActivity is RNActionInterface) {
//            val rNActionInterface = currentActivity as RNActionInterface
//            rNActionInterface.removePrivateKey(promise)
//        } else {
//            promise.reject(Exception("Invalid activity"))
//        }
//    }

    private fun getWalletInfo(promise: Promise) {
        Log.i(tag, "getWalletInfo() called with: promise = $promise")
        if (currentActivity is RNActionInterface) {
            val rnActionInterface = currentActivity as RNActionInterface
            rnActionInterface.getWalletInfo(promise)
        } else {
            promise.reject(Exception("Invalid activity"))
        }
    }

    private fun setWalletInfo(params: String?, promise: Promise) {
        Log.d(tag, "setWalletInfo() called with: params = $params, promise = $promise")
        val walletInfo = GsonUtils.fromJson(params, RNWalletInfo::class.java)
        if (walletInfo == null) {
            promise.reject(Exception("Invalid wallet info"))
            return
        } else {
            if (currentActivity is RNActionInterface) {
                val rnActionInterface = currentActivity as RNActionInterface
                (currentActivity as Activity).runOnUiThread {
                    rnActionInterface.setWalletInfo(walletInfo)
                    promise.resolve(null)
                }
            } else {
                promise.reject(Exception("Invalid activity"))
            }
        }
    }

    private fun close() {
        Log.i(tag, "close() called")
        currentActivity?.finish()
    }

    private fun scan(promise: Promise) {
        Log.i(tag, "scan() called")
        currentActivity?.runOnUiThread {
            if (currentActivity is RNActionInterface) {
                (currentActivity as RNActionInterface).scan(promise)
            }
        }
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

            if (chainCode == "-1") {
                // chainCode为-1时返回所有的钱包地址
//                val chainService = ARouter.getInstance().navigation(ChainService::class.java)
//                putString("mnemonic", chainService.getMnemonic())
//                putMap("wallet", Arguments.createMap().also { codesMap ->
//                    chainService.getChainCodes().forEach { code ->
//                        codesMap.putMap(code, Arguments.createMap().also { walletMap ->
//                            walletMap.putString("address", chainService.getAddressByChain(code))
//                            walletMap.putString(
//                                "privateKey",
//                                chainService.getPrivateKeyByChain(code)
//                            )
//                        })
//                    }
//                })

            } else {
                val wallet = rnActionInterface.getWalletByChainCode(chainCode)
                putString("mnemonic", wallet["mnemonic"])
                putMap("wallet", Arguments.createMap().apply {
                    putString("address", wallet["address"])
                    putString("privateKey", wallet["privateKey"])
                })
            }
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
     * 用户钱包token变化通知，删除/添加token
     */
//    private fun onWalletTokenListChanged(params: String?, promise: Promise) {
//        IoScope.launch {
//            chainsMMKV.clearAll()
//            ChainManager.instance.suspendGetChains(true)
//        }
//    }

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

//    private fun putLogs(a: Activity, params: String?) {
//        if (a is RNActionInterface) {
//            val log = GsonUtils.fromJson<Map<String, String>>(
//                params,
//                object : TypeToken<Map<String, String>>() {}.type
//            )?.get("log")
//            (a as RNActionInterface).putLogs(log)
//        }
//    }

//    private fun launchFlashExchange(params: String?) {
//        params?.let {
//            ARouter.getInstance().navigation(WalletComponentService::class.java)
//                .launchFlashExchange(params)
//        }
//    }

//    private fun goRNFillUpPage(params: String?) {
//        GsonUtils.fromJson(params, RNFillUpParams::class.java)?.apply {
//            RNPageLauncher.launch(
//                app(),
//                chain?.let { c ->
//                    token?.let { t ->
//                        RNPageArgs.FillUpPage(c, t)
//                    }
//                }
//            )
//        }
//    }

    /**
     * 每条链查询一次，每条链的余额回调一次
     */
    private var onceTime: Boolean = true

//    private fun onReceiveBalance(json: String) {
//        val balances = GsonUtils.fromJson<Map<String, Balance>>(
//            json,
//            object : TypeToken<Map<String, Balance>>() {}.type
//        )
//
//        balances?.forEach {
//            Log.d(tag, "onReceiveBalance: balance $it")
//            saveBalance(it)
//
//            // 只触发一次
//            if (onceTime) {
//                onceTime = false
//                Log.d(tag, "onReceiveBalance: $SECONDS_60 s后查询")
//                handler.sendEmptyMessageDelayed(WHAT_GET_BALANCE_AGAIN, SECONDS_60)
//            }
//        }
//    }

//    private fun saveBalance(it: Map.Entry<String, Balance>) {
//        chainsMMKV.putString(TOKEN_BALANCE_ + it.key, GsonUtils.toJson(it.value))
//
//        // 发送余额变化事件
//        IoScope.launch {
//            FlowEventManager.instance.fireEvent(
//                FlowEvent.ReceivedTokenBalanceEvent(
//                    it.key,
//                    it.value
//                )
//            )
//        }
//    }

    private fun onReceiveBalanceFailed(error: String) {
        Log.e(tag, "onReceiveTokensBalanceFailed: $error")
    }

    private fun onReceiveGas(result: String) {
        Log.d(tag, "onReceiveGas $result")

        IoScope.launch {
            FlowEventManager.instance.fireEvent(FlowEvent.ReceiveGasEvent(true, result))
        }
    }

    private fun onReceiveGasFailed(error: String) {
        Log.d(tag, "onReceiveGasFailed: ")

        IoScope.launch {
            FlowEventManager.instance.fireEvent(FlowEvent.ReceiveGasEvent(false, error))
        }
    }

    /**
     * approve交易提交成功
     */
    private fun onApproveTxSubmitted(hash: String) {
        Log.d(tag, "onSendAmount: ")

        IoScope.launch {
            FlowEventManager.instance.fireEvent(FlowEvent.ApproveTxSubmitResultEvent(true, hash))
        }
    }

    private fun onApproveTxSubmitFailed(error: String) {
        Log.d(tag, error)
        IoScope.launch {
            FlowEventManager.instance.fireEvent(FlowEvent.ApproveTxSubmitResultEvent(false, error))
        }
    }

    private fun onEip712Signed(result: String) {
        Log.d(tag, "onSignEip712: ")
        IoScope.launch {
            FlowEventManager.instance.fireEvent(FlowEvent.SignEip712Event(result))
        }
    }

    private fun onTxSent(result: String) {
        Log.d(tag, "onSendTx: ")

    }
}