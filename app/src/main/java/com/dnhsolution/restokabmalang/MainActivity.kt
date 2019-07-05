package com.dnhsolution.restokabmalang

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dnhsolution.restokabmalang.data.DataFragment
import com.dnhsolution.restokabmalang.transaksi.produk_list.ProdukListFragment
import com.dnhsolution.restokabmalang.sistem.master.SistemMasterActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object{
        var valueArgsFromKeranjang:Int? = null
    }

    private val _tag = javaClass.simpleName
    //    private lateinit var textMessage: TextView
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_transaksi -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, ProdukListFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_data -> {
//                textMessage.setText(R.string.title_data)

                supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, DataFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_master -> {
//                textMessage.setText(R.string.title_sistem)
                startActivity(Intent(this,SistemMasterActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, ProdukListFragment()).commit()
    }
}
