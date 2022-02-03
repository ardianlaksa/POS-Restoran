package com.dnhsolution.restokabmalang.transaksi

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
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.databinding.FragmentDataBinding
import com.dnhsolution.restokabmalang.transaksi.produk_list.ProdukListElement
import com.dnhsolution.restokabmalang.transaksi.produk_list.ProdukListFragment
import com.google.android.material.tabs.TabLayoutMediator

class TransaksiFragment : Fragment() {

    private lateinit var tabMain: TabLayout
    private lateinit var viewpager: ViewPager2
    var apsSatu = ArrayList<ProdukListElement>()
    var apsDua = ArrayList<ProdukListElement>()
    var apsTiga = ArrayList<ProdukListElement>()
    var apsEmpat = ArrayList<ProdukListElement>()

    private var titles = arrayOf("Makanan", "Minuman", "DLL")
    private var argTab = arrayOf("")
    private lateinit var binding : FragmentDataBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDataBinding.inflate(layoutInflater)
        val view = binding.root
//        val view =  inflater.inflate(R.layout.fragment_data, container, false)
        viewpager = binding.viewpagerMain
        tabMain = binding.tabsMain

        val fragmentAdapter = ScreenSlidePagerAdapter(this)
        viewpager.adapter = fragmentAdapter

        argTab = MainActivity.argTab

        if(MainActivity.jenisPajak == "01") {
            titles = arrayOf("Fasilitas", "DLL")
        }

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
                (activity as MainActivity).binding.toolbar.collapseActionView()
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                Log.e("onPageScrollState", state.toString())
            }
        })
        return view
    }

    fun tambahDataApsList(arg: String,produk: ProdukListElement){
        when(arg){
            "2" -> {
                if(apsDua.contains(produk)) apsDua.remove(produk)
                else apsDua.add(produk)
            }
            "3" -> {
                if(apsTiga.contains(produk)) apsTiga.remove(produk)
                else apsTiga.add(produk)
            }
            "4" -> {
                if(apsEmpat.contains(produk)) apsEmpat.remove(produk)
                else apsEmpat.add(produk)
            }
            else -> {
                if(apsSatu.contains(produk)) apsSatu.remove(produk)
                else apsSatu.add(produk)
            }
        }
    }

    fun tampilDataApsList(arg: String) : ArrayList<ProdukListElement>{
        return when(arg){
            "2" -> {
                apsDua
            }
            "3" -> {
                apsTiga
            }
            "4" -> {
                apsEmpat
            }
            else -> {
                apsSatu
            }
        }
    }

    private inner class ScreenSlidePagerAdapter(fa: Fragment) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = titles.size
        override fun createFragment(position: Int): Fragment = ProdukListFragment.newInstance(argTab[position])
    }
}