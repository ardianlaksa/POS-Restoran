package com.dnhsolution.restokabmalang.transaksi.produk_list

import android.os.AsyncTask
import com.dnhsolution.restokabmalang.utilities.ProdukOnTask
import com.dnhsolution.restokabmalang.utilities.Connecting

class ProdukListJsonTask(private val listener : ProdukOnTask) : AsyncTask<String, Void, String?>() {
    override fun doInBackground(vararg params: String): String? {
        return Connecting().getConnection(params[0])
    }

    override fun onCancelled() {
        listener.produkOnTask("")
    }

    // onPostExecute displays the results of the AsyncTask.
    override fun onPostExecute(result: String?) {
        listener.produkOnTask(result)
    }
}