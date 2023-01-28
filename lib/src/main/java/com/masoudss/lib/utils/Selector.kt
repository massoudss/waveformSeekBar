package com.masoudss.lib.utils

import kotlin.math.abs

class Selector {
    var mills: Long = 0
    var position: Float = 0.0f
    val distanceThreshold = 80f

    fun isTouching(otherPosition: Float): Boolean{
        var distance = abs(position - otherPosition)
        return distance < distanceThreshold
    }
}