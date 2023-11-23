package com.hinnka.tsbrowser.ext

import android.hardware.SensorEvent
import kotlin.math.atan2

object Orientation{
    const val ReversePortrait = 0
    const val Landscape = 1
    const val Portrait = 2
    const val ReverseLandscape = 3
}
fun orientation(rotation: Int) : Int{
    return when(rotation){
        in (46..135) -> Orientation.ReverseLandscape
        in (136..225) -> Orientation.ReversePortrait
        in (226..315) -> Orientation.Landscape
        else -> Orientation.Portrait
    }
}
fun SensorEvent.orientation() : Int{
    var rotation = -1L
    val x = -values[0]
    val y = -values[1]
    val z = -values[2]
    val magnitude = x * x + y * y
    // Don't trust the angle if the magnitude is small compared to the y value
    if (magnitude * 4 >= z * z) {
        // 屏幕旋转时
        rotation = 90 - Math.round(atan2(-y.toDouble(), x.toDouble()) * 57.29578f)
        // normalize to 0 - 359 range
        while (rotation >= 360) {
            rotation -= 360
        }
        while (rotation < 0) {
            rotation += 360
        }
    }

    // 根据手机屏幕的朝向角度，来设置内容的横竖屏，并且记录状态
    // if (orientation in 46..135) {
    //     return Orientation.ReverseLandscape
    // } else if (orientation in 136..225) {
    //     return Orientation.ReversePortrait
    // } else if (orientation in 226..315) {
    //     return Orientation.Landscape
    // } else if (orientation in 316..360 || orientation in 1..45) {
    //     return Orientation.Portrait
    // }
    return when(rotation){
        in (46..135) -> Orientation.ReverseLandscape
        in (136..225) -> Orientation.ReversePortrait
        in (226..315) -> Orientation.Landscape
        else -> Orientation.Portrait
    }
}