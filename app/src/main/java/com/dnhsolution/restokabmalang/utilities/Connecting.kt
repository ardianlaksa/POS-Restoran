package com.dnhsolution.restokabmalang.utilities

import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class Connecting {

    private val tag = javaClass.simpleName
    private var lineEnd = "\r\n"
    private var twoHyphens = "--"
    private var boundary = "*****"

    fun getConnection(myurl: String): String? {
        var inputStream: InputStream? = null

        try {
            val url = URL(myurl)
            val conn = url.openConnection() as HttpURLConnection
            conn.readTimeout = 10000
            conn.connectTimeout = 15000
            conn.requestMethod = "GET"
            conn.doInput = true
            // Starts the query
            conn.connect()
            val response = conn.responseCode
            Log.d("Debug", "The response is: $response")
            inputStream = conn.inputStream

            // Convert the InputStream into a string
            if (response != 200) { return "" }
            return readIt(inputStream)

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return ""
        } finally {
            inputStream?.close()
        }
    }

    fun postConnection(myurl: String,param:HashMap<String, String>): String? {
        var inputStream: InputStream? = null
        val dataOutputStream:DataOutputStream?

        try {
            val url = URL(myurl)
            val conn = url.openConnection() as HttpURLConnection
            conn.readTimeout = 10000
            conn.connectTimeout = 15000
            conn.requestMethod = "POST"
            conn.doInput = true
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")

            dataOutputStream = DataOutputStream(conn.outputStream)
            val it = param.entries.iterator()
            while (it.hasNext()) {
                val pair = it.next()
                println(tag+" "+pair.key+" "+pair.value)
                addFormField(dataOutputStream, pair.key, pair.value)
                it.remove() // avoids a ConcurrentModificationException
            }
            dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
            // Starts the query
            conn.connect()
            val response = conn.responseCode
            Log.d("Debug", "The response is: $response")
            inputStream = conn.inputStream

            // Convert the InputStream into a string

            if (response != 200) { return "" }
            return readIt(inputStream)

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        } finally {
            inputStream?.close()
        }
    }

    fun postFileConnection(myurl: String,param:HashMap<String, String>,files:HashMap<String, String>): String? {

        val conn: HttpURLConnection?
        var dataOutputStream: DataOutputStream? = null
        var bytesRead: Int
        var bytesAvailable: Int
        var bufferSize: Int
        var buffer: ByteArray
        val maxBufferSize = 1 * 1024 * 1024
        var fileInputStream:FileInputStream?

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
                addFormField(dataOutputStream, pair.key, pair.value)
                it.remove() // avoids a ConcurrentModificationException
            }

            it1 = files.entries.iterator()
            while (it1.hasNext()) {
                val pair = it1.next()
                println(tag+" asdf "+pair.key+" "+pair.value)

                addFileFormField(dataOutputStream, pair.key, pair.value)

                val sourceFile = File(pair.value)

                fileInputStream = FileInputStream(sourceFile)

                bytesAvailable = fileInputStream.available()
                bufferSize = Math.min(bytesAvailable, maxBufferSize)
                buffer = ByteArray(bufferSize)

                bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                while (bytesRead > 0) {
                    dataOutputStream.write(buffer, 0, bufferSize)
                    bytesAvailable = fileInputStream.available()
                    bufferSize = Math.min(bytesAvailable, maxBufferSize)
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                }

                dataOutputStream.writeBytes(lineEnd)
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)

                fileInputStream.close()
                it1.remove() // avoids a ConcurrentModificationException
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
            dataOutputStream?.flush()
            dataOutputStream?.close()
        }
    }

    @Throws(IOException::class)
    fun downloadFile(fileURL: String, saveDir: String) {
        val url = URL(fileURL)
        val httpConn = url.openConnection() as HttpURLConnection
        val responseCode = httpConn.responseCode

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            var fileName = ""
            val disposition = httpConn.getHeaderField("Content-Disposition")
            val contentType = httpConn.contentType
            val contentLength = httpConn.contentLength
            if (disposition != null) {
                // extracts file name from header field
                val index = disposition.indexOf("filename=")
                if (index > 0) {
                    fileName = disposition.substring(
                        index + 10,
                        disposition.length - 1
                    )
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(
                    fileURL.lastIndexOf("/") + 1,
                    fileURL.length
                )
            }
            println("Content-Type = $contentType")
            println("Content-Disposition = $disposition")
            println("Content-Length = $contentLength")
            println("fileName = $fileName")

            // opens input stream from the HTTP connection
            val inputStream = httpConn.inputStream
            val saveFilePath = saveDir + File.separator.toString() + fileName

            // opens an output stream to save into file
            val outputStream = FileOutputStream(saveFilePath)
            var bytesRead = -1
            val buffer = ByteArray(4096)
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.close()
            inputStream.close()
            println("File downloaded")
        } else {
            println("No file to download. Server replied HTTP code: $responseCode")
        }
        httpConn.disconnect()
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
}