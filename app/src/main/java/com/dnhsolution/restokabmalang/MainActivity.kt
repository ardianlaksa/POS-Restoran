package com.dnhsolution.restokabmalang

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dnhsolution.restokabmalang.data.DataFragment
import com.dnhsolution.restokabmalang.sistem.MainMaster
import com.dnhsolution.restokabmalang.transaksi.produk_list.ProdukListFragment
import com.dnhsolution.restokabmalang.sistem.master.SistemMasterActivity
import com.dnhsolution.restokabmalang.utilities.Url
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
                startActivity(Intent(this, MainMaster::class.java))
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
    
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var sharedPreferences: SharedPreferences
        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        val label = sharedPreferences.getString(Url.setLabel, "Belum disetting")
        val tema = sharedPreferences.getString(Url.setTema, "0")
        if (tema!!.equals("0", ignoreCase = true)) {
            this@MainActivity.setTheme(R.style.Theme_First)
        } else if (tema.equals("1", ignoreCase = true)) {
            this@MainActivity.setTheme(R.style.Theme_Second)
        } else if (tema.equals("2", ignoreCase = true)) {
            this@MainActivity.setTheme(R.style.Theme_Third)
        } else if (tema.equals("3", ignoreCase = true)) {
            this@MainActivity.setTheme(R.style.Theme_Fourth)
        } else if (tema.equals("4", ignoreCase = true)) {
            this@MainActivity.setTheme(R.style.Theme_Fifth)
        } else if (tema.equals("5", ignoreCase = true)) {
            this@MainActivity.setTheme(R.style.Theme_Sixth)
        }
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = label
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, ProdukListFragment()).commit()
    }
}
