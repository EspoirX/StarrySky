package com.lzx.musiclib.card

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.lzx.musiclib.R
import kotlinx.android.synthetic.main.activity_card.tabLayout
import kotlinx.android.synthetic.main.activity_card.viewpager

class CardActivity : AppCompatActivity() {

    private var categoryList = mutableListOf<CardCategory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        categoryList.add(CardCategory("card1", "推荐"))
        categoryList.add(CardCategory("card2", "流行电音"))
        categoryList.add(CardCategory("card3", "情感陪伴"))
        categoryList.add(CardCategory("card4", "动感地带"))
        categoryList.add(CardCategory("card5", "劲歌金曲"))

        val adapter = CardCategoryAdapter(supportFragmentManager, categoryList)
        viewpager.removeAllViews()
        viewpager.removeAllViewsInLayout()
        viewpager.adapter = adapter
        tabLayout.setViewPager(viewpager)
    }

}

class CardCategoryAdapter(
    fm: FragmentManager?,
    private var categoryList: MutableList<CardCategory>
) : FragmentStatePagerAdapter(fm!!) {

    private val fragmentMap = hashMapOf<String, Fragment>()

    override fun getItem(position: Int): Fragment {
        val category = categoryList[position]
        if (fragmentMap[category.cardType] != null) {
            return fragmentMap[category.cardType]!!
        }
        val fragment = CardFragment.newInstance(category.cardType, category.cardTitle)
        fragmentMap[category.cardType] = fragment
        return fragment
    }

    override fun getCount(): Int = categoryList.size

    override fun getPageTitle(position: Int): CharSequence? {
        return categoryList[position].cardTitle
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        val category = categoryList[position]
        if (fragmentMap[category.cardType] != null) {
            fragmentMap.remove(category.cardType)
        }
    }

    var currFragment: CardFragment? = null

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        if (obj is CardFragment) {
            currFragment = obj
        }
        super.setPrimaryItem(container, position, obj)
    }
}


data class CardCategory(var cardType: String, var cardTitle: String)