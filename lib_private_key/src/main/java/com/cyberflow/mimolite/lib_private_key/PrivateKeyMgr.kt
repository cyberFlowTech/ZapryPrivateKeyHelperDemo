package com.cyberflow.mimolite.lib_private_key

import android.content.Context
import android.util.Log
import com.cyberflow.mimolite.lib_private_key.util.KeyStoreManager

/**
 * @title 助记词、私钥管理类
 * @author Darren.eth
 * @Date 2024/9/13
 */

/**
 * mmkv id
 */
const val MMAP_ID_BIOMETRIC_HELPER = "BiometricHelper" //指纹授权

/**
 * sp
 */
const val SP_WALLET = "sp_wallet"

/**
 * key
 */
const val KEY_MNEMONIC = "_mnemonic"
const val KEY_PRIVATE_KEY = "_privateKey"

// auth
const val SETTING_KEY_PAY_PASSWORD = "_pay_password"
const val SETTING_KEY_PAY_VERIFY = "pay_verify"
const val SETTING_KEY_ENABLE_LAUNCH_VERIFY = "_launch_verify"


object PrivateKeyMgr {

    private const val TAG = "PrivateKeyMgr"

    private val sp
        get() = PrivateKeyApp.context.getSharedPreferences(
            SP_WALLET,
            Context.MODE_PRIVATE
        )


    fun saveMnemonic(userId: String, mnemonic: String?) {
        Log.i(TAG, "saveMnemonic() called")

        if (mnemonic.isNullOrEmpty()) {
            sp.edit().putString(userId + KEY_MNEMONIC, mnemonic ?: "").apply()
        } else {
            KeyStoreManager.getInstance().encrypt(userId, mnemonic).apply {
                Log.d(TAG, "saveMnemonic: encrypt mnemonic $this")
                sp.edit().putString(userId + KEY_MNEMONIC, this ?: "").apply()
            }
        }
    }

    fun getMnemonic(userId: String): String? {
        val mnemonic = sp.getString(userId + KEY_MNEMONIC, "")?.run {
            KeyStoreManager.getInstance().decrypt(userId, this)
        }

        Log.i(TAG, "getMnemonic() returned")
        Log.d(TAG, "getMnemonic: $mnemonic")
        return mnemonic
    }


    fun savePrivateKeyByChain(userId: String, chainCode: String?, privateKey: String?) {
        Log.d(TAG, "savePrivateKeyByChain() called with: chainCode = $chainCode")

        if (privateKey.isNullOrEmpty()) {
            sp.edit().putString(userId + KEY_PRIVATE_KEY + chainCode, "").apply()
        } else {
            KeyStoreManager.getInstance().encrypt(userId, privateKey).apply {
                sp.edit().putString(userId + KEY_PRIVATE_KEY + chainCode, this.orEmpty()).apply()
            }
        }
    }

    fun getPrivateKeyByChain(userId: String, chainCode: String?): String? {
        Log.d(TAG, "getPrivateKeyByChain() called with: chainCode = $chainCode")

        return sp.getString(userId + KEY_PRIVATE_KEY + chainCode, "").run {
            if (isNullOrEmpty()) {
                return@run null
            }

            return@run KeyStoreManager.getInstance().decrypt(userId, this).apply {
                Log.d(TAG, "getPrivateKeyByChain() from decrypt privateKey $this")
            }
        }
    }

    fun savePayPassword(userId: String, password: String) {
        // 保存支付密码
        val encryptPassword = KeyStoreManager.getInstance().encrypt(userId, password)
        encryptPassword?.let {
            sp.edit().putString(userId + SETTING_KEY_PAY_PASSWORD, it).apply()
        }
    }

    fun getPayPassword(userId: String): String? {
        // 获取支付密码
        return sp.getString(userId + SETTING_KEY_PAY_PASSWORD, "")?.run {
            KeyStoreManager.getInstance().decrypt(userId, this)
        }
    }

    fun removeWallet(keys: List<String>) {
        Log.i(TAG, "removeWallet() called")
        val edit = sp.edit()
        keys.forEach { edit.remove(it) }
        edit.apply()
    }
}