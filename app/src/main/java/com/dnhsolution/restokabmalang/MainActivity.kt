package com.dnhsolution.restokabmalang

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dnhsolution.restokabmalang.data.DataFragment
import com.dnhsolution.restokabmalang.home.HomeFragment
import com.dnhsolution.restokabmalang.sistem.MainMaster
import com.dnhsolution.restokabmalang.sistem.master.SistemMasterActivity
import com.dnhsolution.restokabmalang.utilities.Url
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

//    private lateinit var textMessage: TextView
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
//                textMessage.setText(R.string.title_home)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, HomeFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_data -> {
//                textMessage.setText(R.string.title_data)

                supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, DataFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_sistem -> {
//                textMessage.setText(R.string.title_sistem)
                startActivity(Intent(this,MainMaster::class.java))
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

        if (tema.equals("0", ignoreCase = true)) {
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
            .replace(R.id.frameLayout, HomeFragment()).commit()
    }

    override fun onResume() {
        super.onResume()
        var sharedPreferences: SharedPreferences
        sharedPreferences = getSharedPreferences(Url.SESSION_NAME, Context.MODE_PRIVATE)
        val label = sharedPreferences.getString(Url.setLabel, "Belum disetting")
        supportActionBar!!.setTitle(label)
        val tema = sharedPreferences.getString(Url.setTema, "0")

//        if (tema.equals("0", ignoreCase = true)) {
//            this@MainActivity.setTheme(R.style.Theme_First)
//        } else if (tema.equals("1", ignoreCase = true)) {
//            this@MainActivity.setTheme(R.style.Theme_Second)
//        } else if (tema.equals("2", ignoreCase = true)) {
//            this@MainActivity.setTheme(R.style.Theme_Third)
//        } else if (tema.equals("3", ignoreCase = true)) {
//            this@MainActivity.setTheme(R.style.Theme_Fourth)
//        } else if (tema.equals("4", ignoreCase = true)) {
//            this@MainActivity.setTheme(R.style.Theme_Fifth)
//        } else if (tema.equals("5", ignoreCase = true)) {
//            this@MainActivity.setTheme(R.style.Theme_Sixth)
//        }
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_lanjut, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_menu_lanjut -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}
