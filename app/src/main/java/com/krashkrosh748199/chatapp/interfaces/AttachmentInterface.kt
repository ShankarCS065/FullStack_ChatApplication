package com.krashkrosh748199.chatapp.interfaces

import android.view.View

interface AttachmentInterface {
    fun onDownload(view:View)
    fun onDownloaded(path:String)

}