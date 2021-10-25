package com.dnhsolution.restokabmalang.data

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.dnhsolution.restokabmalang.data.rekap_billing.RekapBillingFragment
import com.dnhsolution.restokabmalang.data.rekap_bulanan.RekapBulananFragment
import com.dnhsolution.restokabmalang.data.rekap_harian.RekapHarianFragment

class DataPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
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

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Harian"
            1 -> "Bulanan"
            else -> {
                return "Billing"
            }
        }
    }
}