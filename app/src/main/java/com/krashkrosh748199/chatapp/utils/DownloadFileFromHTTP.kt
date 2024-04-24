package com.krashkrosh748199.chatapp.utils

import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import com.krashkrosh748199.chatapp.interfaces.AttachmentInterface
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadFileFromHTTP(
    var imagePath:String,
    var fileName:String,
    var attachmentInterface: AttachmentInterface
):AsyncTask<Void,Void,Void>(){

    var cachePath:String = ""

    override fun doInBackground(vararg params: Void?): Void?{
        try {
            val root =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val directoryPath = File(root.absolutePath + "/")
            if (!directoryPath.exists()) {
                directoryPath.mkdir()
            }
            val cachePath = File("$directoryPath/$fileName")
            cachePath.createNewFile()

            val buffer = ByteArray(1024)
            var url = URL(imagePath)
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.doOutput = false
            urlConnection.connect()

            val inputStream = urlConnection.inputStream
            val fileOutput = FileOutputStream(cachePath)
            var bufferLength: Int

            while (inputStream.read(buffer).also {
                    bufferLength = it
                } > 0) {
                fileOutput.write(buffer, 0, bufferLength)
            }
            fileOutput.write(buffer)
            inputStream.close()
            fileOutput.close()

            this.cachePath = cachePath.toString()
        }
        catch (exp:Exception){
            Log.i("mylog",exp.toString())
        }
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        attachmentInterface.onDownloaded(cachePath)
    }
}