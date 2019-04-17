package com.masoudss.lib

import android.content.Context
import android.util.TypedValue
import java.util.*

object Utils {

    fun dp(context: Context?, dp: Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context!!.resources.displayMetrics)
    }


    fun getDummyWaveSample() : ShortArray{
        val data = ShortArray(50)
        for (i in 0 until data.size)
            data[i] = Random().nextInt(data.size).toShort()

        return data
    }

}