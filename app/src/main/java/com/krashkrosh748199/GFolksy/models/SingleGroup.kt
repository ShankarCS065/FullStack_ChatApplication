package com.krashkrosh748199.GFolksy.models

class SingleGroup {
    var _id:String = ""
    var name:String=""
    var createdBy:User = User()
    var attachment:String=""
    var extension:String= ""
    var originalAttachmentName:String=""
    var attachmentName:String=""
    var members:ArrayList<GroupMember> = ArrayList()
    var hasInvite:Boolean = false
    var unread:Boolean = false
    var createdAt:Double = 0.0
}