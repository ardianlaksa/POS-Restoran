package com.dnhsolution.restokabmalang.data

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.dnhsolution.restokabmalang.data.rekap_billing.RekapBillingFragment
import com.dnhsolution.restokabmalang.data.rekap_bulanan.RekapBulananFragment
import com.dnhsolution.restokabmalang.data.rekap_harian.RekapHarianFragment
import com.dnhsolution.restokabmalang.databinding.FragmentDataLamaBinding
import com.google.android.material.tabs.TabLayoutMediator

class DataFragment2 : Fragment() {

    private lateinit var tabMain: TabLayout
    private lateinit var viewpager: ViewPager2

    private var titles = arrayOf("Harian", "Bulanan", "Billing")
    private lateinit var binding : FragmentDataLamaBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDataLamaBinding.inflate(layoutInflater)
        val view = binding.root
//        val view =  inflater.inflate(R.layout.fragment_data, container, false)
        viewpager = binding.viewpagerMain
        tabMain = binding.tabsMain

        val fragmentAdapter = ScreenSlidePagerAdapter(this)
        viewpager.adapter = fragmentAdapter

//        argTab = MainActivity.argTab

        TabLayoutMediator(tabMain, viewpager) { tab, position ->
            tab.text = titles[position]
        }.attach()

        viewpager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                Log.e("onPageScrolled", position.toString())
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                Log.e("onPageScrollState", state.toString())
            }
        })
        return view
    }

    private inner class ScreenSlidePagerAdapter(fa: Fragment) : FragmentStateAdapter(fa) {

        override fun getItemCount(): Int = titles.size

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    RekapHarianFragment.newInstance("0")
                }
                1 -> {
                    RekapBulananFragment.newInstance("1")
                }
                else -> {
                    return RekapBillingFragment.newInstance("2")
                }
            }
        }
    }
}