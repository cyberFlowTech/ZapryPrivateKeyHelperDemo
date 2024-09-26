package com.zapry.pkdemo.dialog

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import com.cyberflow.mimolite.lib_private_key.biometric.BiometricHelper
import com.cyberflow.mimolite.lib_private_key.databinding.DialogBiometriceSettingBinding
import com.zapry.pkdemo.R

class SettingAlertDialog(context: Context) : AppCompatDialog(context) {

    val binding by lazy {
        DialogBiometriceSettingBinding.inflate(LayoutInflater.from(context))
    }
    private var showPassword = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val biometricEnable = BiometricHelper.checkBiometric(context)

        binding.tvTitle.text = if (showPassword || !biometricEnable) {
            context.getString(R.string.biometric_setting_pay_password)
        } else {
            context.getString(R.string.biometric_setting_biometric)
        }

        binding.tvSubTitle.text = if (showPassword || !biometricEnable) {
            context.getString(R.string.biometric_setting_pay_password_subtitle)
        } else {
            context.getString(R.string.biometric_setting_biometric_subtitle)
        }

        binding.tvDesc.isVisible = true
        binding.tvDesc.text = if (showPassword || !biometricEnable) {
            context.getString(R.string.biometric_setting_pay_password_desc)
        } else {
            context.getString(R.string.biometric_setting_biometric_desc)
        }

        binding.tvPositive.setOnClickListener {
            dismiss()
        }
        binding.tvNegative.setOnClickListener {
            if (!showPassword) {
                SettingAlertDialog(context).also {
                    it.showPassword = true
                    it.show()
                }
            }
            dismiss()
        }
    }

    override fun show() {
        super.show()
        val window = window ?: return
        window.attributes.apply {
            gravity = Gravity.CENTER

            val dm: DisplayMetrics = context.resources.displayMetrics
            val w: Int = dm.widthPixels
            val h: Int = dm.heightPixels
            val temp:Int = if (w > h) {
                h
            } else {
                w
            }

            width = (temp * 0.85).toInt()
            height = WindowManager.LayoutParams.WRAP_CONTENT
            dimAmount = 0.6f
            window.attributes = this
        }
        window.setBackgroundDrawable(null)
    }
}