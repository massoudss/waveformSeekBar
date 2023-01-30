package com.masoudss.lib.utils
class Selector() {
    var mills: Long = 0
    val distanceThreshold = 50

    constructor(mills: Long) : this() {
        this.mills = mills
    }
}