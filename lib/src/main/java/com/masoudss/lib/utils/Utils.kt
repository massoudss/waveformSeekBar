package com.masoudss.lib.utils

import android.content.Context
import android.util.TypedValue

object Utils {

    fun dp(context: Context?, dp: Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context!!.resources.displayMetrics)
    }

}