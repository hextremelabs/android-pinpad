package com.hextremelabs.pinpad

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Vibrator
import android.support.v4.view.ViewCompat
import android.support.v7.widget.AppCompatImageButton
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.animation.CycleInterpolator
import uk.co.chrisjenx.calligraphy.TypefaceUtils


/**
 * @author ADIO Kingsley O.
 * @since 16 Apr, 2017
 */
class PinpadView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = R.style.PinpadView) : ViewGroup(context, attrs, defStyleAttr, defStyleRes), View.OnClickListener {

    private var keySpacing: Float
    private var numDigits: Int

    private var passcode: String = ""

    var viewProvider: ViewProvider? = null
    var callback: Callback? = null

    private val keys: List<Key> = listOf(
      Key('1', ""),
      Key('2', "ABC"),
      Key('3', "DEF"),
      Key('4', "GHI"),
      Key('5', "JKL"),
      Key('6', "MNO"),
      Key('7', "PQRS"),
      Key('8', "TUV"),
      Key('9', "WXYZ"),
      Key(KEY_HELP, ""),
      Key('0', "+"),
      Key(KEY_DEL, "")
    )

    init {
        val values = context.obtainStyledAttributes(attrs, R.styleable.PinpadView, defStyleAttr, defStyleRes)
        try {
            keySpacing = values.getDimension(R.styleable.PinpadView_keySpacing, 0F)
            numDigits = values.getInt(R.styleable.PinpadView_numDigits, 0)
            keys.forEach {
                val keyView = KeyView(context, values, it)
                val keyPadding = keySpacing.toInt()
                keyView.setPadding(keyPadding, keyPadding, keyPadding, keyPadding)
                ViewCompat.setBackground(keyView, values.getDrawable(R.styleable.PinpadView_keyBackground))
                keyView.isHapticFeedbackEnabled = true
                keyView.setOnClickListener(this)
                addView(keyView)
            }
        } finally {
            values.recycle()
        }
    }

    override fun onClick(v: View) {
        if (v !is KeyView) return
        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

        val char = v.key.char
        when (char) {
            KEY_DEL -> {
                if (passcode.isNotEmpty()) {
                    passcode = passcode.substring(0, passcode.lastIndex)
                    viewProvider?.onDeleteChar()
                }
            }
            KEY_HELP -> {
                callback?.onHelpRequest()
            }
            else -> {
                if (passcode.length < numDigits) {
                    passcode += char
                    viewProvider?.onAppendChar(char)
                    if (passcode.length == numDigits) {
                        callback?.onPasscodeComplete(passcode)
                    }
                }
            }
        }
    }

    fun reset() {
        passcode = ""
        viewProvider?.onReset()
    }

    fun fail(reset: Boolean = true) {
        if (reset) {
            reset()
        }
        runFailAnimation()
    }

    private fun runFailAnimation() {
        val shakeAnimator = ValueAnimator.ofInt(0, 15)
        shakeAnimator.duration = DURATION_ANIMATION
        shakeAnimator.interpolator = CycleInterpolator(3F)
        shakeAnimator.addUpdateListener { animation ->
            translationX = (animation.animatedValue as Int).toFloat()
        }
        shakeAnimator.start()
        val v = context.systemService<Vibrator>(Context.VIBRATOR_SERVICE)
        v.vibrate(DURATION_ANIMATION)
    }

    private fun childrenSequence(): Sequence<KeyView> {
        return (0..childCount - 1)
          .asSequence()
          .map { getChildAt(it) }
          .filterIsInstance(KeyView::class.java)
    }

    fun setTypeface(tf: Typeface) {
        childrenSequence()
          .forEach { it.setTypeface(tf) }

        requestLayout()
        invalidate()
    }

    fun setKeyBackgroundDrawable(drawable: Drawable) {
        childrenSequence()
          .forEach { ViewCompat.setBackground(it, drawable) }
    }

    fun setKeyTextSize(size: Float) {
        childrenSequence()
          .forEach { it.setTextSize(size) }

        requestLayout()
        invalidate()
    }

    fun setKeySubTextSize(size: Float) {
        childrenSequence()
          .forEach { it.setSubTextSize(size) }

        requestLayout()
        invalidate()
    }

    fun setKeyTextColor(color: Int) {
        childrenSequence()
          .forEach { it.setTextColor(color) }
        invalidate()
    }

    fun setKeySubTextColor(color: Int) {
        childrenSequence()
          .forEach { it.setSubTextColor(color) }
        invalidate()
    }

