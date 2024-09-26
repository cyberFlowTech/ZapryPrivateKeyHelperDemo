package com.zapry.pkdemo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import com.cyberflow.mimolite.lib_private_key.KEY_MNEMONIC
import com.cyberflow.mimolite.lib_private_key.KEY_PRIVATE_KEY
import com.cyberflow.mimolite.lib_private_key.PrivateKeyLib
import com.cyberflow.mimolite.lib_private_key.PrivateKeyMgr
import com.cyberflow.mimolite.lib_private_key.SETTING_KEY_PAY_VERIFY
import com.cyberflow.mimolite.lib_private_key.SP_WALLET
import com.cyberflow.mimolite.lib_private_key.biometric.BiometricCallback
import com.cyberflow.mimolite.lib_private_key.biometric.BiometricHelper
import com.cyberflow.mimolite.lib_private_key.biometric.BiometricHelper.VerifyType
import com.cyberflow.mimolite.lib_private_key.dialog.AuthDialog
import com.cyberflow.mimolite.lib_private_key.model.PayAuth
import com.facebook.react.bridge.Promise
import com.zapry.pkdemo.databinding.ActivityMainBinding
import com.zapry.pkdemo.dialog.SettingAlertDialog
import com.zapry.pkdemo.rn.RNWalletActivity
import com.zapry.pkdemo.util.GsonUtils

const val TEST_USER_ID = "847711"
const val SP_DEMO = "sp_demo"
const val SP_KEY_CHAIN_CODES = "chain_codes"


private const val PAY_PASSWORD_SETTING = "PayPasswordSetting"
private const val OPEN_BIOMETRIC_BOOT = "OpenBiometricBoot"


