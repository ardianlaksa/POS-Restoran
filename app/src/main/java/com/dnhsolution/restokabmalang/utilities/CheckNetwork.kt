package com.dnhsolution.restokabmalang.utilities

import android.content.Context
import android.net.ConnectivityManager

class CheckNetwork {
    fun checkingNetwork(context:Context): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            return true
        } else {
            return false
        }
    }
}