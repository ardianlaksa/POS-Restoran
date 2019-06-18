package com.dnhsolution.restokabmalang

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dnhsolution.restokabmalang.data.DataFragment
import com.dnhsolution.restokabmalang.home.HomeFragment
import com.dnhsolution.restokabmalang.sistem.SistemFragment
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
                startActivity(Intent(this,SistemFragment::class.java))
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
            .replace(R.id.frameLayout, HomeFragment()).commit()
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
