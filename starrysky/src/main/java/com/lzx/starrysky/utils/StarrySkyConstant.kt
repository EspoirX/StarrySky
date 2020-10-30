package com.lzx.starrysky.utils

import com.lzx.basecode.KtPreferences

object StarrySkyConstant : KtPreferences() {

    var KEY_CACHE_SWITCH by booleanPref()
    var KEY_REPEAT_MODE by stringPref()

}