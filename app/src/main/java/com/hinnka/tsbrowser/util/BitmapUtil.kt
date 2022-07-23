package com.hinnka.tsbrowser.util

import android.graphics.BitmapFactory

fun dataUrlToBitmap(dataUrl: String){
    val imageDataString: ByteArray = Base64Utils.getInstance().decode(dataUrl.substring(dataUrl.indexOf(",") + 1))
    val bitMap = BitmapFactory.decodeByteArray(imageDataString, 0, imageDataString.size)
}