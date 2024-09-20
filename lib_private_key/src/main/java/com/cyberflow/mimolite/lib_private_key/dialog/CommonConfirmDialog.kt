package com.cyberflow.mimolite.lib_private_key.dialog

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.cyberflow.mimolite.lib_private_key.R

import com.cyberflow.mimolite.lib_private_key.databinding.DialogCommonConfirmBinding

class CommonConfirmDialog : DialogFragment() {
    private var _binding: DialogCommonConfirmBinding? = null
    private val binding: DialogCommonConfirmBinding get() = _binding!!

    @StringRes
    private var titleRes: Int? = null
    private var title: String? = null

    @DrawableRes
    private var drawableRes: Int? = null
    private var drawable: Drawable? = null

    @StringRes
    private var subTitleRes: Int? = null
    private var subTitle: String? = null

    @StringRes
    private var cancelRes: Int? = R.string.common_cancel
    private var cancel: String? = null

    @StringRes
    private var confirmRes: Int? = R.string.common_ensure
    private var confirm: String? = null

    private var onCancelClick: (() -> Unit)? = null
    private var onConfirmClick: (() -> Unit)? = null

    private var countdownTimer: CountDownTimer? = null
    private var countdownInProgress = false
    private var countdownDurationMillis: Long = 6000
    private var confirmBtnBackgroundDrawable: Drawable? = null
    private var countdownListener: OnCountdownListener? = null

    private var cancelable: Boolean = true


    interface OnCountdownListener {
        fun onCountdownStart()
        fun onCountdownTick(millisUntilFinished: Long)
        fun onCountdownFinish()
    }

    fun setOnCountdownListener(listener: OnCountdownListener) {
        this.countdownListener = listener
    }

    fun setConfirmButtonEnabled(enabled: Boolean) {
        binding.confirmBtn.isEnabled = enabled
    }

    @SuppressLint("StringFormatInvalid")
    fun updateConfirmButtonCountdownText(millisUntilFinished: Long) {
        binding.confirmBtn.text =
            getString(R.string.settings_logout_check_confirm, millisUntilFinished / 1000)
    }

