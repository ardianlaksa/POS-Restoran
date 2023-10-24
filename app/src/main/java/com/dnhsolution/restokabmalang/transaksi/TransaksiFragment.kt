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
    val kategoriList = ArrayList<KategoriElement>()
    private val _tag = javaClass.simpleName

//    private var argTab = arrayOf("")
    private lateinit var binding : FragmentDataBinding
    private lateinit var kategoriListViewModel: KategoriListViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDataBinding.inflate(layoutInflater)
        val view = binding.root
        viewpager = binding.viewpagerMain
        tabMain = binding.tabsMain

//        argTab = MainActivity.argTab

        if(getActivity()!=null && isAdded())
        kategoriListViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[KategoriListViewModel::class.java]

        val getAppDatabase = AppRoomDatabase.getAppDataBase(requireContext())
        val a= getAppDatabase?.tblProdukKategoriDao()?.getAll()
        a?.observe(requireActivity()) { it ->
            if(kategoriList.size == 0)
                for (b in it) {
                    Log.d(_tag, a.toString())
                    kategoriList.add(
                        KategoriElement(
                            b.idKategoriServer.toString(),
                            b.nama,
                            b.idTempatUsaha,
                            b.idPengguna
                        )
                    )
                }
                kategoriListViewModel.items.value = KategoriListlement(kategoriList)
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
    }

    fun tampilDataApsList() : ArrayList<ProdukListElement> {
        val array = ArrayList<ProdukListElement>()
        for(i in apsSatu){
            array.add(i)
        }
        return array
    }

    private inner class ScreenSlidePagerAdapter(fa: Fragment) : FragmentStateAdapter(fa) {

        override fun getItemCount(): Int = kategoriList.size

//        override fun createFragment(position: Int): Fragment {
//            return ProdukListFragment()
//        }

//        override fun getItem(position: Int): Fragment = SlideFragment()
        override fun createFragment(position: Int): Fragment = ProdukListFragment.newInstance(
            kategoriList[position].id)
    }
}