const express = require("express")
const auth = require("./auth")
module.exports = {
    init(app){
        const router = express.Router()

        router.post("/fetch",auth,async function(request,result){
            const user = request.user
            const contacts = user.contacts || []
            
            contacts.sort(function(a,b){
                const keyA = typeof a.updatedAt === "undefined" ? 0 :
                      new Date(a.updatedAt)
                const keyB = typeof b.updatedAt === "undefined" ? 0 :
                      new Date(b.updatedAt)

                      if(keyA < keyB){
                        return 1
                      }
                      if(keyA > keyB){
                        return -1
                      }
                      return 0
            })

            result.json({
                status: "success",
                message:"Data has been fetched.",
                contacts:contacts
            })

        })

        router.post("/save",auth,async function (request,result){
            const user = request.user
            const contacts = JSON.parse(request.fields.contacts)

            await db.collection("users").findOneAndUpdate({
                _id:user._id
            },{
                $set:{
                    contacts:contacts
                }
            } )
            result.json({
                status:"success",
                message:"Contacts has been saved."
            })

        })
          app.use("/contacts",router)
    }
}