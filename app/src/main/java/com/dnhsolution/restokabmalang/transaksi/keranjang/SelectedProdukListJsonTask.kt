package com.dnhsolution.restokabmalang.transaksi.keranjang

import android.os.AsyncTask
import com.dnhsolution.restokabmalang.utilities.KeranjangTransaksiOnTask
import com.dnhsolution.restokabmalang.utilities.Connecting

class SelectedProdukListJsonTask(private val listener : KeranjangTransaksiOnTask, private val params : HashMap<String, String>)
    : AsyncTask<String, Void, String?>() {

    override fun doInBackground(vararg url: String): String? {
        return Connecting().postConnection(url[0],params)
    }

    override fun onCancelled() {
        listener.keranjangTransaksiOnTask("")
    }

    // onPostExecute displays the results of the AsyncTask.
    override fun onPostExecute(result: String?) {
        listener.keranjangTransaksiOnTask(result)
    }

}