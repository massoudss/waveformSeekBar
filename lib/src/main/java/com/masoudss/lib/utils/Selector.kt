package com.masoudss.lib.utils

import android.util.Log
import kotlin.math.abs

class Selector() {
    var mills: Long = 0
    var position: Float = 0f
    private val distanceThreshold = 100

    constructor(mills: Long) : this() {
        this.mills = mills
    }

    constructor(position: Float) : this() {
        this.position = position
    }

    fun isTouching(otherPosition: Float): Boolean{
        val distance = abs(position - otherPosition)
        Log.e("DEB","Dist: $distance")
        return distance < distanceThreshold
    }
}