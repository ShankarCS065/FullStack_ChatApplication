package com.krashkrosh748199.chatapp.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AlertDialog
import java.io.File

object Utility {
    val apiUrl:String ="http://192.168.11.69:3000"

    fun getExtension(contentResolver: ContentResolver,uri: Uri):String{
        if (uri.scheme == ContentResolver.SCHEME_CONTENT){
            val mime = MimeTypeMap.getSingleton()
           return mime.getExtensionFromMimeType(contentResolver.getType(uri)).toString()
        }
        else{
               return MimeTypeMap.getFileExtensionFromUrl(
                   Uri.fromFile(
                       uri.path?.let { File(it) }
                   ).toString()
               )
        }

    }

    fun getFileName(contentResolver: ContentResolver,uri:Uri):String{
       val cursor:Cursor? = contentResolver.query(uri,null,null,null,null)
       val nameIndex =  cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor?.moveToFirst()
      val name =  nameIndex?.let {
            cursor.getString(it)
        }
        cursor?.close()
        return name.toString()

    }

    fun showAlert(context: Context,title:String="",
    message:String="",onYes:Runnable?=null,onNo:Runnable?=null){
        val alertDialogBuilder = AlertDialog.Builder(context)
         alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton(android.R.string.yes){ dialog,which->
            onYes?.run()
        }
        alertDialogBuilder.setNegativeButton(android.R.string.no){ dialog,which->
            onNo?.run()

        }
        alertDialogBuilder.show()
    }
}