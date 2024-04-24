package com.krashkrosh748199.chatapp.models

class Message {
    var _id:String = ""
    var message: String = ""
    var sender:User = User()
    var receiver:User = User()
    var attachment : String = ""
    var extension: String = ""
    var attachmentName: String = ""
    var createdAt:Double = 0.0
}