class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val sp
        get() = this.getSharedPreferences(SP_DEMO, Context.MODE_PRIVATE)

    private val spWallet
        get() = this.getSharedPreferences(SP_WALLET, Context.MODE_PRIVATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.initView()
        binding.initListener()
        initData()
        eventObserver()
    }

    private fun initData() {
        spWallet.edit().putInt(TEST_USER_ID + SETTING_KEY_PAY_VERIFY, VerifyType.BIOMETRICS.value).apply()
    }

    private fun ActivityMainBinding.initView() {

    }

    private fun ActivityMainBinding.initListener() {
        btnCreateWallet.setOnClickListener {
            if (!PrivateKeyMgr.getMnemonic(TEST_USER_ID).isNullOrEmpty()) {
                Toast.makeText(this@MainActivity, "Wallet already exist", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startRNWallet(this@MainActivity, null, -1)
        }

        btnAuth.setOnClickListener {
            if (PrivateKeyMgr.getMnemonic(TEST_USER_ID).isNullOrEmpty()) {
                Toast.makeText(this@MainActivity, "Wallet is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            requestPayAuth("{\"payType\":20,\"data\":{\"chainCode\":\"1000000\",\"token\":{\"chainCode\":5000005,\"chainName\":\"ALG L2\",\"contact\":\"0x46278c527fA92387657Aa946c2dbd1416B6d00c6\",\"exApi\":\"\",\"gasLimit\":1000000,\"gasSymbol\":\"ALG\",\"icon\":\"https://res.mimo.immo/chains_icons/tokens/ciqi.png\",\"id\":50,\"issuer\":\"\",\"name\":\"CIQI\",\"packIcon\":\"\",\"scaleShow\":6,\"sort\":8,\"token\":\"CIQI\",\"unit\":1000000000000000000,\"unitWei\":\"ether\"},\"amount\":\"1\"}}")
        }

        btnDeleteWallet.setOnClickListener {
            if (PrivateKeyMgr.getMnemonic(TEST_USER_ID).isNullOrEmpty()) {
                Toast.makeText(this@MainActivity, "Wallet is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 由lib_private_key sdk存储的
            val sdkList = mutableListOf<String>()
            sdkList.add(TEST_USER_ID + KEY_MNEMONIC)
            sdkList.add(TEST_USER_ID + SETTING_KEY_PAY_VERIFY)
            sp.getStringSet(SP_KEY_CHAIN_CODES, null)?.forEach {
                sdkList.add(TEST_USER_ID + KEY_PRIVATE_KEY + it)
            }
            PrivateKeyMgr.removeWallet(sdkList)
            Toast.makeText(this@MainActivity, "Delete Success", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eventObserver() {
        // TODO: 接收授权dialog弹窗事件 
    }
    
    private fun startRNWallet(
        context: Context,
        bundle: Bundle?,
        requestCode: Int
    ) {
        Intent(context, RNWalletActivity::class.java).apply {
            if (context !is Activity) {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            // 没有助记词说明没有钱包
            val notWallet = PrivateKeyMgr.getMnemonic(TEST_USER_ID).isNullOrEmpty()
            val args = bundle ?: Bundle()
            val screen = args.getString("screen")
            if (notWallet && screen != OPEN_BIOMETRIC_BOOT && screen != PAY_PASSWORD_SETTING) {
                // 没钱包只能跳固定的创建钱包页，需要移除screen参数
                args.remove("screen")
            }

            when (BiometricHelper.getVerifyType()) {
                BiometricHelper.VerifyType.NOT_SETTING -> {
                    // 没有设置验证方式需要先设置验证方式
                    if (notWallet) {
                        if (BiometricHelper.checkBiometric(context)) {
                            BiometricHelper.VerifyType.BIOMETRICS
                        } else {
                            BiometricHelper.VerifyType.PASSWORD
                        }.apply {
                            args.putString("supportAuthType", this.value.toString())
                        }
                    } else {
                        when (val from = args.getString("screen") ?: "wallet") {

                            OPEN_BIOMETRIC_BOOT -> {
                                args.putString(
                                    "supportAuthType",
                                    BiometricHelper.VerifyType.BIOMETRICS.value.toString()
                                )
                                args.putString("from", "setting")
                            }

                            PAY_PASSWORD_SETTING -> {
                                args.putString(
                                    "supportAuthType",
                                    BiometricHelper.VerifyType.PASSWORD.value.toString()
                                )
                                args.putString("from", "setting")
                            }

                            else -> {
                                if (BiometricHelper.checkBiometric(context)) {
                                    BiometricHelper.VerifyType.BIOMETRICS.value.toString()
                                } else {
                                    BiometricHelper.VerifyType.PASSWORD.value.toString()
                                }.also { authType ->
                                    args.putString("supportAuthType", authType)
                                }
                                args.putString("from", from)
                            }
                        }

                        if (BiometricHelper.checkBiometric(context)) {
                            OPEN_BIOMETRIC_BOOT
                        } else {
                            PAY_PASSWORD_SETTING
                        }.apply {
                            args.putString("screen", this)
                        }
                    }
                }

                else -> {
                    when (val from = args.getString("screen") ?: "wallet") {
                        OPEN_BIOMETRIC_BOOT -> {
                            args.putString(
                                "supportAuthType",
                                BiometricHelper.VerifyType.BIOMETRICS.value.toString()
                            )
                        }

                        PAY_PASSWORD_SETTING -> {
                            args.putString(
                                "supportAuthType",
                                BiometricHelper.VerifyType.PASSWORD.value.toString()
                            )
                        }

                        else -> {
                            args.putString("supportAuthType", "-1")
                        }
                    }
                }
            }
            putExtras(args)
            Log.i(TAG, "launchRNWallet: args is $args")

            if (requestCode != -1 && context is Activity) {
                context.startActivityForResult(this, requestCode)
            } else {
                context.startActivity(this)
            }
        }
    }

    private fun requestPayAuth(params: String?) {
        Log.d(TAG, "requestPayAuth() called with: params = $params, promise = ")
//        if (currentActivity !is FragmentActivity) {
//            promise.reject(Throwable("currentActivity not FragmentActivity"))
//            return
//        }

//        val activity = currentActivity as FragmentActivity
        GsonUtils.fromJson(params, PayAuth::class.java)?.apply {
            val callback = object : BiometricCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    val error = errString.toString()
//                    promise.reject(errorCode.toString(), error, Throwable(error))
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    Toast.makeText(this@MainActivity, "Auth success", Toast.LENGTH_SHORT).show()
                }

                override fun onPasswordSucceeded() {
                    Toast.makeText(this@MainActivity, "Auth success", Toast.LENGTH_SHORT).show()
                }

                override fun onPasswordFailed() {
                    super.onPasswordFailed()
                    Toast.makeText(this@MainActivity, "Auth Failed", Toast.LENGTH_SHORT).show()
//                    promise.reject(
//                        ERROR_CODE_PASSWORD_FAILED,
//                        INCORRECT_PASSWORD,
//                        Throwable(INCORRECT_PASSWORD)
//                    )
                }

                override fun onAuthenticationFailed() {
                    val message = getString(R.string.verification_failed_tip)
                        ?: "authentication failed"
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()

//                    promise.reject(ERROR_CODE_BIOMETRIC_FAILED, message, Throwable(message))
                }
            }

            when (payType) {
                PayAuth.UNBIND, PayAuth.MNEMONIC, PayAuth.NEW_WALLET, PayAuth.SWITCH_AUTH_VERIFY, PayAuth.ADD_CHAIN -> {
                    justBiometric(this@MainActivity, this, callback)
                }

                PayAuth.TRANSFER, PayAuth.CONTRACT_TRADING, PayAuth.SIGN, PayAuth.SIGN_EIP712 -> {
                    runOnUiThread {
                        try {
                            AuthDialog.createBuilder(this@MainActivity, this, callback)
                                .show(supportFragmentManager)
                        } catch (e: Exception) {
                            Log.e(TAG, e.stackTraceToString())
//                            promise.reject(Throwable(e.message))
                        }
                    }
                }

                else -> {}
            }
        }
    }

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
}




