package com.dnhsolution.restokabmalang.transaksi.keranjang.tambah

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.database.AppRoomDatabase
import com.dnhsolution.restokabmalang.databinding.ActivityTambahhProdukTransaksiBinding
import com.dnhsolution.restokabmalang.transaksi.KategoriElement
import com.dnhsolution.restokabmalang.transaksi.KategoriListViewModel
import com.dnhsolution.restokabmalang.transaksi.KategoriListlement
import com.dnhsolution.restokabmalang.transaksi.ProdukSerializable
import com.dnhsolution.restokabmalang.transaksi.tab_fragment.ProdukListElement
import com.dnhsolution.restokabmalang.utilities.Url
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class TambahProdukTransaksiActivity : AppCompatActivity() {

    private var loadingLayout: RelativeLayout? = null
    private lateinit var handler: Handler
    private lateinit var tabMain: TabLayout
    private lateinit var viewpager: ViewPager2
    var apsSatu = ArrayList<ProdukListElement>()
//    var apsDua = ArrayList<ProdukListElement>()
//    var apsTiga = ArrayList<ProdukListElement>()
//    var apsEmpat = ArrayList<ProdukListElement>()
    private val _tag = javaClass.simpleName
    var valueArgsFromKeranjang: ArrayList<ProdukSerializable>? = null

    companion object{
        var jumlahProdukTerpilih = 0
        var uuid: String? = null
        var idPengguna: String? = null
        var idTmpUsaha: String? = null
    }

    lateinit var binding: ActivityTambahhProdukTransaksiBinding
    private lateinit var kategoriListViewModel: KategoriListViewModel
    val kategoriList = ArrayList<KategoriElement>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        idTmpUsaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA, "")
        idPengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "0")
        uuid = sharedPreferences.getString(Url.SESSION_UUID, "")
        binding = ActivityTambahhProdukTransaksiBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val i = intent
        valueArgsFromKeranjang = i.getParcelableArrayListExtra("ARRAYLIST")
        Log.i(_tag,"onResume $valueArgsFromKeranjang")
        jumlahProdukTerpilih = valueArgsFromKeranjang?.size ?: 0

        val toolbar = binding.toolbar
        viewpager = binding.viewpagerMain
        tabMain = binding.tabsMain
        loadingLayout = binding.loadingLayout

        setSupportActionBar(toolbar)
        kategoriListViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[KategoriListViewModel::class.java]
        val getAppDatabase = AppRoomDatabase.getAppDataBase(this)
        val a= getAppDatabase?.tblProdukKategoriDao()?.getAll()
        a?.observe(this) { it ->
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
            viewpager.adapter?.notifyDataSetChanged()
        }

        val fragmentAdapter = ScreenSlidePagerAdapter(this)
        viewpager.adapter = fragmentAdapter

        val kategoriListObserver = Observer<KategoriListlement>{ arg ->
            TabLayoutMediator(tabMain, viewpager) { tab, position ->
                tab.text = arg.list1[position].value1
            }.attach()
        }

        // Observe the live data, passing in this activity as the life cycle owner and the observer
        kategoriListViewModel.items.observe(this,kategoriListObserver)

        viewpager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                Log.e("onPageScrolled", position.toString())
                toolbar.collapseActionView()
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.e("Selected_Page", position.toString())
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                Log.e("onPageScrollState", state.toString())
            }
        })

        handler = Handler(Looper.getMainLooper())
    }

    fun tambahDataApsList(arg: String,produk: ProdukListElement){
        loadingLayout?.visibility = View.VISIBLE
        val milis = valueArgsFromKeranjang!!.size*10
        handler.postDelayed({
            if(!produk.isFavorite) {
                var valueIndex = -1
                valueArgsFromKeranjang!!.forEachIndexed { index, produkSerializable ->
                    if (produk.idItem == produkSerializable.idItem) {
                        valueIndex = index
                    }
                }
                valueArgsFromKeranjang!!.removeAt(valueIndex)
            }
            if(apsSatu.contains(produk)) apsSatu.remove(produk)
            else apsSatu.add(produk)
//            when(arg){
//                "2" -> {
//                    if(apsDua.contains(produk)) {
//                        apsDua.remove(produk)
//                    }else apsDua.add(produk)
//                }
//                "3" -> {
//                    if(apsTiga.contains(produk)) {
//                        apsTiga.remove(produk)
//                    } else apsTiga.add(produk)
//                }
//                "4" -> {
//                    if(apsEmpat.contains(produk)) apsEmpat.remove(produk)
//                    else apsEmpat.add(produk)
//                }
//                else -> {
//                    if(apsSatu.contains(produk)) {
//                        apsSatu.remove(produk)
//                    } else apsSatu.add(produk)
//                }
//            }
            loadingLayout?.visibility = View.INVISIBLE
        }, milis.toLong())
    }

    fun tampilDataApsList(arg: String) : ArrayList<ProdukListElement>{
        val array = ArrayList<ProdukListElement>()
        for(i in apsSatu){
            if(arg == i.jnsProduk) array.add(i)
        }
        return array
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = kategoriList.size
        override fun createFragment(position: Int): Fragment =
            TambahProdukListFragment.newInstance(kategoriList[position].id,valueArgsFromKeranjang)
    }
}