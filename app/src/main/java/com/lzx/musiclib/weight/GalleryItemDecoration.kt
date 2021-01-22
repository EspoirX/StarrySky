package com.lzx.musiclib.weight

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.lzx.musiclib.dp
import com.lzx.starrysky.utils.orDef


class GalleryItemDecoration() : RecyclerView.ItemDecoration() {


    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount.orDef()

        val isLastPosition = position == itemCount - 1

        val itemHeight: Int = parent.height - 97.dp

        val lp = view.layoutParams as RecyclerView.LayoutParams
        lp.height = itemHeight

        var bottomMargin = 15.dp
        if (isLastPosition) {
            bottomMargin = 97.dp
        }
        lp.setMargins(15.dp, lp.topMargin, 15.dp, bottomMargin)
        view.layoutParams = lp
        super.getItemOffsets(outRect, view, parent, state)
    }
}