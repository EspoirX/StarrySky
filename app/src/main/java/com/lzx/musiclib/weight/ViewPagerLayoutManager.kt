package com.lzx.musiclib.weight

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
import androidx.recyclerview.widget.RecyclerView.Recycler


/**
 * description  ViewPager页面切换类型LayoutManager，监听了item的进入和退出并回调
 */
class ViewPagerLayoutManager : LinearLayoutManager {
    private var mPagerSnapHelper = SquarePagerSnapHelper()
    private var mOnViewPagerListener: OnViewPagerListener? = null
    private var mRecyclerView: RecyclerView? = null

    /**
     * 位移，用来判断移动方向
     */
    private var mDrift = 0

    constructor(context: Context?) : super(context) {
    }

    constructor(context: Context?, orientation: Int, reverseLayout: Boolean) :
        super(context, orientation, reverseLayout) {
    }


    override fun onAttachedToWindow(view: RecyclerView) {
        super.onAttachedToWindow(view)
        mPagerSnapHelper.attachToRecyclerView(view)
        this.mRecyclerView = view
        mRecyclerView?.addOnChildAttachStateChangeListener(mChildAttachStateChangeListener)
    }

    fun getCurrPosition(): Int {
        val viewIdle = mPagerSnapHelper.findSnapView(this) ?: return 0
        return getPosition(viewIdle)
    }

    /**
     * 滑动状态的改变
     *
     * @param state
     */
    override fun onScrollStateChanged(state: Int) {
        when (state) {
            RecyclerView.SCROLL_STATE_IDLE -> {
                val viewIdle = mPagerSnapHelper.findSnapView(this) ?: return
                val positionIdle = getPosition(viewIdle)
                if (childCount == 1 || childCount == 2) {
                    mOnViewPagerListener?.onPageSelected(positionIdle, positionIdle == itemCount - 1)
                }
            }
            RecyclerView.SCROLL_STATE_DRAGGING, RecyclerView.SCROLL_STATE_SETTLING -> {
                val viewDrag = mPagerSnapHelper.findSnapView(this)
                if (viewDrag != null) {
                    val positionDrag = getPosition(viewDrag)
                }
            }
            else -> {
            }
        }
    }

    /**
     * 监听竖直方向的相对偏移量
     *
     * @param dy
     * @param recycler
     * @param state
     * @return
     */
    override fun scrollVerticallyBy(dy: Int, recycler: Recycler, state: RecyclerView.State): Int {
        mDrift = dy
        return super.scrollVerticallyBy(dy, recycler, state)
    }

    /**
     * 监听水平方向的相对偏移量
     *
     * @param dx
     * @param recycler
     * @param state
     * @return
     */
    override fun scrollHorizontallyBy(dx: Int, recycler: Recycler, state: RecyclerView.State): Int {
        mDrift = dx
        return super.scrollHorizontallyBy(dx, recycler, state)
    }

    /**
     * 设置监听
     *
     * @param listener
     */
    fun setOnViewPagerListener(listener: OnViewPagerListener?) {
        mOnViewPagerListener = listener
    }

    private val mChildAttachStateChangeListener: OnChildAttachStateChangeListener =
        object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                if (childCount == 1) {
                    mOnViewPagerListener?.onInitComplete()
                }
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                if (mDrift >= 0) {
                    mOnViewPagerListener?.onPageRelease(true, getPosition(view))
                } else {
                    mOnViewPagerListener?.onPageRelease(false, getPosition(view))
                }
                mOnViewPagerListener?.onPageRelease(true, getPosition(view))
            }
        }
}

class SquarePagerSnapHelper : PagerSnapHelper() {

    private var verticalHelper: OrientationHelper? = null

    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray? {
        val out = IntArray(2)
        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToStart(targetView, getVerticalHelper(layoutManager))
        } else {
            out[1] = 0
        }
        return out
    }

    private fun distanceToStart(targetView: View, helper: OrientationHelper): Int {
        return helper.getDecoratedStart(targetView) - helper.startAfterPadding
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        return findCenterView(layoutManager, getVerticalHelper(layoutManager))
    }


    private fun findCenterView(layoutManager: RecyclerView.LayoutManager,
                               helper: OrientationHelper): View? {
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return null
        }
        var closestChild: View? = null
        val center = helper.startAfterPadding + helper.totalSpace / 2
        var absClosest = Int.MAX_VALUE
        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i)
            val childCenter = (helper.getDecoratedStart(child) +
                helper.getDecoratedMeasurement(child) / 2)
            val absDistance = Math.abs(childCenter - center)

            if (absDistance < absClosest) {
                absClosest = absDistance
                closestChild = child
            }
        }
        return closestChild
    }

    private fun findStartView(layoutManager: RecyclerView.LayoutManager,
                              helper: OrientationHelper): View? {
        if (layoutManager is LinearLayoutManager) {
            val firstChild = layoutManager.findFirstVisibleItemPosition()
            val lastChild = layoutManager.findLastVisibleItemPosition()
            if (firstChild == RecyclerView.NO_POSITION) {
                return null
            }
            //这行的作用是如果是最后一个，翻到最后一条，解决显示不全的问题
            if (lastChild == layoutManager.getItemCount() - 1) {
                return layoutManager.findViewByPosition(lastChild)
            }
            val child = layoutManager.findViewByPosition(firstChild)
            //获取偏左显示的Item
            return if (helper.getDecoratedEnd(child) >= helper.getDecoratedMeasurement(child) / 2
                && helper.getDecoratedEnd(child) > 0) {
                child
            } else {
                layoutManager.findViewByPosition(firstChild + 1)
            }
        }
        return super.findSnapView(layoutManager)
    }

    private fun getVerticalHelper(
        layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (verticalHelper == null) {
            verticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
        }
        return verticalHelper!!
    }
}

interface OnViewPagerListener {
    /**
     * 初始化完成
     */
    fun onInitComplete()

    /**
     * 释放的监听
     */
    fun onPageRelease(isNext: Boolean, position: Int)

    /**
     * 选中的监听以及判断是否滑动到底部
     */
    fun onPageSelected(position: Int, isBottom: Boolean)
}