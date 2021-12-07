package com.dnhsolution.restokabmalang.transaksi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.transaksi.produk_list.ProdukListElement
import com.dnhsolution.restokabmalang.transaksi.produk_list.ProdukListFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*

class TransaksiFragment : Fragment() {

    private lateinit var tabMain: TabLayout
    private lateinit var viewpager: ViewPager2
    var apsMakanan = ArrayList<ProdukListElement>()
    var apsMinuman = ArrayList<ProdukListElement>()
    var apsDll = ArrayList<ProdukListElement>()

    private val titles = arrayOf("Makanan", "Minuman", "DLL")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_data, container, false)
        viewpager = view.findViewById(R.id.viewpager_main) as ViewPager2
        tabMain = view.findViewById(R.id.tabs_main) as TabLayout

        val fragmentAdapter = ScreenSlidePagerAdapter(this)
        viewpager.adapter = fragmentAdapter

        TabLayoutMediator(tabMain, viewpager) { tab, position ->
            //To get the first name of doppelganger celebrities
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
                (activity as MainActivity).toolbar.collapseActionView()
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
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
                if(apsMinuman.contains(produk)) apsMinuman.remove(produk)
                else apsMinuman.add(produk)
                println("tambahDataApsList $arg ${apsMinuman.size}")
            }
            "3" -> {
                if(apsDll.contains(produk)) apsDll.remove(produk)
                else apsDll.add(produk)
                println("tambahDataApsList $arg ${apsDll.size}")
            }
            else -> {
                if(apsMakanan.contains(produk)) apsMakanan.remove(produk)
                else apsMakanan.add(produk)
                println("tambahDataApsList $arg ${apsMakanan.size}")
            }
        }
    }

    fun tampilDataApsList(arg: String) : ArrayList<ProdukListElement>{
        return when(arg){
            "2" -> {
                apsMinuman
            }
            "3" -> {
                apsDll
            }
            else -> {
                apsMakanan
            }
        }
    }

    private inner class ScreenSlidePagerAdapter(fa: Fragment) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = titles.size
        override fun createFragment(position: Int): Fragment = ProdukListFragment.newInstance((position+1).toString())
    }
}