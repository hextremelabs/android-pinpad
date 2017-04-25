package com.hextremelabs.pinpad.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.hextremelabs.pinpad.PinTextView
import com.hextremelabs.pinpad.PinpadView

class MainActivity : AppCompatActivity(), PinpadView.Callback {

    internal val pinpad by lazy { findViewById(R.id.pinpad) as PinpadView }
    internal val pinview by lazy { findViewById(R.id.pinview) as PinTextView }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pinpad.viewProvider = pinview
        pinpad.callback = this
    }

    override fun onPasscodeComplete(passcode: String) {
        if (Math.random() > 0.5) {
            Toast.makeText(this, "Login success!", Toast.LENGTH_SHORT).show()
            pinpad.reset()
        } else {
            pinpad.fail()
        }
    }

    override fun onHelpRequest() {
        Toast.makeText(this, "Help requested!", Toast.LENGTH_SHORT).show()
    }
}
