package com.dnhsolution.restokabmalang.data.rekap_harian.task

import android.os.AsyncTask
import com.dnhsolution.restokabmalang.utilities.RekapHarianOnTask
import com.dnhsolution.restokabmalang.utilities.Connecting

class RekapHarianJsonTask(private val listener : RekapHarianOnTask) : AsyncTask<String, Void, String?>() {
    override fun doInBackground(vararg params: String): String? {
        return Connecting().getConnection(params[0])
    }

    override fun onCancelled() {
        listener.rekapHarianOnTask("")
    }

    // onPostExecute displays the results of the AsyncTask.
    override fun onPostExecute(result: String?) {
        listener.rekapHarianOnTask(result)
    }
}