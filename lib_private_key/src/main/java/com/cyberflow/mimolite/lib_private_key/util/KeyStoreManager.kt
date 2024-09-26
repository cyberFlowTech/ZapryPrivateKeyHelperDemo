package com.cyberflow.mimolite.lib_private_key.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.KeyStoreException
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.annotation.RequiresApi
import com.cyberflow.mimolite.lib_private_key.PrivateKeyLib
import com.cyberflow.mimolite.lib_private_key.SP_WALLET
import com.cyberflow.mimolite.lib_private_key.ext.masked
import com.tencent.mmkv.MMKV
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.UnrecoverableKeyException
import java.util.Calendar
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.security.auth.x500.X500Principal


class KeyStoreManager private constructor() {

    private val keyStore: KeyStore = KeyStore.getInstance(KEY_TYPE)

    init {
        keyStore.load(null)
    }

    private val alias = "mimo-key-alias"

    private val sp
        get() = PrivateKeyLib.context.getSharedPreferences(
            SP_WALLET,
            Context.MODE_PRIVATE
        )
    private val kv by lazy { MMKV.defaultMMKV() }

    /**
     * 只能在API 23以上调用
     */
    @RequiresApi(Build.VERSION_CODES.M)
    @Throws(
        KeyStoreException::class,
        NoSuchAlgorithmException::class,
        UnrecoverableKeyException::class
    )
    private fun getSecretKey(): SecretKey {
        if (!keyStore.containsAlias(alias)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_TYPE)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build()
            )
            Log.d(TAG, "getSecretKey: create new key")
            return keyGenerator.generateKey()
        } else {
            Log.d(TAG, "getSecretKey: get key from keystore")
            return keyStore.getKey(alias, null) as SecretKey
        }
    }

    /**
     * API 在23以下调用
     */
    @SuppressLint("WrongConstant")
    @Deprecated("API 在23以下调用")
    private fun generateKeyPair() {
        if (!keyStore.containsAlias(alias)) {
            // 对于API 21和22
            // 使用适用于这些API级别的方法，例如使用KeyPairGeneratorSpec
            // 注意：这里可能需要使用非对称加密，因为对称密钥生成在API 23之前不受支持
            val notBefore: Calendar = Calendar.getInstance()
            val notAfter: Calendar = Calendar.getInstance()
            notAfter.add(Calendar.YEAR, 1)

            val spec = KeyPairGeneratorSpec.Builder(PrivateKeyLib.context)
                .setAlias(alias)
                .setSubject(X500Principal("CN=$alias"))
                .setSerialNumber(BigInteger.ONE)
                .setStartDate(notBefore.time)
                .setEndDate(notAfter.time)
                .setKeySize(2048) // 可以根据需要设置密钥大小
                .setKeyType("RSA") // 设置密钥类型为 RSA
                .build()

            val generator = KeyPairGenerator.getInstance("RSA", KEY_TYPE)
            generator.initialize(spec)
            generator.generateKeyPair()
        }
    }

    /**
     * API 在23以下调用
     */
    @Deprecated("API 在23以下调用")
    private fun encryptData(dataToEncrypt: ByteArray?): ByteArray? {
        return try {
            val publicKey = keyStore.getCertificate(alias).publicKey
            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            cipher.doFinal(dataToEncrypt)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * API 在23以下调用
     */
    @Deprecated("API 在23以下调用")
    private fun decryptData(encryptedData: ByteArray?): ByteArray? {
        return try {
            val privateKey = keyStore.getKey(alias, null) as PrivateKey
            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            cipher.doFinal(encryptedData)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun encrypt(userId: String, data: String): String? {
        Log.d(TAG, "encrypt() called with")
        val encodeToBase64 = StringUtils.wrapBase64(data)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val secretKey = getSecretKey()

                val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                val encryptionBytes = cipher.doFinal(StringUtils.decodeFromBase64(encodeToBase64))

                // Key字符串过长会保存不了，这里只截取前10位
                val result = StringUtils.encodeToBase64(encryptionBytes)
                val key = if (result.length < 10) {
                    userId + result
                } else {
                    userId + result.substring(0, 10)
                }

                val ivStr = StringUtils.encodeToBase64(cipher.iv)
                Log.i(TAG, "encrypt: iv is ${ivStr.masked()}")
                sp.edit().putString(key, ivStr).apply()

                result
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            generateKeyPair()
            encryptData(StringUtils.decodeFromBase64(encodeToBase64))?.run {
                StringUtils.encodeToBase64(this)
            }
        }
    }

    fun decrypt(userId: String, data: String): String? {
        Log.d(TAG, "decrypt() called with: data = $data")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val secretKey = getSecretKey()

                val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")

                // Key字符串过长会保存不了，这里只截取前10位
                val key = if (data.length < 10) {
                    userId + data
                } else {
                    userId + data.substring(0, 10)
                }
                Log.i(TAG, "decrypt: key is ${key.masked()}")
                tranDecryptData2SP(key)
                val iv = sp.getString(key, "")?.run {
                    Log.i(TAG, "decrypt() called iv is ${this.masked()}")
                    StringUtils.decodeFromBase64(this)
                }

                cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

                val decryptionBytes = cipher.doFinal(StringUtils.decodeFromBase64(data))

                StringUtils.encodeToBase64(decryptionBytes).let {
                    StringUtils.unwrapBase64(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            generateKeyPair()
            decryptData(StringUtils.decodeFromBase64(data))?.run {
                StringUtils.encodeToBase64(this).let {
                    StringUtils.unwrapBase64(it)
                }
            }
        }
    }

    // TODO: 过段时间删除迁移代码 
    fun tranBack2SP(mmkv: MMKV, key: String, data: String) {
        Log.d(TAG, "tranBack2SP: key $key")
        if (data.isEmpty()) {
            return
        }

        val value = mmkv.getString(key, "")
        if (!value.isNullOrEmpty()) {
            sp.edit().putString(key, value).apply()
            mmkv.remove(key)
            Log.e(TAG, "tranBack2SP: success key:$key")
        }
    }

    private fun tranDecryptData2SP(key: String) {
        Log.d(TAG, "tranDecryptData2SP: ")
        kv.getString(key, "").apply {
            if (this.isNullOrEmpty()) {
                return
            }

            sp.edit().putString(key, this).apply()
            kv.remove(key)
            Log.d(TAG, "tranDecryptData2SP: success key$key")
        }
    }

    fun tranIntBack2SP(mmkv: MMKV, key: String) {
        Log.d(TAG, "tranIntBack2SP: key $key")
        val value = mmkv.getInt(key, -1)

        if (value == -1) {
            return
        }

        sp.edit().putInt(key, value).apply()
        mmkv.remove(key)
        Log.e(TAG, "tranIntBack2SP: success key:$key")

    }

    fun tranBoolBack2SP(mmkv: MMKV, key: String) {
        Log.d(TAG, "tranBoolBack2SP: key $key")
        val value = mmkv.getBoolean(key, false)

        if (!value) {
            return
        }

        sp.edit().putBoolean(key, value).apply()
        mmkv.remove(key)
        Log.e(TAG, "tranBoolBack2SP:==== success key:$key")
    }

    companion object {
        private const val TAG = "KeyStoreManager"
        private const val KEY_TYPE = "AndroidKeyStore"

        private val instance = KeyStoreManager()

        fun getInstance() = instance
    }
}