package com.hextremelabs.pinpad

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.text.InputType
import android.util.AttributeSet
import android.view.Gravity


/**
 * @author ADIO Kingsley O.
 * @since 17 Apr, 2017
 */

class PinTextView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr), PinpadView.ViewProvider {

    init {
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        gravity = Gravity.CENTER
        textSize = 40F
    }

    override fun setNumDigits(numDigits: Int) {
        //no-op
    }

    @SuppressLint("SetTextI18n")
    override fun onAppendChar(char: Char) {
        text = text.toString() + char
    }

    override fun onDeleteChar() {
        text = text.substring(0, text.lastIndex)
    }

    override fun onReset() {
        text = ""
    }
}