    companion object {

        @Deprecated("Use Builder() instead")
        fun newInstance(
            title: String? = null,
            subTitle: String? = null,
            onCancelClick: (() -> Unit)? = null,
            onConfirmClick: (() -> Unit)? = null
        ): CommonConfirmDialog {
            val dialog = CommonConfirmDialog()

            dialog.title = title
            dialog.subTitle = subTitle

            dialog.onCancelClick = onCancelClick
            dialog.onConfirmClick = onConfirmClick
            return dialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCommonConfirmBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (binding.subtitleTv.lineCount == 1) {
                    binding.subtitleTv.gravity = Gravity.CENTER
                }
            }
        })
    }

    private fun initView() {
        if (!title.isNullOrEmpty()) {
            binding.titleTv.isVisible = true
            binding.titleTv.text = title
        }

        if (titleRes != null) {
            binding.titleTv.isVisible = true
            binding.titleTv.text = getString(titleRes!!)
        }

        if (drawableRes != null) {
            binding.ivIcon.isVisible = true
            binding.ivIcon.setImageResource(drawableRes!!)
        }

        if (drawable != null) {
            binding.ivIcon.isVisible = true
            binding.ivIcon.setImageDrawable(drawable)
        }

        if (!subTitle.isNullOrEmpty()) {
            binding.subtitleTv.isVisible = true
            binding.subtitleTv.text = subTitle
        }

        if (subTitleRes != null) {
            binding.subtitleTv.isVisible = true
            binding.subtitleTv.text = getString(subTitleRes!!)
        }

        if (!cancel.isNullOrEmpty()) {
            binding.cancelBtn.isVisible = true
            binding.cancelBtn.text = cancel
        }

        if (cancelRes != null) {
            binding.cancelBtn.isVisible = true
            binding.cancelBtn.text = getString(cancelRes!!)
        }

        if (!confirm.isNullOrEmpty()) {
            binding.confirmBtn.isVisible = true
            binding.confirmBtn.text = confirm
        }

        if (confirmRes != null) {
            binding.confirmBtn.isVisible = true
            binding.confirmBtn.text = getString(confirmRes!!)
        }

        binding.cancelBtn.setOnClickListener {
            onCancelClick?.invoke()
            dismiss()
        }

        binding.confirmBtn.setOnClickListener {
            onConfirmClick?.invoke()
            dismiss()
        }

        if (confirmBtnBackgroundDrawable != null) {
            binding.confirmBtn.background = confirmBtnBackgroundDrawable
            countdownTimer = object : CountDownTimer(countdownDurationMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    countdownListener?.onCountdownTick(millisUntilFinished)
                }

                override fun onFinish() {
                    countdownListener?.onCountdownFinish()
                    countdownTimer?.cancel()
                    binding.confirmBtn.text = getString(R.string.tribe_confirm)
                    val originDrawable = dialog?.context?.let {
                        ContextCompat.getDrawable(
                            it,
                            R.drawable.btn_blue_confirm_shape_30
                        )
                    }
                    binding.confirmBtn.background = originDrawable
                }
            }
            if (!countdownInProgress) {
                countdownListener?.onCountdownStart()
                countdownTimer?.start()
            }
        }

        isCancelable = cancelable
    }


    override fun onStart() {
        super.onStart()
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)
        dialog?.window?.setLayout(
            (dm.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(null)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        countdownTimer?.cancel()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        countdownTimer?.cancel()
    }

    class Builder() {

        @StringRes
        private var titleRes: Int? = null
        private var title: String? = null

        @DrawableRes
        private var drawableRes: Int? = null
        private var drawable: Drawable? = null

        @StringRes
        private var subTitleRes: Int? = null
        private var subTitle: String? = null

        @StringRes
        private var cancelRes: Int? = R.string.common_cancel
        private var cancel: String? = null

        @StringRes
        private var confirmRes: Int? = R.string.common_ensure
        private var confirm: String? = null

        private var onCancelClick: (() -> Unit)? = null
        private var onConfirmClick: (() -> Unit)? = null
        private var countdownDurationMillis: Long = 6000 // 默认倒计时
        private var confirmBtnBackgroundDrawable: Drawable? = null

        private var cancelable: Boolean = true

        fun setCountdownDurationMillis(durationMillis: Long): Builder {
            this.countdownDurationMillis = durationMillis
            return this
        }

        fun build(): CommonConfirmDialog {
            val dialog = CommonConfirmDialog()
            dialog.titleRes = titleRes
            dialog.title = title

            dialog.drawableRes = drawableRes
            dialog.drawable = drawable

            dialog.subTitleRes = subTitleRes
            dialog.subTitle = subTitle

            dialog.cancelRes = cancelRes
            dialog.cancel = cancel

            dialog.confirmRes = confirmRes
            dialog.confirm = confirm

            dialog.onCancelClick = onCancelClick
            dialog.onConfirmClick = onConfirmClick

            dialog.confirmBtnBackgroundDrawable = confirmBtnBackgroundDrawable

            dialog.cancelable = cancelable

            // 设置倒计时监听器
            dialog.setOnCountdownListener(object : OnCountdownListener {
                override fun onCountdownStart() {
                    // 倒计时开始时，禁用确认按钮
                    dialog.setConfirmButtonEnabled(false)
                }

                override fun onCountdownTick(millisUntilFinished: Long) {
                    dialog.updateConfirmButtonCountdownText(millisUntilFinished)
                }

                override fun onCountdownFinish() {
                    dialog.setConfirmButtonEnabled(true)
                }
            })
            return dialog
        }

        fun setTitle(title: String?): Builder {
            this.titleRes = null
            this.title = title
            return this
        }

        fun setTitle(@StringRes title: Int?): Builder {
            this.titleRes = title
            this.title = null
            return this
        }

        fun setDrawable(drawable: Drawable?): Builder {
            this.drawableRes = null
            this.drawable = drawable
            return this
        }

        fun setDrawable(@DrawableRes drawable: Int?): Builder {
            this.drawableRes = drawable
            this.drawable = null
            return this
        }

        fun setSubTitle(subTitle: String?): Builder {
            this.subTitleRes = null
            this.subTitle = subTitle
            return this
        }

        fun setSubTitle(@StringRes subTitle: Int?): Builder {
            this.subTitleRes = subTitle
            this.subTitle = null
            return this
        }

        fun setCancel(cancel: String?, onCancelClick: (() -> Unit)? = this.onCancelClick): Builder {
            this.cancelRes = null
            this.cancel = cancel
            this.onCancelClick = onCancelClick
            return this
        }


        fun setCancel(
            @StringRes cancel: Int?,
            onCancelClick: (() -> Unit)? = this.onCancelClick
        ): Builder {
            this.cancelRes = cancel
            this.cancel = null
            this.onCancelClick = onCancelClick
            return this
        }

        fun setOnCancelClick(onCancelClick: () -> Unit): Builder {
            this.onCancelClick = onCancelClick
            return this
        }

        fun setConfirm(
            confirm: String?,
            onConfirmClick: (() -> Unit)? = this.onConfirmClick
        ): Builder {
            this.confirmRes = null
            this.confirm = confirm
            this.onConfirmClick = onConfirmClick
            return this
        }


        fun setConfirm(
            @StringRes confirm: Int?,
            onConfirmClick: (() -> Unit)? = this.onConfirmClick
        ): Builder {
            this.confirmRes = confirm
            this.confirm = null
            this.onConfirmClick = onConfirmClick
            return this
        }

        fun setOnConfirmClick(onConfirmClick: () -> Unit): Builder {
            this.onConfirmClick = onConfirmClick
            return this
        }

        fun setConfirmBtnBackground(drawable: Drawable?): Builder {
            this.confirmBtnBackgroundDrawable = drawable
            return this
        }

        /**
         * 点按屏幕其他位置、返回键时，弹窗是否可取消
         */
        fun setCancelable(
            cancelable:Boolean
        ): Builder {
            this.cancelable = cancelable
            return this
        }
    }
}