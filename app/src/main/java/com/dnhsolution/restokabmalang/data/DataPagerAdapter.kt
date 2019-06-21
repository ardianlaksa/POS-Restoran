package com.dnhsolution.restokabmalang.data

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.dnhsolution.restokabmalang.data.rekap_bulanan.RekapBulananFragment
import com.dnhsolution.restokabmalang.data.rekap_harian.RekapHarianFragment

class DataPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                RekapHarianFragment.newInstance("0")
            }
            else -> {
                return RekapBulananFragment.newInstance("1")
            }
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Rekap Harian"
            else -> {
                return "Rekap Bulanan"
            }
        }
    }
}