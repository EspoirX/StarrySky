package com.lzx.musiclib.weight

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.lzx.musiclib.R
import com.lzx.musiclib.dp
import com.lzx.musiclib.parseColor
import com.lzx.musiclib.sp
import java.util.*

/**
 *
 */
/**
 *
 * Project: BuDaoShortVideo
 *
 * Description: SlidingTabLayout 滑动TabLayout,对于ViewPager的依赖性强, https://github.com/H07000223/FlycoTabLayout，
 * commit 749ef0632daa3fbcfd9dab1c77e8082167554685，在此基础上修改，不局限于添加红点，还可添加其他的msg View
 *
 * Copyright (c) 2017 www.duowan.com Inc. All rights reserved.
 *
 * Company: YY Inc.
 *
 * @author: Aragon.Wu
 * @date: 2017-06-06
 * @vserion: 1.0
 * 注：根据SlidingTabLayout 和 CommonTabLayout 改造，可配置简单icon
 */
class SlidingTabLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : HorizontalScrollView(context, attrs, defStyleAttr), OnPageChangeListener {
    private val mContext: Context
    private var mViewPager: ViewPager? = null
    private val mTabEntitys = mutableListOf<CustomTabEntity>()
    private val mTabsContainer: LinearLayout
    private var mCurrentTab = 0
    private var mCurrentPositionOffset = 0f
    var tabCount = 0
        private set

    /**
     * 用于绘制显示器
     */
    private val mIndicatorRect = Rect()

    /**
     * 用于实现滚动居中
     */
    private val mTabRect = Rect()
    private val mIndicatorDrawable = GradientDrawable()
    private val mRectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mDividerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mTrianglePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mTrianglePath = Path()
    private var mIndicatorStyle = STYLE_NORMAL
    private var mTabPadding = 0f
    private var mTabSpaceEqual = false
    private var mTabWidth = 0f
    private var mTabGravity = 0

    /**
     * indicator
     */
    private var mIndicatorColor = 0
    private var mIndicatorHeight = 0f
    private var mIndicatorWidth = 0f
    private var mIndicatorCornerRadius = 0f
    var indicatorMarginLeft = 0f
        private set
    var indicatorMarginTop = 0f
        private set
    var indicatorMarginRight = 0f
        private set
    var indicatorMarginBottom = 0f
        private set
    private var mIndicatorGravity = 0
    private var mIndicatorWidthEqualTitle = false

    /**
     * underline
     */
    private var mUnderlineColor = 0
    private var mUnderlineHeight = 0f
    private var mUnderlineGravity = 0

    /**
     * divider
     */
    private var mDividerColor = 0
    private var mDividerWidth = 0f
    private var mDividerPadding = 0f
    private var mTextsize = 0f
    private var mTextSelectSize = 0f
    private var mTextSelectColor = 0
    private var mTextUnselectColor = 0
    private var mTextBold = 0
    private var mTextSelectedBg: Drawable? = null
    private var mTextNormalBg: Drawable? = null
    private var mTextPaddingTop = 0
    private var mTextPaddingBottom = 0
    private var mTextPaddingLeft = 0
    private var mTextPaddingRight = 0
    private var mTextAllCaps = false
    private var mNumColorSameAsText // 数字的颜色是否跟随tab文本颜色,true:是
            = false
    private var mNumAlignTab //0:None ,1:top,2:bottom
            = 0
    private var mNumMarginLeft = 0f
    private var mNumMarginBottom = 0f

