package com.dnhsolution.restokabmalang.data.rekap_bulanan.task

import android.os.AsyncTask
import com.dnhsolution.restokabmalang.utilities.RekapBulananOnTask
import com.dnhsolution.restokabmalang.utilities.Connecting

class RekapBulananJsonTask(private val listener : RekapBulananOnTask) : AsyncTask<String, Void, String?>() {
    override fun doInBackground(vararg params: String): String? {
        return Connecting().getConnection(params[0])
    }

    override fun onCancelled() {
        listener.rekapBulananOnTask("")
    }

    // onPostExecute displays the results of the AsyncTask.
    override fun onPostExecute(result: String?) {
        listener.rekapBulananOnTask(result)
    }
}