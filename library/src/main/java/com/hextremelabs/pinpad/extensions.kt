package com.hextremelabs.pinpad

import android.content.Context
import android.graphics.Paint

/**
 * @author ADIO Kingsley O.
 * @since 25 Apr, 2017
 */


internal val Paint.textHeight get() = descent() - ascent()