    /**
     * icon
     */
    private var mIconVisible = false
    private var mIconWidth = 0f
    private var mIconHeight = 0f
    private var mIconMargin = 0f
    private var mLastScrollX = 0
    private var mHeight = 0
    private var mSnapOnTabClick = false
    private fun obtainAttributes(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.SlidingTabLayout)
        mIndicatorStyle = ta.getInt(R.styleable.SlidingTabLayout_tl_indicator_style, STYLE_NORMAL)
        val isBlock = mIndicatorStyle == STYLE_BLOCK
        val isTriangle = mIndicatorStyle == STYLE_TRIANGLE
        mIndicatorColor = ta.getColor(R.styleable.SlidingTabLayout_tl_indicator_color, if (isBlock) "#4B6A87".parseColor() else "#fccd40".parseColor())
        mIndicatorHeight = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_height, if (isTriangle) 4f.dp else if (isBlock) (-1f).dp else 2f.dp)
        mIndicatorWidth = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_width, if (isTriangle) 10f.dp else (-1f).dp)
        mIndicatorCornerRadius = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_corner_radius, if (isBlock) (-1).dp else 0.dp)
        indicatorMarginLeft = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_margin_left, 0f)
        indicatorMarginTop = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_margin_top, if (isBlock) 7f.dp else 0f)
        indicatorMarginRight = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_margin_right, 0f)
        indicatorMarginBottom = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_margin_bottom, if (isBlock) 7f.dp else 8.dp)
        mIndicatorGravity = ta.getInt(R.styleable.SlidingTabLayout_tl_indicator_gravity, Gravity.BOTTOM)
        mIndicatorWidthEqualTitle = ta.getBoolean(R.styleable.SlidingTabLayout_tl_indicator_width_equal_title, true)
        mUnderlineColor = ta.getColor(R.styleable.SlidingTabLayout_tl_underline_color, "#ffffff".parseColor())
        mUnderlineHeight = ta.getDimension(R.styleable.SlidingTabLayout_tl_underline_height, 0f)
        mUnderlineGravity = ta.getInt(R.styleable.SlidingTabLayout_tl_underline_gravity, Gravity.BOTTOM)
        mDividerColor = ta.getColor(R.styleable.SlidingTabLayout_tl_divider_color, "#ffffff".parseColor())
        mDividerWidth = ta.getDimension(R.styleable.SlidingTabLayout_tl_divider_width, 0f)
        mDividerPadding = ta.getDimension(R.styleable.SlidingTabLayout_tl_divider_padding, 12f.dp)
        mTextsize = ta.getDimension(R.styleable.SlidingTabLayout_tl_textsize, 14f.sp)
        mTextSelectSize = ta.getDimension(R.styleable.SlidingTabLayout_tl_textSelectsize, mTextsize)
        mTextSelectColor = ta.getColor(R.styleable.SlidingTabLayout_tl_textSelectColor, "#ffae00".parseColor())
        mTextUnselectColor = ta.getColor(R.styleable.SlidingTabLayout_tl_textUnselectColor, "#bdbdbd".parseColor())
        mTextBold = ta.getInt(R.styleable.SlidingTabLayout_tl_textBold, TEXT_BOLD_NONE)
        mTextAllCaps = ta.getBoolean(R.styleable.SlidingTabLayout_tl_textAllCaps, false)
        mTextSelectedBg = ta.getDrawable(R.styleable.SlidingTabLayout_tl_textSelectedBg)
        mTextNormalBg = ta.getDrawable(R.styleable.SlidingTabLayout_tl_textNormalBg)
        mTextPaddingLeft = ta.getDimensionPixelSize(R.styleable.SlidingTabLayout_tl_textPaddingLeft, 0)
        mTextPaddingRight = ta.getDimensionPixelSize(R.styleable.SlidingTabLayout_tl_textPaddingRight, 0)
        mTextPaddingTop = ta.getDimensionPixelSize(R.styleable.SlidingTabLayout_tl_textPaddingTop, 0)
        mTextPaddingBottom = ta.getDimensionPixelSize(R.styleable.SlidingTabLayout_tl_textPaddingBottom, 0)
        mIconVisible = ta.getBoolean(R.styleable.SlidingTabLayout_tl_iconVisible, true)
        mIconWidth = ta.getDimension(R.styleable.SlidingTabLayout_tl_iconWidth, 0f)
        mIconHeight = ta.getDimension(R.styleable.SlidingTabLayout_tl_iconHeight, 0f)
        mIconMargin = ta.getDimension(R.styleable.SlidingTabLayout_tl_iconMargin, 2.5f.dp)
        mTabSpaceEqual = ta.getBoolean(R.styleable.SlidingTabLayout_tl_tab_space_equal, false)
        mTabWidth = ta.getDimension(R.styleable.SlidingTabLayout_tl_tab_width, (-1f).dp)
        mTabPadding = ta.getDimension(R.styleable.SlidingTabLayout_tl_tab_padding, if (mTabSpaceEqual || mTabWidth > 0) 0f else 20f.dp)
        mTabGravity = ta.getInt(R.styleable.SlidingTabLayout_tl_tab_gravity, Gravity.START)
        mNumColorSameAsText = ta.getBoolean(R.styleable.SlidingTabLayout_tl_numColorSameAsText, false)
        mNumAlignTab = ta.getInt(R.styleable.SlidingTabLayout_tl_numAlignTab, 0)
        mNumMarginLeft = ta.getDimension(R.styleable.SlidingTabLayout_tl_numMarginLeft, 0f)
        mNumMarginBottom = ta.getDimension(R.styleable.SlidingTabLayout_tl_numMarginBottom, 0f)
        ta.recycle()
    }

    /**
     * 关联ViewPager
     */
    fun setViewPager(vp: ViewPager?) {
        check(!(vp == null || vp.adapter == null)) { "ViewPager or ViewPager adapter can not be NULL !" }
        mCurrentTab = 0
        mViewPager = vp
        mViewPager?.removeOnPageChangeListener(this)
        mViewPager?.addOnPageChangeListener(this)
        notifyDataSetChanged()
    }

    /**
     * 关联ViewPager,用于不想在ViewPager适配器中设置titles数据的情况
     */
    fun setViewPager(vp: ViewPager, tabEntitys: ArrayList<CustomTabEntity>) {
        checkNotNull(vp.adapter) { "ViewPager or ViewPager adapter can not be NULL !" }
        check(tabEntitys.size == vp.adapter?.count) { "Titles length must be the same as the page count !" }
        mViewPager = vp
        mTabEntitys.clear()
        mTabEntitys.addAll(tabEntitys)
        mViewPager?.removeOnPageChangeListener(this)
        mViewPager?.addOnPageChangeListener(this)
        notifyDataSetChanged()
    }

    /**
     * 更新数据
     */
    fun notifyDataSetChanged() {
        mTabsContainer.removeAllViews()
        tabCount = if (mTabEntitys.isEmpty()) mViewPager?.adapter?.count ?: 0 else mTabEntitys.size
        var tabView: View
        for (i in 0 until tabCount) {
            tabView = inflate(mContext, R.layout.slide_layout_tab, null)
            tabView.tag = i
            val msgView: MsgView = tabView.findViewById(R.id.rtv_msg_tip)
            if (mNumAlignTab > 0) {
                val rlp = msgView.layoutParams as RelativeLayout.LayoutParams
                if (mNumAlignTab == 1) {
                    rlp.addRule(RelativeLayout.ALIGN_TOP, R.id.tv_tab_title)
                } else if (mNumAlignTab == 2) {
                    rlp.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.tv_tab_title)
                }
                rlp.leftMargin = mNumMarginLeft.toInt()
                msgView.layoutParams = rlp
            }
            addTab(i, tabView)
        }
        updateTabStyles()
    }

    /**
     * 创建并添加tab
     */
    private fun addTab(position: Int, tabView: View) {
        val tvTabTitle = tabView.findViewById<TextView>(R.id.tv_tab_title)
        if (!mTabEntitys.isNullOrEmpty()) {
            tvTabTitle.text = mTabEntitys[position].tabTitle
        } else {
            tvTabTitle.text = mViewPager?.adapter?.getPageTitle(position)
        }
        val ivTabIcon = tabView.findViewById<ImageView>(R.id.iv_tab_icon)
        if (mIconVisible) {
            if (!mTabEntitys.isNullOrEmpty()) {
                ivTabIcon.visibility = VISIBLE
                ivTabIcon.setImageResource(mTabEntitys[position].tabUnselectedIcon)
            } else {
                ivTabIcon.visibility = GONE
            }
        } else {
            ivTabIcon.visibility = GONE
        }
        tabView.setBackgroundColor(Color.TRANSPARENT)
        tabView.setOnClickListener { v: View? ->
            val position1 = mTabsContainer.indexOfChild(v)
            if (position1 != -1) {
                if (mViewPager?.currentItem != position1) {
                    mViewPager?.setCurrentItem(position1, !mSnapOnTabClick)
                    mListener?.onTabSelect(position1)
                } else {
                    mListener?.onTabReselect(position1)
                }
            }
        }
        /** 每一个Tab的布局参数  */
        var lpTab = if (mTabSpaceEqual) LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f) else LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        if (mTabWidth > 0) {
            lpTab = LinearLayout.LayoutParams(mTabWidth.toInt(), LayoutParams.MATCH_PARENT)
        }
        mTabsContainer.addView(tabView, position, lpTab)
    }

    private fun updateTabStyles() {
        mTabsContainer.gravity = mTabGravity
        for (i in 0 until tabCount) {
            val v = mTabsContainer.getChildAt(i)
            val relativeLayout = v.findViewById<RelativeLayout>(R.id.ll_tap)
            relativeLayout.setPadding(mTabPadding.toInt(), 0, mTabPadding.toInt(), 0)
            val tvTabTitle = v.findViewById<TextView>(R.id.tv_tab_title)
            if (tvTabTitle != null) {
                tvTabTitle.setTextColor(if (i == mCurrentTab) mTextSelectColor else mTextUnselectColor)
                tvTabTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, if (i == mCurrentTab) mTextSelectSize else mTextsize)
                if (mTextAllCaps) {
                    tvTabTitle.text = tvTabTitle.text.toString().toUpperCase()
                }
                if (i == mCurrentTab) {
                    if (mTextSelectedBg != null) {
                        tvTabTitle.background = mTextSelectedBg
                    }
                } else {
                    if (mTextNormalBg != null) {
                        tvTabTitle.background = mTextNormalBg
                    }
                }
                tvTabTitle.setPadding(mTextPaddingLeft, mTextPaddingTop, mTextPaddingRight, mTextPaddingBottom)
                if (mTextBold == TEXT_BOLD_BOTH) {
                    tvTabTitle.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                } else if (mTextBold == TEXT_BOLD_NONE) {
                    tvTabTitle.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                } else if (mTextBold == TEXT_BOLD_WHEN_SELECT) {
                    if (i == mCurrentTab) {
                        tvTabTitle.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    }
                }
            }
            val msgView: MsgView = v.findViewById(R.id.rtv_msg_tip)
            if (mNumColorSameAsText) {
                msgView.setTextColor(if (i == mCurrentTab) mTextSelectColor else mTextUnselectColor)
            }
            val rlp = msgView.layoutParams as RelativeLayout.LayoutParams
            rlp.bottomMargin = if (i == mCurrentTab) mNumMarginBottom.toInt() * 3 else mNumMarginBottom.toInt()
            msgView.layoutParams = rlp
            val ivTabIcon = v.findViewById<ImageView>(R.id.iv_tab_icon)
            if (mIconVisible) {
                if (!mTabEntitys.isNullOrEmpty()) {
                    ivTabIcon.visibility = VISIBLE
                    val tabEntity = mTabEntitys[i]
                    ivTabIcon.setImageResource(if (i == mCurrentTab) tabEntity.tabSelectedIcon else tabEntity.tabUnselectedIcon)
                    val lp = RelativeLayout.LayoutParams(
                            if (mIconWidth <= 0) RelativeLayout.LayoutParams.WRAP_CONTENT else mIconWidth.toInt(),
                            if (mIconHeight <= 0) RelativeLayout.LayoutParams.WRAP_CONTENT else mIconHeight.toInt())
                    lp.rightMargin = mIconMargin.toInt()
                    lp.addRule(Gravity.CENTER_VERTICAL)
                    ivTabIcon.layoutParams = lp
                } else {
                    ivTabIcon.visibility = GONE
                }
            } else {
                ivTabIcon.visibility = GONE
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        /**
         * position:当前View的位置
         * mCurrentPositionOffset:当前View的偏移量比例.[0,1)
         */
        mCurrentTab = position
        mCurrentPositionOffset = positionOffset
        scrollToCurrentTab()
        invalidate()
    }

    override fun onPageSelected(position: Int) {
        mCurrentTab = position
        updateTabSelection(position)
    }

    override fun onPageScrollStateChanged(state: Int) {}

    /**
     * HorizontalScrollView滚到当前tab,并且居中显示
     */
    private fun scrollToCurrentTab() {
        if (tabCount <= 0) {
            return
        }
        val childView = mTabsContainer.getChildAt(mCurrentTab) ?: return
        val offset = (mCurrentPositionOffset * childView.width).toInt()

        /**当前Tab的left+当前Tab的Width乘以positionOffset */
        var newScrollX = childView.left + offset
        if (mCurrentTab > 0 || offset > 0) {
            /**HorizontalScrollView移动到当前tab,并居中 */
            newScrollX -= width / 2 - paddingLeft
            calcIndicatorRect()
            newScrollX += (mTabRect.right - mTabRect.left) / 2
        }
        if (newScrollX != mLastScrollX) {
            mLastScrollX = newScrollX
            /** scrollTo（int x,int y）:x,y代表的不是坐标点,而是偏移量
             * x:表示离起始位置的x水平方向的偏移量
             * y:表示离起始位置的y垂直方向的偏移量
             */
            scrollTo(newScrollX, 0)
            //            smoothScrollTo(newScrollX, 0);
        }
    }

    private fun updateTabSelection(position: Int) {
        for (i in 0 until tabCount) {
            val tabView = mTabsContainer.getChildAt(i) ?: continue
            val isSelect = i == position
            val tabTitle = tabView.findViewById<TextView>(R.id.tv_tab_title)
            if (tabTitle != null) {
                tabTitle.setTextColor(if (isSelect) mTextSelectColor else mTextUnselectColor)
                tabTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, if (isSelect) mTextSelectSize else mTextsize)
                if (mTextBold == TEXT_BOLD_WHEN_SELECT) {
                    if (isSelect) {
                        tabTitle.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    } else {
                        tabTitle.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    }
                }
                if (isSelect) {
                    if (mTextSelectedBg != null) {
                        tabTitle.background = mTextSelectedBg
                    }
                } else {
                    if (mTextNormalBg != null) {
                        tabTitle.background = mTextNormalBg
                    }
                }
                tabTitle.setPadding(mTextPaddingLeft, mTextPaddingTop, mTextPaddingRight, mTextPaddingBottom)
            }
            val msgView: MsgView = tabView.findViewById(R.id.rtv_msg_tip)
            if (mNumColorSameAsText) {
                msgView.setTextColor(if (i == mCurrentTab) mTextSelectColor else mTextUnselectColor)
            }
            val rlp = msgView.layoutParams as RelativeLayout.LayoutParams
            rlp.bottomMargin = if (i == mCurrentTab) mNumMarginBottom.toInt() * 3 else mNumMarginBottom.toInt()
            msgView.layoutParams = rlp
            val ivTabIcon = tabView.findViewById<ImageView>(R.id.iv_tab_icon)
            if (mIconVisible) {
                if (!mTabEntitys.isNullOrEmpty()) {
                    ivTabIcon.visibility = VISIBLE
                    val tabEntity = mTabEntitys[i]
                    ivTabIcon.setImageResource(if (isSelect) tabEntity.tabSelectedIcon else tabEntity.tabUnselectedIcon)
                } else {
                    ivTabIcon.visibility = GONE
                }
            } else {
                ivTabIcon.visibility = GONE
            }
        }
    }

    private var margin = 0f
    private fun calcIndicatorRect() {
        val currentTabView = mTabsContainer.getChildAt(mCurrentTab) ?: return
        var left = currentTabView.left.toFloat()
        var right = currentTabView.right.toFloat()

        if (mIndicatorStyle == STYLE_NORMAL && mIndicatorWidthEqualTitle) {
            val tabTitle = currentTabView.findViewById<View>(R.id.tv_tab_title) as TextView
            mTextPaint.textSize = mTextsize
            var ivTabIconWidth = 0f
            if (mIconVisible) {
                val ivTabIcon = currentTabView.findViewById<ImageView>(R.id.iv_tab_icon)
                ivTabIconWidth = ivTabIcon.width.toFloat()
            }
            val textWidth = mTextPaint.measureText(tabTitle.text.toString())
            margin = (right - left - textWidth - ivTabIconWidth) / 2
        }
        if (mCurrentTab < tabCount - 1) {
            val nextTabView = mTabsContainer.getChildAt(mCurrentTab + 1)
            val nextTabLeft = nextTabView.left.toFloat()
            val nextTabRight = nextTabView.right.toFloat()
            left += mCurrentPositionOffset * (nextTabLeft - left)
            right += mCurrentPositionOffset * (nextTabRight - right)

            //for mIndicatorWidthEqualTitle
            if (mIndicatorStyle == STYLE_NORMAL && mIndicatorWidthEqualTitle) {
                val nextTabTitle = nextTabView.findViewById<View>(R.id.tv_tab_title) as TextView
                mTextPaint.textSize = mTextsize
                var ivTabIconWidth = 0f
                if (mIconVisible) {
                    val ivTabIcon = nextTabView.findViewById<ImageView>(R.id.iv_tab_icon)
                    ivTabIconWidth = ivTabIcon.width.toFloat()
                }
                val nextTextWidth = mTextPaint.measureText(nextTabTitle.text.toString())
                val nextMargin = (nextTabRight - nextTabLeft - nextTextWidth - ivTabIconWidth) / 2
                margin += mCurrentPositionOffset * (nextMargin - margin)
            }
        }
        mIndicatorRect.left = left.toInt()
        mIndicatorRect.right = right.toInt()
        //for mIndicatorWidthEqualTitle
        if (mIndicatorStyle == STYLE_NORMAL && mIndicatorWidthEqualTitle) {
            mIndicatorRect.left = (left + margin - 1).toInt()
            mIndicatorRect.right = (right - margin - 1).toInt()
        }
        mTabRect.left = left.toInt()
        mTabRect.right = right.toInt()
        if (mIndicatorWidth < 0) {   //indicatorWidth小于0时,原jpardogo's PagerSlidingTabStrip
        } else { //indicatorWidth大于0时,圆角矩形以及三角形
            var indicatorLeft = currentTabView.left + (currentTabView.width - mIndicatorWidth) / 2
            if (mCurrentTab < tabCount - 1) {
                val nextTab = mTabsContainer.getChildAt(mCurrentTab + 1)
                indicatorLeft += mCurrentPositionOffset * (currentTabView.width / 2 + nextTab.width / 2)
            }
            mIndicatorRect.left = indicatorLeft.toInt()
            mIndicatorRect.right = (mIndicatorRect.left + mIndicatorWidth).toInt()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isInEditMode || tabCount <= 0) {
            return
        }
        val height = height
        val paddingLeft = paddingLeft
        // draw divider
        if (mDividerWidth > 0) {
            mDividerPaint.strokeWidth = mDividerWidth
            mDividerPaint.color = mDividerColor
            for (i in 0 until tabCount - 1) {
                val tab = mTabsContainer.getChildAt(i)
                canvas.drawLine(paddingLeft + tab.right.toFloat(), mDividerPadding, paddingLeft +
                        tab.right.toFloat(), height - mDividerPadding, mDividerPaint)
            }
        }

        // draw underline
        if (mUnderlineHeight > 0) {
            mRectPaint.color = mUnderlineColor
            if (mUnderlineGravity == Gravity.BOTTOM) {
                canvas.drawRect(paddingLeft.toFloat(), height - mUnderlineHeight, mTabsContainer.width +
                        paddingLeft.toFloat(), height.toFloat(), mRectPaint)
            } else {
                canvas.drawRect(paddingLeft.toFloat(), 0f, mTabsContainer.width + paddingLeft.toFloat(), mUnderlineHeight,
                        mRectPaint)
            }
        }

        //draw indicator line
        calcIndicatorRect()
        if (mIndicatorStyle == STYLE_TRIANGLE) {
            if (mIndicatorHeight > 0) {
                mTrianglePaint.color = mIndicatorColor
                mTrianglePath.reset()
                mTrianglePath.moveTo(paddingLeft + mIndicatorRect.left.toFloat(), height.toFloat())
                mTrianglePath.lineTo(paddingLeft + mIndicatorRect.left / 2 + (mIndicatorRect.right / 2).toFloat(),
                        height - mIndicatorHeight)
                mTrianglePath.lineTo(paddingLeft + mIndicatorRect.right.toFloat(), height.toFloat())
                mTrianglePath.close()
                canvas.drawPath(mTrianglePath, mTrianglePaint)
            }
        } else if (mIndicatorStyle == STYLE_BLOCK) {
            if (mIndicatorHeight < 0) {
                mIndicatorHeight = height - indicatorMarginTop - indicatorMarginBottom
            }
            if (mIndicatorHeight > 0) {
                if (mIndicatorCornerRadius < 0 || mIndicatorCornerRadius > mIndicatorHeight / 2) {
                    mIndicatorCornerRadius = mIndicatorHeight / 2
                }
                mIndicatorDrawable.setColor(mIndicatorColor)
                mIndicatorDrawable.setBounds(paddingLeft + indicatorMarginLeft.toInt() + mIndicatorRect.left,
                        indicatorMarginTop.toInt(), (paddingLeft + mIndicatorRect.right - indicatorMarginRight).toInt(),
                        (indicatorMarginTop + mIndicatorHeight).toInt())
                mIndicatorDrawable.cornerRadius = mIndicatorCornerRadius
                mIndicatorDrawable.draw(canvas)
            }
        } else {
            if (mIndicatorHeight > 0) {
                mIndicatorDrawable.setColor(mIndicatorColor)
                if (mIndicatorGravity == Gravity.BOTTOM) {
                    mIndicatorDrawable.setBounds(paddingLeft + indicatorMarginLeft.toInt() + mIndicatorRect.left,
                            height - mIndicatorHeight.toInt() - indicatorMarginBottom.toInt(),
                            paddingLeft + mIndicatorRect.right - indicatorMarginRight.toInt(),
                            height - indicatorMarginBottom.toInt())
                } else {
                    mIndicatorDrawable.setBounds(paddingLeft + indicatorMarginLeft.toInt() + mIndicatorRect.left,
                            indicatorMarginTop.toInt(),
                            paddingLeft + mIndicatorRect.right - indicatorMarginRight.toInt(),
                            mIndicatorHeight.toInt() + indicatorMarginTop.toInt())
                }
                mIndicatorDrawable.cornerRadius = mIndicatorCornerRadius
                mIndicatorDrawable.draw(canvas)
            }
        }
    }

    fun setCurrentTabOnly(currentTab: Int) {
        mCurrentTab = currentTab
        updateTabStyles()
    }

    fun setCurrentTab(currentTab: Int, smoothScroll: Boolean) {
        mCurrentTab = currentTab
        mViewPager?.setCurrentItem(currentTab, smoothScroll)
    }

    fun setTabGravity(gravity: Int) {
        mTabGravity = gravity
        updateTabStyles()
    }

    fun setIndicatorGravity(indicatorGravity: Int) {
        mIndicatorGravity = indicatorGravity
        invalidate()
    }

    fun setIndicatorMargin(indicatorMarginLeft: Float, indicatorMarginTop: Float,
                           indicatorMarginRight: Float, indicatorMarginBottom: Float) {
        this.indicatorMarginLeft = dp2px(indicatorMarginLeft).toFloat()
        this.indicatorMarginTop = dp2px(indicatorMarginTop).toFloat()
        this.indicatorMarginRight = dp2px(indicatorMarginRight).toFloat()
        this.indicatorMarginBottom = dp2px(indicatorMarginBottom).toFloat()
        invalidate()
    }

    fun setIndicatorWidthEqualTitle(indicatorWidthEqualTitle: Boolean) {
        mIndicatorWidthEqualTitle = indicatorWidthEqualTitle
        invalidate()
    }

    fun setUnderlineGravity(underlineGravity: Int) {
        mUnderlineGravity = underlineGravity
        invalidate()
    }

    fun setSnapOnTabClick(snapOnTabClick: Boolean) {
        mSnapOnTabClick = snapOnTabClick
    }

    //setter and getter
    var currentTab: Int
        get() = mCurrentTab
        set(currentTab) {
            mCurrentTab = currentTab
            mViewPager?.currentItem = currentTab
        }
    var indicatorStyle: Int
        get() = mIndicatorStyle
        set(indicatorStyle) {
            mIndicatorStyle = indicatorStyle
            invalidate()
        }
    var tabPadding: Float
        get() = mTabPadding
        set(tabPadding) {
            mTabPadding = dp2px(tabPadding).toFloat()
            updateTabStyles()
        }
    var isTabSpaceEqual: Boolean
        get() = mTabSpaceEqual
        set(tabSpaceEqual) {
            mTabSpaceEqual = tabSpaceEqual
            updateTabStyles()
        }
    var tabWidth: Float
        get() = mTabWidth
        set(tabWidth) {
            mTabWidth = dp2px(tabWidth).toFloat()
            updateTabStyles()
        }
    var indicatorColor: Int
        get() = mIndicatorColor
        set(indicatorColor) {
            mIndicatorColor = indicatorColor
            invalidate()
        }
    var indicatorHeight: Float
        get() = mIndicatorHeight
        set(indicatorHeight) {
            mIndicatorHeight = dp2px(indicatorHeight).toFloat()
            invalidate()
        }
    var indicatorWidth: Float
        get() = mIndicatorWidth
        set(indicatorWidth) {
            mIndicatorWidth = dp2px(indicatorWidth).toFloat()
            invalidate()
        }
    var indicatorCornerRadius: Float
        get() = mIndicatorCornerRadius
        set(indicatorCornerRadius) {
            mIndicatorCornerRadius = dp2px(indicatorCornerRadius).toFloat()
            invalidate()
        }
    var underlineColor: Int
        get() = mUnderlineColor
        set(underlineColor) {
            mUnderlineColor = underlineColor
            invalidate()
        }
    var underlineHeight: Float
        get() = mUnderlineHeight
        set(underlineHeight) {
            mUnderlineHeight = dp2px(underlineHeight).toFloat()
            invalidate()
        }
    var dividerColor: Int
        get() = mDividerColor
        set(dividerColor) {
            mDividerColor = dividerColor
            invalidate()
        }
    var dividerWidth: Float
        get() = mDividerWidth
        set(dividerWidth) {
            mDividerWidth = dp2px(dividerWidth).toFloat()
            invalidate()
        }
    var dividerPadding: Float
        get() = mDividerPadding
        set(dividerPadding) {
            mDividerPadding = dp2px(dividerPadding).toFloat()
            invalidate()
        }
    var textsize: Float
        get() = mTextsize
        set(textsize) {
            mTextsize = sp2px(textsize).toFloat()
            updateTabStyles()
        }
    var textSelectColor: Int
        get() = mTextSelectColor
        set(textSelectColor) {
            mTextSelectColor = textSelectColor
            updateTabStyles()
        }
    var textUnselectColor: Int
        get() = mTextUnselectColor
        set(textUnselectColor) {
            mTextUnselectColor = textUnselectColor
            updateTabStyles()
        }
    var textBold: Int
        get() = mTextBold
        set(textBold) {
            mTextBold = textBold
            updateTabStyles()
        }
    var isTextAllCaps: Boolean
        get() = mTextAllCaps
        set(textAllCaps) {
            mTextAllCaps = textAllCaps
            updateTabStyles()
        }

    fun getTitleView(tab: Int): TextView {
        val tabView = mTabsContainer.getChildAt(tab)
        return tabView.findViewById<View>(R.id.tv_tab_title) as TextView
    }

    //setter and getter
    // show MsgTipView
    private val mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mInitSetMap = SparseArray<Boolean?>()


    private var mListener: OnTabSelectListener? = null
    fun setOnTabSelectListener(listener: OnTabSelectListener?) {
        mListener = listener
    }

    internal inner class InnerPagerAdapter(fm: FragmentManager?, fragments: ArrayList<Fragment>,
                                           tabEntitys: ArrayList<CustomTabEntity>) : FragmentPagerAdapter(fm!!) {
        private var fragments = ArrayList<Fragment>()
        private val mTabEntitys: ArrayList<CustomTabEntity>
        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mTabEntitys[position].tabTitle
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            // 覆写destroyItem并且空实现,这样每个Fragment中的视图就不会被销毁
            // super.destroyItem(container, position, object);
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }

        init {
            this.fragments = fragments
            this.mTabEntitys = tabEntitys
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("instanceState", super.onSaveInstanceState())
        bundle.putInt("mCurrentTab", mCurrentTab)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        var state: Parcelable? = state
        if (state is Bundle) {
            val bundle = state
            mCurrentTab = bundle.getInt("mCurrentTab")
            state = bundle.getParcelable("instanceState")
            if (mCurrentTab != 0 && mTabsContainer.childCount > 0) {
                updateTabSelection(mCurrentTab)
                scrollToCurrentTab()
            }
        }
        super.onRestoreInstanceState(state)
    }

    protected fun dp2px(dp: Float): Int {
        val scale = mContext.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    protected fun sp2px(sp: Float): Int {
        val scale = mContext.resources.displayMetrics.scaledDensity
        return (sp * scale + 0.5f).toInt()
    }

    companion object {
        private const val STYLE_NORMAL = 0
        private const val STYLE_TRIANGLE = 1
        private const val STYLE_BLOCK = 2

        /**
         * title
         */
        private const val TEXT_BOLD_NONE = 0
        private const val TEXT_BOLD_WHEN_SELECT = 1
        private const val TEXT_BOLD_BOTH = 2
    }

    init {
        isFillViewport = true //设置滚动视图是否可以伸缩其内容以填充视口
        setWillNotDraw(false) //重写onDraw方法,需要调用这个方法来清除flag
        clipChildren = false
        clipToPadding = false
        mContext = context
        mTabsContainer = LinearLayout(context)
        mTabsContainer.setBackgroundColor(Color.TRANSPARENT)
        addView(mTabsContainer, LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT))
        obtainAttributes(context, attrs)

        //get layout_height
        val height = attrs!!.getAttributeValue("http://schemas.android.com/apk/res/android", "layout_height")
        when (height) {
            ViewGroup.LayoutParams.MATCH_PARENT.toString() + "" -> {
            }
            ViewGroup.LayoutParams.WRAP_CONTENT.toString() + "" -> {
            }
            else -> {
                val systemAttrs = intArrayOf(android.R.attr.layout_height)
                val a = context.obtainStyledAttributes(attrs, systemAttrs)
                mHeight = a.getDimensionPixelSize(0, ViewGroup.LayoutParams.WRAP_CONTENT)
                a.recycle()
            }
        }
    }
}