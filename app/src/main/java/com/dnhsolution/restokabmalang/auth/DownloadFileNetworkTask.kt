package com.dnhsolution.restokabmalang.auth

import com.dnhsolution.restokabmalang.utilities.BaseTask
import com.dnhsolution.restokabmalang.utilities.Connecting
import com.dnhsolution.restokabmalang.utilities.DownloadFileNetworkResult

internal class DownloadFileNetworkTask(//listener in fragment that shows and hides ProgressBar
    private val listener: DownloadFileNetworkResult,
    private val url: String,
    private val saveDir: String
) : BaseTask<Any?>() {
    override fun call(): Any {
        return Connecting().downloadFile(url, saveDir)
    }

    override fun setDataAfterLoading(result: Any?) {
        listener.downloadFileNetworkResult(result)
    }
}