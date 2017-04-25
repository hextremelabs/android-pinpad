package com.hextremelabs.pinpad

import android.content.Context
import android.graphics.Paint

/**
 * @author ADIO Kingsley O.
 * @since 25 Apr, 2017
 */


internal val Paint.textHeight get() = descent() - ascent()

internal inline fun <reified T> Context.systemService(serviceName: String): T {
    return getSystemService(serviceName) as T
}
