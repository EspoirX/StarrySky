/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lzx.musiclib.weight.dialog

import android.content.Context
import androidx.annotation.LayoutRes
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.lzx.musiclib.inflate

class DialogLayout(
        context: Context,
        attrs: AttributeSet?
) : FrameLayout(context, attrs) {

    var titleView: TextView? = null
    var messageView: TextView? = null
    var leftButton: TextView? = null
    var rightButton: TextView? = null
    var okBtn: TextView? = null

    var customView: View? = null

    fun addCustomView(
            @LayoutRes res: Int?,
            view: View?
    ): View {
        check(customView == null) { "Custom view already set." }
        if (view != null && view.parent != null) {
            val parent = view.parent as? ViewGroup
            parent?.let { parent.removeView(view) }
        }
        customView = view ?: this.inflate(res!!)
        removeAllViews()
        addView(customView)

        return customView!!
    }

    fun clearView() {
        removeAllViews()
        titleView = null
        messageView = null
        leftButton = null
        rightButton = null
        okBtn = null
        customView = null
    }
}