    fun setSpacing(spacing: Int) {
        childrenSequence()
          .forEach { it.setPadding(spacing, spacing, spacing, spacing) }

        keySpacing = spacing.toFloat()
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount != keys.size) {
            return super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

        var width = (paddingLeft + paddingRight).toFloat()
        width += (keySpacing * (NUM_COLS + 1))
        var height = (paddingTop + paddingBottom).toFloat()
        height += (keySpacing * (NUM_ROWS + 1))

        val specWidth = MeasureSpec.getSize(widthMeasureSpec)
        val calculatedChildWidth = (specWidth - width) / NUM_COLS

        val specHeight = MeasureSpec.getSize(heightMeasureSpec)
        val calculatedChildHeight = (specHeight - height) / NUM_ROWS

        val aChildView = getChildAt(1)
        aChildView.measure(
          MeasureSpec.makeMeasureSpec(calculatedChildWidth.toInt(), MeasureSpec.getMode(widthMeasureSpec)),
          MeasureSpec.makeMeasureSpec(calculatedChildHeight.toInt(), MeasureSpec.getMode(heightMeasureSpec))
        )

        width += (aChildView.measuredWidth * NUM_COLS)
        height += (aChildView.measuredHeight * NUM_ROWS)

        setMeasuredDimension(
          resolveSizeAndState(width.toInt(), widthMeasureSpec, MeasureSpec.getMode(widthMeasureSpec)),
          resolveSizeAndState(height.toInt(), heightMeasureSpec, MeasureSpec.getMode(heightMeasureSpec))
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val width = right - left
        val height = bottom - top
        val childWidth = (width - paddingLeft - paddingRight - (keySpacing * (NUM_COLS + 1))) / NUM_COLS
        val childHeight = (height - paddingTop - paddingBottom - (keySpacing * (NUM_ROWS + 1))) / NUM_ROWS

        for (index in 0..childCount - 1) {
            val column = index % NUM_COLS
            val row = index / NUM_COLS
            val child = getChildAt(index)

            val cl = paddingLeft + (keySpacing * (column + 1)) + (childWidth * column)
            val ct = paddingTop + (keySpacing * (row + 1)) + (childHeight * row)
            val cr = cl + childWidth
            val cb = ct + childHeight

            child.layout(cl.toInt(), ct.toInt(), cr.toInt(), cb.toInt())
        }
    }

    @SuppressLint("ViewConstructor")
    private class KeyView(context: Context, attrs: TypedArray, val key: Key = Key.NULL) : AppCompatImageButton(context) {

        private val charPaint: Paint
        private val subTextPaint: Paint

        init {
            val flags = Paint.ANTI_ALIAS_FLAG or
              Paint.DITHER_FLAG or Paint.LINEAR_TEXT_FLAG

            charPaint = Paint(flags).apply {
                textAlign = Paint.Align.CENTER
                textSize = attrs.getDimension(R.styleable.PinpadView_textSize, 0F)
                color = attrs.getColor(R.styleable.PinpadView_textColor, 0)
            }
            subTextPaint = Paint(flags).apply {
                textAlign = Paint.Align.CENTER
                textSize = attrs.getDimension(R.styleable.PinpadView_subTextSize, 0F)
                color = attrs.getColor(R.styleable.PinpadView_subTextColor, 0)
            }

            val fontPath = attrs.getString(R.styleable.PinpadView_fontSource)
            if (fontPath != null && fontPath.isNotBlank()) {
                setTypeface(TypefaceUtils.load(context.assets, fontPath))
            }
        }

        fun setTypeface(typeface: Typeface) {
            charPaint.typeface = typeface
            subTextPaint.typeface = typeface
        }

        fun setTextSize(size: Float) {
            charPaint.textSize = size
        }

        fun setSubTextSize(size: Float) {
            subTextPaint.textSize = size
        }

        fun setTextColor(color: Int) {
            charPaint.color = color
        }

        fun setSubTextColor(color: Int) {
            subTextPaint.color = color
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            var width = (paddingLeft + paddingRight).toFloat()
            val charSize = charPaint.measureText(key.char.toString())
            val subTextSize = subTextPaint.measureText(key.subText)
            width += Math.max(charSize, subTextSize)

            var height = (paddingTop + paddingBottom).toFloat()
            height += charPaint.textHeight
            if (key.subText.isNotBlank()) {
                height += subTextPaint.textHeight
            }

            setMeasuredDimension(
              resolveSizeAndState(width.toInt(), widthMeasureSpec, 0),
              resolveSizeAndState(height.toInt(), heightMeasureSpec, 0)
            )
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            canvas.drawText(key.char.toString(),
              width / 2F, paddingTop - charPaint.ascent(), charPaint)

            if (key.subText.isNotBlank()) {
                canvas.drawText(key.subText, width / 2F,
                  height - paddingBottom - subTextPaint.descent(), subTextPaint)
            }
        }
    }

    private class Key(val char: Char, val subText: String) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Key) return false

            if (char != other.char) return false

            return true
        }

        override fun hashCode(): Int {
            return char.hashCode()
        }

        companion object {
            @JvmField val NULL = Key('\u0000', "")
        }
    }

    interface ViewProvider {
        fun setNumDigits(numDigits: Int)
        fun onAppendChar(char: Char)
        fun onDeleteChar()
        fun onReset()
    }

    interface Callback {
        fun onPasscodeComplete(passcode: String)
        fun onHelpRequest()
    }

    companion object {
        const private val KEY_DEL = '\u232B'
        const private val KEY_HELP = '\u2753'
        const private val NUM_COLS = 3
        const private val NUM_ROWS = 4
        const private val DURATION_ANIMATION = 300L
    }
}
