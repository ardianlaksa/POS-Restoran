package com.dnhsolution.restokabmalang.data

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.dnhsolution.restokabmalang.R
import com.google.android.material.tabs.TabLayout

class DataFragment : Fragment() {

    private lateinit var tabMain: TabLayout
    private lateinit var viewpager: ViewPager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_data_lama, container, false)
        viewpager = view.findViewById(R.id.viewpager_main) as ViewPager
        tabMain = view.findViewById(R.id.tabs_main) as TabLayout

        val fragmentAdapter = DataPagerAdapter(childFragmentManager)
        viewpager.adapter = fragmentAdapter

        tabMain.setupWithViewPager(viewpager)

        return view
    }
}