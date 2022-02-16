package com.dnhsolution.restokabmalang.transaksi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.database.AppRoomDatabase
import com.dnhsolution.restokabmalang.databinding.FragmentDataBinding
import com.dnhsolution.restokabmalang.transaksi.tab_fragment.ProdukListElement
import com.dnhsolution.restokabmalang.transaksi.tab_fragment.ProdukListFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class TransaksiFragment : Fragment() {

    private lateinit var tabMain: TabLayout
    private lateinit var viewpager: ViewPager2
    var apsSatu = ArrayList<ProdukListElement>()
//    var apsDua = ArrayList<ProdukListElement>()
//    var apsTiga = ArrayList<ProdukListElement>()
//    var apsEmpat = ArrayList<ProdukListElement>()
    val kategoriList = ArrayList<KategoriElement>()
    private val _tag = javaClass.simpleName

    private var argTab = arrayOf("")
    private lateinit var binding : FragmentDataBinding
    private lateinit var kategoriListViewModel: KategoriListViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDataBinding.inflate(layoutInflater)
        val view = binding.root
//        val view =  inflater.inflate(R.layout.fragment_data, container, false)
        viewpager = binding.viewpagerMain
        tabMain = binding.tabsMain

        argTab = MainActivity.argTab
        kategoriListViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[KategoriListViewModel::class.java]

        // Create the observer which updates the ui
//        val kategoriListObserver = Observer<KategoriListlement>{ arg ->
//
//            val fragmentAdapter = ScreenSlidePagerAdapter(this)
//            viewpager.adapter = fragmentAdapter
//            TabLayoutMediator(tabMain, viewpager) { tab, position ->
//                tab.text = arg.list1[position].value1
//            }.attach()
//        }

        val getAppDatabase = AppRoomDatabase.getAppDataBase(requireContext())
        val a= getAppDatabase?.tblProdukKategoriDao()?.getAll()
        a?.observe(requireActivity()) { it ->
            if(kategoriList.size == 0)
                for (b in it) {
                    Log.d(_tag, a.toString())
                    kategoriList.add(
                        KategoriElement(
                            b.id.toString(),
                            b.nama,
                            b.idTempatUsaha,
                            b.idPengguna
                        )
                    )
                }
                kategoriListViewModel.items.value = KategoriListlement(kategoriList)
                // Observe the live data, passing in this activity as the life cycle owner and the observer
//                kategoriListViewModel.items.observe(requireActivity(),kategoriListObserver)
//            } else
                viewpager.adapter?.notifyDataSetChanged()
        }

        val kategoriListObserver = Observer<KategoriListlement>{ arg ->

            val fragmentAdapter = ScreenSlidePagerAdapter(this)
            viewpager.adapter = fragmentAdapter
            TabLayoutMediator(tabMain, viewpager) { tab, position ->
                tab.text = arg.list1[position].value1
            }.attach()
        }
        kategoriListViewModel.items.observe(requireActivity(),kategoriListObserver)

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
        if(apsSatu.contains(produk)) apsSatu.remove(produk)
        else apsSatu.add(produk)
//        when(arg){
//            "2" -> {
//                if(apsDua.contains(produk)) apsDua.remove(produk)
//                else apsDua.add(produk)
//            }
//            "3" -> {
//                if(apsTiga.contains(produk)) apsTiga.remove(produk)
//                else apsTiga.add(produk)
//            }
//            "4" -> {
//                if(apsEmpat.contains(produk)) apsEmpat.remove(produk)
//                else apsEmpat.add(produk)
//            }
//            else -> {
//                if(apsSatu.contains(produk)) apsSatu.remove(produk)
//                else apsSatu.add(produk)
//            }
//        }
    }

    fun tampilDataApsList(arg: String) : ArrayList<ProdukListElement> {
        val array = ArrayList<ProdukListElement>()
        for(i in apsSatu){
            if(arg == i.jnsProduk) array.add(i)
        }
        return array
//        return when(arg){
//            "2" -> {
//                apsDua
//            }
//            "3" -> {
//                apsTiga
//            }
//            "4" -> {
//                apsEmpat
//            }
//            else -> {
//                apsSatu
//            }
//        }
    }

    private inner class ScreenSlidePagerAdapter(fa: Fragment) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = kategoriList.size
        override fun createFragment(position: Int): Fragment = ProdukListFragment.newInstance(
            kategoriList[position].id)
    }
}