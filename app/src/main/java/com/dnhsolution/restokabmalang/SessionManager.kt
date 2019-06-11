package com.dnhsolution.restokabmalang

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor

class SessionManager (_context: Context ) {
    // Shared Preferences
    private val pref: SharedPreferences

    // Editor for Shared preferences
    private lateinit var editor: Editor

    init {
        val PRIVATE_MODE = 0
        val PREF_NAME = "session_login"
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
    }

    fun editStatusMenu(statusmenu: String) {
        editor = pref.edit()
        editor.putString(key_wpid,statusmenu)
        editor.apply()
    }

    fun createLoginSession(isSet: Boolean,
                           wpid: String){

        editor = pref.edit()
        editor.putBoolean(is_set,isSet)
        editor.putString(key_wpid,wpid)
        editor.apply()
    }

    fun removeLoginSession(){
        editor = pref.edit()
        editor.clear()
        editor.apply()
    }

    val detailUserLogin: HashMap<String, String?>
        get(){
            val bus = HashMap<String, String?>()
            bus.put(key_wpid,pref.getString(key_wpid,null))
            return bus
        }

    val isSetIn: Boolean
        get() = pref.getBoolean(is_set, false)

    companion object {

        // All Shared Preferences Keys

        val is_set = "IsSetIn"
        val key_wpid = "wpid"
    }
}
