package com.cyberflow.mimolite.lib_private_key.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.biometric.BiometricPrompt
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentManager
import com.cyberflow.mimolite.lib_private_key.PrivateKeyLib
import com.cyberflow.mimolite.lib_private_key.PrivateKeyMgr
import com.cyberflow.mimolite.lib_private_key.R
import com.cyberflow.mimolite.lib_private_key.biometric.BiometricCallback
import com.cyberflow.mimolite.lib_private_key.biometric.BiometricHelper
import com.cyberflow.mimolite.lib_private_key.databinding.DialogBiometricVerifyBinding
import com.cyberflow.mimolite.lib_private_key.event.FlowEventMgr
import com.cyberflow.mimolite.lib_private_key.event.PrivateKeyFlowEvent
import com.cyberflow.mimolite.lib_private_key.ext.IoScope
import com.cyberflow.mimolite.lib_private_key.ext.getParcelableExtraCompat
import com.cyberflow.mimolite.lib_private_key.model.PayAuth
import com.cyberflow.mimolite.lib_private_key.util.KeyboardUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class AuthDialog : BottomSheetDialogFragment() {
    private lateinit var callback: BiometricCallback
    private var _binding: DialogBiometricVerifyBinding? = null
    private val binding: DialogBiometricVerifyBinding get() = _binding!!

    private val userId get() = PrivateKeyLib.userId


    private fun TextView.fillText(text: String) {
        this.isVisible = text.isNotEmpty()
        this.text = text
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogBiometricVerifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.ivClose.setOnClickListener {
            dismiss()
            callback.onAuthenticationError(BiometricPrompt.ERROR_CANCELED, "")
        }

        when (BiometricHelper.getVerifyType()) {
            BiometricHelper.VerifyType.NOT_SETTING -> {
                // 没有设置验证方式，先跳设置验证方式的页面
                Handler(Looper.getMainLooper()).postDelayed({
                    alertSetVerifyType()
                }, 500)
            }

            BiometricHelper.VerifyType.PASSWORD -> {
                showPassword()
            }

            BiometricHelper.VerifyType.BIOMETRICS -> {
                shoBiometrics()
            }
        }

        arguments?.apply {
            val args = getMainArgs()
            binding.tvTitle.text = args.title

            binding.tvMessage.let {
                it.text = args.message
                it.isVisible = args.message.isNotEmpty()
            }

            when (args) {
                is AuthDialogArgs.NonTransfer -> {
                    binding.groupCurrency.isVisible = false
                    binding.groupNft.isVisible = false
                    binding.tvContent.fillText(args.content)
                }

                is AuthDialogArgs.TransferCurrency -> {
                    binding.tvContent.isVisible = false
                    binding.groupNft.isVisible = false
                    binding.groupCurrency.isVisible = true
                    binding.tvAmount.fillText(args.amount)
                    binding.tvCurrency.fillText(args.currency)
                }

                is AuthDialogArgs.TransferNft -> {
                    binding.groupCurrency.isVisible = false
                    binding.tvContent.isVisible = false
                    binding.groupNft.isVisible = true
                    binding.tvNftName.fillText(args.nftName)
                    binding.tvNftTokenId.fillText(args.nftTokenId)
                }
            }
        }
    }

    private fun alertSetVerifyType() {
        IoScope.launch {
            FlowEventMgr.instance.fireEvent(
                PrivateKeyFlowEvent.ShowAuthSettingDialogEvent(requireContext())
            )
        }
//        SettingAlertDialog(requireContext()).show()
        callback.onAuthenticationFailed()
        dismiss()
    }

    private fun showPassword() {
        binding.ivFingerprint.isVisible = false
        binding.groupPassword.isVisible = true

        // 直接弹出键盘无效 所以延迟了200ms
        Handler(Looper.getMainLooper()).postDelayed({
            binding.etPassword.requestFocus()
            KeyboardUtil.show(binding.etPassword)
        }, 200)
        setPasswordListener()
    }

    private fun shoBiometrics() {
        binding.ivFingerprint.isVisible = true
        binding.groupPassword.isVisible = false

        binding.ivFingerprint.setOnClickListener {
            BiometricHelper.promptBiometric(
                requireActivity(),
                object : BiometricCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        callback.onAuthenticationError(errorCode, errString)
                        dismiss()
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        callback.onAuthenticationSucceeded(result)
                        dismiss()
                    }

                    override fun onPasswordSucceeded() {
                        callback.onPasswordSucceeded()
                        dismiss()
                    }

                    override fun onPasswordFailed() {
                        callback.onPasswordFailed()
                        dismiss()
                    }

                    override fun onAuthenticationFailed() {
                        callback.onAuthenticationFailed()
                        dismiss()
                    }
                }
            )
        }
    }

    private fun setPasswordListener() {
        binding.etPassword.addTextChangedListener {
            when (it?.length ?: 0) {
                1 -> {
                    binding.ivPassword0.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword1.setImageDrawable(null)
                    binding.ivPassword2.setImageDrawable(null)
                    binding.ivPassword3.setImageDrawable(null)
                    binding.ivPassword4.setImageDrawable(null)
                    binding.ivPassword5.setImageDrawable(null)
                }

                2 -> {
                    binding.ivPassword0.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword1.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword2.setImageDrawable(null)
                    binding.ivPassword3.setImageDrawable(null)
                    binding.ivPassword4.setImageDrawable(null)
                    binding.ivPassword5.setImageDrawable(null)
                }

                3 -> {
                    binding.ivPassword0.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword1.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword2.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword3.setImageDrawable(null)
                    binding.ivPassword4.setImageDrawable(null)
                    binding.ivPassword5.setImageDrawable(null)
                }

                4 -> {
                    binding.ivPassword0.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword1.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword2.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword3.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword4.setImageDrawable(null)
                    binding.ivPassword5.setImageDrawable(null)
                }

                5 -> {
                    binding.ivPassword0.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword1.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword2.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword3.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword4.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword5.setImageDrawable(null)
                }

                6 -> {
                    binding.ivPassword0.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword1.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword2.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword3.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword4.setImageResource(R.drawable.ic_black_dot)
                    binding.ivPassword5.setImageResource(R.drawable.ic_black_dot)
                    val payPwd = userId.let { uid -> PrivateKeyMgr.getPayPassword(uid) }
                    if (payPwd == it.toString()) {
                        callback.onPasswordSucceeded()
                        dismiss()
                    } else {
                        callback.onPasswordFailed()
//                        dismiss()
                        resetPwdInputUI()
                        binding.etPassword.text?.clear()
                    }
                }

                else -> {
                    resetPwdInputUI()
                }
            }
        }
    }

    private fun resetPwdInputUI() {
        binding.ivPassword0.setImageDrawable(null)
        binding.ivPassword1.setImageDrawable(null)
        binding.ivPassword2.setImageDrawable(null)
        binding.ivPassword3.setImageDrawable(null)
        binding.ivPassword4.setImageDrawable(null)
        binding.ivPassword5.setImageDrawable(null)
    }

    companion object {
        private const val TAG = "AuthDialog"
        private const val ARG_MAIN = "main"

        private fun AuthDialog.getMainArgs(): AuthDialogArgs {
            return requireArguments().getParcelableExtraCompat(
                ARG_MAIN,
                AuthDialogArgs::class.java
            )!!
        }

        fun createBuilder(
            context: Context,
            message: String,
            amount: String,
            currency: String,
            callback: BiometricCallback,
        ): Builder {
            val argsMain = AuthDialogArgs.TransferCurrency(
                title = getTitleRes(context),
                message = message, amount = amount,
                currency = currency,
            )
            return Builder(context, argsMain, callback)
        }

        fun createBuilder(
            context: Context,
            payAuth: PayAuth,
            callback: BiometricCallback
        ): Builder {
            val argsMain = when (payAuth.payType) {
                PayAuth.TRANSFER -> {
                    val nftTokenId = payAuth.data?.nftTokenId.orEmpty()
                    if (nftTokenId.isNotEmpty()) {
                        val m = context.getString(
                            R.string.biometric_transferring_nft,
                            payAuth.displayNick(),
                            payAuth.displayAddress()
                        )
                        AuthDialogArgs.TransferNft(
                            title = getTitleRes(context),
                            message = m,
                            nftName = payAuth.data?.nftName.orEmpty(),
                            nftTokenId = "#$nftTokenId",
                        )
                    } else {
                        val m = context.getString(
                            R.string.biometric_transferring,
                            payAuth.displayNick(),
                            payAuth.displayAddress()
                        )
                        AuthDialogArgs.TransferCurrency(
                            title = getTitleRes(context),
                            message = m, amount = payAuth.data?.amount.orEmpty(),
                            currency = payAuth.data?.token?.token.orEmpty(),
                        )
                    }
                }

                PayAuth.CONTRACT_TRADING -> {
                    val m = if (payAuth.displayAddress().isNotEmpty()) {
                        context.getString(
                            R.string.biometric_transferring,
                            payAuth.displayNick(),
                            payAuth.displayAddress()
                        )
                    } else {
                        null
                    }
                    AuthDialogArgs.NonTransfer(
                        title = getTitleRes(context),
                        message = m.orEmpty(),
                        content = context.getString(R.string.biometric_contract_trading)
                    )
                }

                PayAuth.SIGN -> {
                    val m = if (payAuth.displayAddress().isNotEmpty()) {
                        context.getString(
                            R.string.biometric_transferring,
                            payAuth.displayNick(),
                            payAuth.displayAddress()
                        )
                    } else {
                        null
                    }
                    AuthDialogArgs.NonTransfer(
                        getTitleRes(context),
                        m.orEmpty(),
                        content = context.getString(R.string.signature_message_sign)
                    )
                }

                PayAuth.SIGN_EIP712 -> {
                    val m = context.getString(
                        R.string.biometric_transferring,
                        payAuth.displayNick(),
                        payAuth.displayAddress()
                    )
                    AuthDialogArgs.TransferCurrency(
                        title = getTitleRes(context),
                        message = m, amount = payAuth.data?.amount.orEmpty(),
                        currency = payAuth.data?.token?.token.orEmpty(),
                    )
                }

                else -> {
                    throw IllegalArgumentException("payType is not supported")
                }
            }
            return Builder(context, argsMain, callback)
        }

        private fun getTitleRes(context: Context): String {
            return when (BiometricHelper.getVerifyType()) {
                BiometricHelper.VerifyType.NOT_SETTING -> {
                    IoScope.launch {
                        FlowEventMgr.instance.fireEvent(
                            PrivateKeyFlowEvent.ShowAuthSettingDialogEvent(context)
                        )
                    }
                    ""
                }

                BiometricHelper.VerifyType.PASSWORD -> {
                    context.getString(R.string.biometric_input_pay_password)
                }

                BiometricHelper.VerifyType.BIOMETRICS -> {
                    context.getString(R.string.biometric_fingerprint_verify)
                }
            }
        }
    }

    class Builder(
        val context: Context,
        private val argsMain: AuthDialogArgs,
        val callback: BiometricCallback,
    ) {
        fun show(fragmentManager: FragmentManager) {
            when (BiometricHelper.getVerifyType()) {
                BiometricHelper.VerifyType.NOT_SETTING -> {
                    callback.onAuthenticationFailed()
                    return
                }

                else ->
                    build().show(fragmentManager, "AuthDialog")
            }
        }

        @SuppressLint("ResourceType")
        private fun build(): AuthDialog {
            val dialog = AuthDialog()
            dialog.arguments = Bundle().apply {
                putParcelable(ARG_MAIN, argsMain)
            }
            dialog.isCancelable = false
            dialog.callback = callback
            return dialog
        }
    }
}

sealed class AuthDialogArgs(
    open val title: String,
    open val message: String,
) : Parcelable {
    @Parcelize
    class TransferCurrency(
        override val title: String,
        override val message: String,
        val amount: String,
        val currency: String,
    ) : AuthDialogArgs(title, message)

    @Parcelize
    class NonTransfer(
        override val title: String,
        override val message: String,
        val content: String,
    ) : AuthDialogArgs(title, message)

    @Parcelize
    class TransferNft(
        override val title: String,
        override val message: String,
        val nftName: String,
        val nftTokenId: String,
    ) : AuthDialogArgs(title, message)
}