package com.dnhsolution.restokabmalang.data

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.data.rekap_harian.RekapHarianListElement
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_data.*

class DataFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_data, container, false)
        val viewpager = view.findViewById(R.id.viewpager_main) as ViewPager
        val tabMain = view.findViewById(R.id.tabs_main) as TabLayout

        val fragmentAdapter = DataPagerAdapter(activity!!.supportFragmentManager)
        viewpager.adapter = fragmentAdapter

        tabMain.setupWithViewPager(viewpager)

        return view
    }
}