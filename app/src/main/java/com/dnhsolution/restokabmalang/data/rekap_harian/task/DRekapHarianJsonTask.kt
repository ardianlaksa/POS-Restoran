package com.dnhsolution.restokabmalang.data.rekap_harian.task

import android.os.AsyncTask
import com.dnhsolution.restokabmalang.utilities.RekapHarianOnTask
import com.dnhsolution.restokabmalang.utilities.Connecting
import com.dnhsolution.restokabmalang.utilities.DRekapHarianOnTask

class DRekapHarianJsonTask(private val listener : DRekapHarianOnTask) : AsyncTask<String, Void, String?>() {
    override fun doInBackground(vararg params: String): String? {
        return Connecting().getConnection(params[0])
    }

    override fun onCancelled() {
        listener.DrekapHarianOnTask("")
    }

    // onPostExecute displays the results of the AsyncTask.
    override fun onPostExecute(result: String?) {
        listener.DrekapHarianOnTask(result)
    }
}