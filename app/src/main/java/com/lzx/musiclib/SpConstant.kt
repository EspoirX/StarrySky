package com.lzx.musiclib

import com.lzx.basecode.KtPreferences

object SpConstant : com.lzx.basecode.KtPreferences() {
    var HAS_PERMISSION by booleanPref()
    var KEY_TOKEN by stringPref()
    var KEY_EXPIRES by stringPref()
}