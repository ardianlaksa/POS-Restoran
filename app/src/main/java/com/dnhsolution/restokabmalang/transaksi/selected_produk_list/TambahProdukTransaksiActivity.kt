package com.dnhsolution.restokabmalang.transaksi.selected_produk_list

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.dnhsolution.restokabmalang.MainActivity
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.transaksi.ProdukSerializable
import com.dnhsolution.restokabmalang.transaksi.produk_list.ProdukListElement
import com.dnhsolution.restokabmalang.utilities.Url
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*

class TambahProdukTransaksiActivity : AppCompatActivity() {

    private var loadingLayout: RelativeLayout? = null
    private lateinit var handler: Handler
    private lateinit var tabMain: TabLayout
    private lateinit var viewpager: ViewPager2
    var apsSatu = ArrayList<ProdukListElement>()
    var apsDua = ArrayList<ProdukListElement>()
    var apsTiga = ArrayList<ProdukListElement>()
    var apsEmpat = ArrayList<ProdukListElement>()

    private var titles = arrayOf("Makanan", "Minuman", "DLL")
    private var argTab = arrayOf("")
    private val _tag = javaClass.simpleName
    var valueArgsFromKeranjang: ArrayList<ProdukSerializable>? = null

    companion object{
        var jumlahProdukTerpilih = 0
        var uuid: String? = null
        var idPengguna: String? = null
        var idTmpUsaha: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        idTmpUsaha = sharedPreferences.getString(Url.SESSION_ID_TEMPAT_USAHA, "")
        idPengguna = sharedPreferences.getString(Url.SESSION_ID_PENGGUNA, "0")
        uuid = sharedPreferences.getString(Url.SESSION_UUID, "")
        setContentView(R.layout.activity_tambahh_produk_transaksi)
        setSupportActionBar(toolbar)

        argTab = MainActivity.argTab

        if(MainActivity.jenisPajak == "01") {
            titles = arrayOf("Fasilitas", "DLL")
        }

        val i = intent
        valueArgsFromKeranjang = i.getParcelableArrayListExtra("ARRAYLIST")
        Log.i(_tag,"onResume $valueArgsFromKeranjang")
        jumlahProdukTerpilih = valueArgsFromKeranjang?.size ?: 0

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        viewpager = findViewById<ViewPager2>(R.id.viewpager_main)
        tabMain = findViewById<TabLayout>(R.id.tabs_main)
        loadingLayout = findViewById<RelativeLayout>(R.id.loadingLayout)

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
//                    println("tambahDataApsList forEachIndexed ${produk.idItem} ${produkSerializable.idItem}")
                    if (produk.idItem == produkSerializable.idItem) {
                        valueIndex = index
                    }
                }
                valueArgsFromKeranjang!!.removeAt(valueIndex)
            }
            when(arg){
                "2" -> {
                    if(apsDua.contains(produk)) {
                        apsDua.remove(produk)
                    }else apsDua.add(produk)
//                    println("tambahDataApsList $arg ${apsDua.size}")
                }
                "3" -> {
                    if(apsTiga.contains(produk)) {
                        apsTiga.remove(produk)
                    } else apsTiga.add(produk)
//                    println("tambahDataApsList $arg ${apsTiga.size}")
                }
                "4" -> {
                    if(apsEmpat.contains(produk)) apsEmpat.remove(produk)
                    else apsEmpat.add(produk)
                }
                else -> {
                    if(apsSatu.contains(produk)) {
                        apsSatu.remove(produk)
                    } else apsSatu.add(produk)
//                    println("tambahDataApsList $arg ${apsSatu.size}")
                }
            }
            loadingLayout?.visibility = View.INVISIBLE
        }, milis.toLong())
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

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = titles.size
        override fun createFragment(position: Int): Fragment = TambahProdukListFragment.newInstance((argTab[position]),valueArgsFromKeranjang)
    }
}