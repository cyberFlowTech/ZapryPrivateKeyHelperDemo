package com.cyberflow.mimolite.lib_private_key.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.cyberflow.mimolite.lib_private_key.R

class PureColorButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(
    context, attrs, defStyleAttr
) {

    private var drawableHorizontalCenter = false
    init {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.PureColorButton,
            defStyleAttr,
            R.style.Theme_client_Button_Default
        )
        var cornerRadius = 0F
        var normalSolidColor = 0
        var normalStrokeColor = 0
        var normalStrokeWidth = 0F
        var pressedSolidColor = 0
        var pressedStrokeColor = 0
        var pressedStrokeWidth = 0F
        var disabledSolidColor = 0
        var disabledStrokeColor = 0
        var disabledStrokeWidth = 0F
        for (i in 0 until a.indexCount) {
            when (val attr = a.getIndex(i)) {
                R.styleable.PureColorButton_corner_radius -> {
                    cornerRadius = a.getDimension(attr, 0F)
                }

                R.styleable.PureColorButton_normal_solid_color -> {
                    normalSolidColor = a.getColor(attr, Color.TRANSPARENT)
                }

                R.styleable.PureColorButton_normal_stroke_color -> {
                    normalStrokeColor = a.getColor(attr, Color.TRANSPARENT)
                }

                R.styleable.PureColorButton_normal_stroke_width -> {
                    normalStrokeWidth = a.getDimension(attr, 0F)
                }

                R.styleable.PureColorButton_pressed_solid_color -> {
                    pressedSolidColor = a.getColor(attr, Color.TRANSPARENT)
                }

                R.styleable.PureColorButton_pressed_stroke_color -> {
                    pressedStrokeColor = a.getColor(attr, Color.TRANSPARENT)
                }

                R.styleable.PureColorButton_pressed_stroke_width -> {
                    pressedStrokeWidth = a.getDimension(attr, 0F)
                }

                R.styleable.PureColorButton_disabled_solid_color -> {
                    disabledSolidColor = a.getColor(attr, Color.TRANSPARENT)
                }

                R.styleable.PureColorButton_disabled_stroke_color -> {
                    disabledStrokeColor = a.getColor(attr, Color.TRANSPARENT)
                }

                R.styleable.PureColorButton_disabled_stroke_width -> {
                    disabledStrokeWidth = a.getDimension(attr, 0F)
                }

                R.styleable.PureColorButton_drawable_horizontal_center -> {
                    drawableHorizontalCenter = a.getBoolean(attr, false)
                }

            }
        }
        a.recycle()

        val gNormal = GradientDrawable().also {
            it.shape = GradientDrawable.RECTANGLE
            it.setColor(normalSolidColor)
            it.cornerRadius = cornerRadius
            it.setStroke(normalStrokeWidth.toInt(), normalStrokeColor)
        }
        val gPressed = GradientDrawable().also {
            it.shape = GradientDrawable.RECTANGLE
            it.setColor(pressedSolidColor)
            it.cornerRadius = cornerRadius
            it.setStroke(pressedStrokeWidth.toInt(), pressedStrokeColor)
        }
        val gDisabled = GradientDrawable().also {
            it.shape = GradientDrawable.RECTANGLE
            it.setColor(disabledSolidColor)
            it.cornerRadius = cornerRadius
            it.setStroke(disabledStrokeWidth.toInt(), disabledStrokeColor)
        }

        val sd = StateListDrawable().apply {
            addState(intArrayOf(-android.R.attr.state_enabled), gDisabled)
            addState(intArrayOf(android.R.attr.state_pressed), gPressed)
            addState(intArrayOf(), gNormal)
        }
        setBackgroundDrawable(sd)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {

        if (!drawableHorizontalCenter) {
            return
        }

        val drawables = compoundDrawables //drawables always not null;
        val drawableLeft = drawables[0]
        val drawableTop = drawables[1]
        val drawableRight = drawables[2]
        val drawableBottom = drawables[3]
        val text = text.toString()
        val textWidth = paint.measureText(text, 0, text.length)
        var totalDrawablePaddingH =
            0 //the total horizontal padding of drawableLeft and drawableRight
        var totalDrawableWidth = 0 //the total width of drawableLeft and drawableRight
        val totalWidth: Float //the total width of drawableLeft , drawableRight and text
        val paddingH: Int //the horizontal padding,used both left and right

        // measure width
        if (drawableLeft != null) {
            totalDrawableWidth += drawableLeft.intrinsicWidth
            totalDrawablePaddingH += compoundDrawablePadding //drawablePadding
        }
        if (drawableRight != null) {
            totalDrawableWidth += drawableRight.intrinsicWidth
            totalDrawablePaddingH += compoundDrawablePadding
        }
        totalWidth = textWidth + totalDrawableWidth + totalDrawablePaddingH
        paddingH = (width - totalWidth).toInt() / 2

        // reset padding.
        setPadding(paddingH, 0, paddingH, 0) //this method calls invalidate() inside;
    }
}