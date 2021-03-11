package com.lzx.starrysky.utils

import android.annotation.SuppressLint

@SuppressLint("StaticFieldLeak")
object StarrySkyConstant : KtPreferences() {

    var KEY_CACHE_SWITCH by booleanPref()
    var KEY_REPEAT_MODE by stringPref()

    //音效相关
    var keyEffectSwitch by booleanPref()
    var keySaveEffectConfig by booleanPref()
    var keyEqualizerSetting by stringPref()
    var keyBassBoostSetting by stringPref()
    var keyVirtualizerSetting by stringPref()
}