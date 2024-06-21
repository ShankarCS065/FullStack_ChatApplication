package com.krashkrosh748199.GFolksy.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import java.io.IOException
import java.net.URL

class FetchImageFromInternet(
    private var imageView: ImageView
) : AsyncTask<String, Void, Bitmap>() {

    override fun doInBackground(vararg urls: String?): Bitmap? {
        val url = urls.firstOrNull()
        if (url.isNullOrEmpty()) {
            return null
        }

        return try {
            val inputStream = URL(url).openStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            Log.e("FetchImageFromInternet", "Error fetching image from URL: $url", e)
            null
        }
    }

    override fun onPostExecute(result: Bitmap?) {
        super.onPostExecute(result)
        result?.let {
            imageView.setImageBitmap(it)
        }
    }

    override fun onCancelled(result: Bitmap?) {
        super.onCancelled(result)
        // Clean up any resources if needed
    }
}
