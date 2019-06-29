package com.dnhsolution.restokabmalang.sistem.master

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dnhsolution.restokabmalang.R
import com.dnhsolution.restokabmalang.SistemMasterOnTask
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class SistemMasterJsonTask(private val listener : SistemMasterOnTask, private val params : HashMap<String, String>
                          , private val files : HashMap<String, String>, private val context: Context)
    : AsyncTask<String, Int, String?>() {

    private var builder: NotificationCompat.Builder? = null
    private val tag = javaClass.simpleName
    private var lineEnd = "\r\n"
    private var twoHyphens = "--"
    private var boundary = "*****"
    private var notificationId = 0
    private var CHANNEL_ID = "sipanji_"
    private var fileName = ""

    override fun doInBackground(vararg url: String): String? {
        return postFileConnection(url[0],params,files)
    }

    fun postFileConnection(myurl: String, param:HashMap<String, String>, files:HashMap<String, String>): String? {

        val conn: HttpURLConnection?
        var dataOutputStream: DataOutputStream? = null
        var bytesRead: Int
        var bytesAvailable: Int
        var bufferSize: Int
        var buffer: ByteArray
        val maxBufferSize = 1 * 1024 * 1024
        var fileInputStream: FileInputStream? = null
        var sentData: Int
        var statusPengiriman = ""

        try {
            val url = URL(myurl)

            conn = url.openConnection() as HttpURLConnection
            conn.doInput = true
            conn.doOutput = true
            conn.useCaches = false
            conn.requestMethod = "POST"
            conn.setRequestProperty("Connection", "Keep-Alive")
            conn.setRequestProperty("ENCTYPE", "multipart/form-data")
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")

            var it1 = files.entries.iterator()
            while (it1.hasNext()) {
                val pair = it1.next()
                println(tag+" "+pair.key+" "+pair.value)

                conn.setRequestProperty(pair.key,pair.value)
            }

            dataOutputStream = DataOutputStream(conn.outputStream)
            val it = param.entries.iterator()
            while (it.hasNext()) {
                val pair = it.next()
                println(tag+" "+pair.key+" "+pair.value)
                if (pair.key == "statusPengiriman") {
                    statusPengiriman = pair.value
                }
                addFormField(dataOutputStream, pair.key, pair.value)
                it.remove() // avoids a ConcurrentModificationException
            }

            it1 = files.entries.iterator()
            var iterasi = 1
            while (it1.hasNext()) {
                val pair = it1.next()
                println(tag + " iterasi " + pair.key + " " + pair.value)

                CHANNEL_ID = CHANNEL_ID + iterasi
                notificationId = iterasi
                fileName = pair.value
                fileName = fileName.substring(fileName.lastIndexOf("/")+1)

                addFileFormField(dataOutputStream, pair.key, pair.value)

                val sourceFile = File(pair.value)

                fileInputStream = FileInputStream(sourceFile)

                bytesAvailable = fileInputStream.available()
                bufferSize = Math.min(bytesAvailable, maxBufferSize)
                buffer = ByteArray(bufferSize)

                bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                sentData = 0
                while (bytesRead > 0) {
                    sentData += bytesRead
                    val progress = ((sentData / bytesAvailable) * 100)
                    Log.d("progress", progress.toString())

                    println("statusPengiriman $statusPengiriman")
                    if (statusPengiriman == "2") {
                        dataOutputStream.flush()
                        publishProgress(progress)
                    }

                    dataOutputStream.write(buffer, 0, bufferSize)

                    bytesAvailable = fileInputStream.available()
                    bufferSize = Math.min(bytesAvailable, maxBufferSize)
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                }

                dataOutputStream.writeBytes(lineEnd)
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)

                it1.remove() // avoids a ConcurrentModificationException
                iterasi++
            }

            val response = conn.responseCode
            val inputStream = conn.inputStream
            val serverResponseMessage = conn.responseMessage

            Log.i("uploadFile", "HTTP Response is : $serverResponseMessage: $response")

            if (response != 200) { return "" }
            return readIt(inputStream)

        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        } finally {
            fileInputStream?.close()
            dataOutputStream?.close()
        }
    }

    private fun addFileFormField(dataOutputStream: DataOutputStream, parameter: String, filename: String) {
        try {
            dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd)
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"$parameter\";filename=\"$filename\"$lineEnd")
//            dataOutputStream.writeBytes("Content-Type: " + URLConnection.guessContentTypeFromName(filename))
            dataOutputStream.writeBytes(lineEnd)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addFormField(dataOutputStream: DataOutputStream, parameter: String, value: String) {
        try {
            dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd)
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"$parameter\"$lineEnd")
            dataOutputStream.writeBytes(lineEnd)
            dataOutputStream.writeBytes(value)
            dataOutputStream.writeBytes(lineEnd)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Reads an InputStream and converts it to a String.
    private fun readIt(stream: InputStream?): String {
        val reader = InputStreamReader(stream!!, "UTF-8")

        var nextCharacter: Int // read() returns an int, we cast it to char later
        var responseData = ""
        while (true) { // Infinite loop, can only be stopped by a "break" statement
            nextCharacter = reader.read() // read() without parameters returns one character
            if (nextCharacter == -1)
            // A return value of -1 means that we reached the end
                break
            responseData += nextCharacter.toChar() // The += operator appends the character to the end of the string
        }
        return responseData
    }

    override fun onCancelled() {
        listener.sistemMasterOnTask("")
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setContentTitle("Upload File $fileName")
            setContentText("Upload in progress")
            setSmallIcon(R.mipmap.ic_launcher_round)
            setPriority(NotificationCompat.PRIORITY_LOW)
        }
        val PROGRESS_MAX = 100
        val PROGRESS_CURRENT = values[0] as Int
        NotificationManagerCompat.from(context).apply {
            // Issue the initial notification with zero progress
            builder!!.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false)
            notify(notificationId, builder!!.build())

            // Do the job here that tracks the progress.
            // Usually, this should be in a
            // worker thread
            // To show progress, update PROGRESS_CURRENT and update the notification with:
            // builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
            // notificationManager.notify(notificationId, builder.build());

            // When done, update the notification one more time to remove the progress bar
//            builder.setContentText("Upload complete")
//                .setProgress(0, 0, false)
//            notify(notificationId, builder.build())
        }
    }

    // onPostExecute displays the results of the AsyncTask.
    override fun onPostExecute(result: String?) {
            NotificationManagerCompat.from(context).apply {

                // When done, update the notification one more time to remove the progress bar
                if (builder != null) {
                    builder!!.setContentText("Upload complete")
                        .setProgress(0, 0, false)
                    notify(notificationId, builder!!.build())
                }
            }

        listener.sistemMasterOnTask(result)
    }

}