package com.hinnka.tsbrowser.persist

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import com.google.gson.Gson
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.BuildConfig
import com.hinnka.tsbrowser.ext.asMutable
import com.tencent.mmkv.MMKV
import java.util.concurrent.TimeUnit

object LabSettings {
    private val pref by lazy { MMKV.mmkvWithID("lab") }

    var blockTimes: Long
        get() = pref.getLong("blockTimes", 0L)
        set(value) {
            pref.edit { putLong("blockTimes", value) }
        }

    val dokitState: State<Boolean> = mutableStateOf(isEnableDokit)
    var isEnableDokit: Boolean
        get() = BuildConfig.DEBUG && pref.getBoolean("enable_dokit", true)
        set(value) {
            pref.putBoolean("enable_dokit" ,value)
        }
}