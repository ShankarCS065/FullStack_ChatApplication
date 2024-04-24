const express = require("express")
const auth = require("./auth")
const crypto = require("crypto-js")
const { ObjectId } = require("mongodb")
const fileSystem = require("fs")
const algorithm = "aes-256-cbc"
const key = "abcdefghijklmnopqrstuvwxyz123456"


module.exports = {
    init(app){

       const router = express.Router()
         
       router.post("/fetch",auth,async function(request,result){
         const user = request.user
         const phone = request.fields.phone

         if(!phone){
           result.json({
            status:"error",
            message:"Please fill all fields."
           }) 
           return
         }

         const receiver =  await db.collection("users").findOne({
            phone:phone
          })
   
          if(receiver == null){
            result.json({
               status:"error",
               message:"Receiver not found."
            })
            return 
          }  
          
         const messages = await db.collection("messages").find({
            $or:[{
              "sender._id":user._id,
              "receiver._id":receiver._id
            },{
              "sender._id":receiver._id,
              "receiver._id":user._id
            }]
          }).sort({
            createdAt:-1
          }).toArray()

          const data = []
          for(let a=0;a<messages.length;a++){

             let message = messages[a].message
             message = crypto.AES.decrypt(message,key)
             message = message.toString(crypto.enc.Utf8)

             const messageObj = {
                _id:messages[a]._id,
                message:message,
                sender:messages[a].sender,
                receiver:messages[a].receiver,
                attachment:messages[a].attachment || "",
                extension:messages[a].extension || "",
                attachmentName:(new Date().getTime()) + "." +
                      messages[a].extension,
                createdAt:messages[a].createdAt
             }

             if(messageObj.attachment != ""){
               messageObj.attachment = apiUrl + "/" + messageObj.attachment
               
             }
             data.push(messageObj)
          }
          
          data.reverse()

          await db.collection("users").findOneAndUpdate({
            $and: [{
              _id:user._id
            },{
              "contacts.phone":phone
            }]
          },{
            $set: {
                "contacts.$.hasUnreadMessage": 0
            }
          })

          result.json({
            status:"success",
            message:"Data has been fetched.",
            data:data
          })
           
       }) 


       router.post("/send",auth,async function(request,result){
        const user = request.user
        const phone= request.fields.phone
        const message = request.fields.message
        const base64 = request.fields.base64 || ""
        const originalAttachmentName = request.fields.attachmentName
        const extension = request.fields.extension
        if(!phone || !message){
           result.json({
            status:"error",
            message:"Please fill all fields."
           }) 
           
           return 
        }
        
       const encryptedText = crypto.AES.encrypt(message,key).toString();
       // console.log(encryptedText)

       const receiver =  await db.collection("users").findOne({
         phone:phone
       })

       if(receiver == null){
         result.json({
            status:"error",
            message:"Receiver not found."
         })
         return 
       }

       let attachment = ""
       let attachmentName = ""
       
       if(base64!=""){
        attachmentName = (new Date().getTime()) + "." + 
             extension
             attachment = "uploads/" + attachmentName
             
             fileSystem.writeFile(attachment,base64,"base64",
             function(error){
                 if(error){
                  console.log(error)
                 }
             })
            
       }


       const messageObj = {
         _id:ObjectId(),
         message:encryptedText,
         sender:{
            _id:user._id,
            name:user.name,
            phone:user.phone
         },
         receiver:{
            _id:receiver._id,
            name:receiver.name,
            phone:receiver.phone
         },
         attachment:attachment,
         extension:extension,
         attachmentName:attachmentName,
         originalAttachmentName:originalAttachmentName,
         createdAt:new Date().getTime()
       }

       await db.collection("messages").insertOne(messageObj)
       messageObj.message = message

       if(attachmentName != ""){
          messageObj.attachmentName = apiUrl + "/" + attachment
       }
       await db.collection("users").findOneAndUpdate({
        $and: [{
          _id: user._id
        },{
          "contacts.phone":receiver.phone
        }]
       },{
         $set:{
          "contacts.$.updatedAt":new Date().getTime()
         }
       })

       await db.collection("users").findOneAndUpdate({
        $and: [{
          _id:receiver._id
        },{
          "contacts.phone":user.phone
        }]
       },{
        $set: {
           "contacts.$.hasUnreadMessage":1,
           "contacts.$.updatedAt": new Date().getTime()
        }
       })

       result.json({
         status:"success",
         message:"Message has been saved.",
         messageObj:messageObj
       })

       })
       app.use("/chats",router)
    }
}