package com.dnhsolution.restokabmalang.tersimpan

import com.dnhsolution.restokabmalang.utilities.BaseTask
import com.dnhsolution.restokabmalang.utilities.OnDataFetched
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

internal class CekDataTersimpanNetworkTask(
        private val listener: OnDataFetched, private val url: String) : BaseTask<Any?>() {

    @Throws(Exception::class)
    override fun call(): Any {
        return try {
            val url = URL(url)
            val urlc: HttpURLConnection = url.openConnection() as HttpURLConnection
            urlc.setRequestProperty("User-Agent", "test")
            urlc.setRequestProperty("Connection", "close")
            urlc.connectTimeout = 1000 // mTimeout is in seconds
            urlc.connect()
            if(urlc.responseCode == 200) "1"
            else "0"
        } catch (e: IOException) {
//            Log.i(_tag, "Error checking internet connection", e)
            ""
        }
    }

    override fun setDataAfterLoading(result: Any?) {
        listener.setDataInPageWithResult(result)
    }
}