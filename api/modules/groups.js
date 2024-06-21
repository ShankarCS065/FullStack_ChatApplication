const express = require("express")
const auth = require("./auth")
const fileSystem = require("fs")
const  {ObjectId}  = require("mongodb")

module.exports = {
    init(app) { 
        const router = express.Router()

        router.post("/removeMember", auth, async function(request,result){
            const user = request.user
            const _id = request.fields._id
            const groupId = request.fields.groupId

            const group = await db.collection("groups").findOne({ 
                _id:ObjectId(_id)

            })

            if(group==null){
                result.json({
                    status:"error",
                    message:"Group does not exist"
                })
                return 
            }

            if(group.createdBy._id.toString()!= user._id.toString()){
                result.json({
                    status:"error",
                    message:"Unauthorized."
                })
                return 
            }

            let isAlreadMember = false
            for(let a=0;a<group.members.length;a++){
                  if(group.members[a].user._id.toString() == _id){
                        isAlreadMember= true
                        break
                  }
            }
            
            if(!isAlreadMember){
                result.json({
                    status:"error",
                    message:"User is not a member of this group."
                })
                return 
            }

            await db.collection("groups").findOneAndUpdate({
                _id:group._id
            },{
                $pull:{
                    members:{
                       "user._id":ObjectId(_id) 
                    }
                }
            })

            await db.collection("users").findOneAndUpdate({
                _id:ObjectId(_id)
            },{
                $pull: {
                    "groups._id":group._id
                }
            })
            result.json({
                status: "success",
                message:"Member has been removed."
            })

        })

        router.post("/detail",auth,async function(request,result){
            const user = request.user
            const _id = request.fields._id

            const group = await db.collection("groups").findOne({ 
                _id:ObjectId(_id)

            })

            if(group==null){
                result.json({
                    status:"error",
                    message:"Group does not exist"
                })
                return 
            }

            let isAlreadMember = false
            for(let a=0;a<group.members.length;a++){
                  if(group.members[a].user._id.toString() == user._id.toString()){
                        isAlreadMember= true
                        break
                  }
            }

            if(!isAlreadMember && group.createdBy._id.toString()!=user._id.toString()){
                result.json({
                    status:"error",
                    message:"Sorry,you are not a member of this group."
                })
                return 
            }

            const isAdmin = (group.createdBy._id.toString() == user._id.toString())

            result.json({
                status:"success",
                message:"Data has been fetched.",
                group:group,
                isAdmin:isAdmin
            })


        })

        router.post("/leaveGroup",auth,async function(request,result){
            const user = request.user
            const _id = request.fields._id

            const group = await db.collection("groups").findOne({ 
                _id:ObjectId(_id)

            })

            if(group==null){
                result.json({
                    status:"error",
                    message:"Group does not exist"
                })
                return 
            }

            if(group.createdBy._id.toString()==user._id.toString()){
                result.json({
                    status:"error",
                    message:"Sorry,you are an admin. Please make anyone else an admin before leaving."
                })
                return 
            }

            let isAlreadMember = false
            for(let a=0;a<group.members.length;a++){
                  if(group.members[a].user._id.toString() == user._id.toString()){
                        isAlreadMember= true
                        break
                  }
            }

            if(!isAlreadMember){
                result.json({
                    status:"error",
                    message:"Sorry,you are not a member of this group."
                })
                return 
            }

            await db.collection("groups").findOneAndUpdate({
                _id:group._id
            },{
                $pull:{
                    members:{
                        "user._id":user._id
                    }
                }
            })
            result.json({
                status:"success",
                message:"Group has been left."
            })
        })

        router.post("/acceptInvite",auth,async function(request,result){
            const user = request.user
            const _id = request.fields._id

            const group = await db.collection("groups").findOne({ 
                _id:ObjectId(_id)

            })

            if(group==null){
                result.json({
                    status:"error",
                    message:"Group does not exist"
                })
                return 
            }

            await db.collection("groups").findOneAndUpdate({
                $and: [{
                    _id:group._id
                },{
                    "members.user._id":user._id
                }]
            },{
                $set:{
                    "members.$.status": "accepted"
                }
            })

            result.json({
                status:"success",
                message:"Invitation has been accepted."
            })

        })

        router.post("/inviteMember",auth,async function(request,result) {
            const user = request.user
            const _id = request.fields._id
            const phone = request.fields.phone

            const group = await db.collection("groups").findOne({ 
                _id:ObjectId(_id)

            })

            if(group==null){
                result.json({
                    status:"error",
                    message:"Group does not exist"
                })
                return 
            }

            const member = await db.collection("users").findOne({
               phone:phone

            })

            if(member==null){
                result.json({
                    status:"error",
                    message:"User does not exist"
                })
                return 
            }

            if(group.createdBy._id.toString() != user._id.toString()){
                result.json({
                    status:"error",
                    message:"Unauthorized."
                })
                return 
            }
            let isAlreadMember = false
            for(let a=0;a<group.members.length;a++){
                  if(group.members[a].user.phone == phone){
                        isAlreadMember= true
                        break
                  }
            }

            if(group.createdBy._id.toString() == member._id.toString() || isAlreadMember){
                result.json({
                    status:"error",
                    message:"Already a member of this group."
                })
                return 
            }
          
            await db.collection("groups").findOneAndUpdate({
                _id:group._id
            },{
                $push:{
                    members:{
                        _id:ObjectId(),
                        status:"pending",
                        sentBy:{
                            _id:user._id,
                            name:user.name,
                            phone:user.phone
                        },
                        user:{
                            _id:member._id,
                            name:member.name,
                            phone:member.phone
                        },
                        createAt:new Date().getTime()
                    }
                    
                }
            })

            result.json({
                status:"success",
                message:"Invitation has been sent."
            })
 
        })

        router.post("/fetch", auth, async function (request, result) {
            const user = request.user
        
            const groups = await db.collection("groups").find({
                $or: [{
                    "createdBy._id": user._id
                }, {
                    "members.user._id": user._id
                }]
            }).sort({
                createdAt: -1
            }).toArray()
        
            const unreadGroupIds = []
            const userGroups = user.groups || []

            for (let a = 0; a < groups.length; a++) {
                if (groups[a].attachment != "") {
                    groups[a].attachment = apiUrl + "/" + groups[a].attachment
                }
        
                let hasInvite = false
                for (let b = 0; b < groups[a].members.length; b++) {
                    if (groups[a].members[b].user._id.toString() == user._id.toString() && groups[a].members[b].status == "pending") {
                        hasInvite = true
                        break
                    }
                }
                groups[a].hasInvite = hasInvite
                groups[a].unread = false

                for(let b=0;b<userGroups.length;b++){
                    if(userGroups[b]._id.toString()== groups[a]._id.toString() && userGroups[b].unread){
                        groups[a].unread = true
                        unreadGroupIds.push(groups[a]._id)
                    }
                }
            }

            await db.collection("users").updateMany({
                $and: [{
                   "groups._id":{
                    $in:unreadGroupIds
                   }
                },{
                    _id:user._id
                }]
            },{
                $set:{
                    "groups.$.unread":false
                }
            })
        
            result.json({
                status: "success",
                message: "Data has been fetched.",
                groups: groups
            })
        })
        
         router.post("/create",auth,async function (request,result)
         {
           const user = request.user
           const name = request.fields.name
           const base64 = request.fields.base64 ?? ""
           const originalAttachmentName = request.fields.attachmentName ?? ""
           const extension = request.fields.extension ?? ""

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

           const groupObj = {
            name:name,
            createdBy: {
                _id: user._id,
                name:user.name
            },
            attachment: attachment,
            extension: extension,
            originalAttachmentName:originalAttachmentName,
            attachmentName:attachmentName,
            members: [],
            createdAt: new Date().getTime()
           }

const group = await db.collection("groups").insertOne(groupObj)
     groupObj._id = group.insertedId

     if(attachment != ""){
        groupObj.attachment = apiUrl + "/" + attachment
     }

     await db.collection("users").findOneAndUpdate({
        _id:user._id
       },{
        $push: {
            groups: {
                _id: groupObj._id,
                name:name,
                unread:false
            }
        }

       })
       result.send({
        status: "success",
        message: "Group has been created",
        group:groupObj
       }) 
    
         })
        app.use("/groups",router)
    }
}

