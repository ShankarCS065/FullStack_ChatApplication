package com.krashkrosh748199.GFolksy

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.krashkrosh748199.GFolksy.adapters.ChatsAdapter
import com.krashkrosh748199.GFolksy.interfaces.AttachmentInterface
import com.krashkrosh748199.GFolksy.models.FetchMessagesModel
import com.krashkrosh748199.GFolksy.models.Message
import com.krashkrosh748199.GFolksy.models.SendMessageModel
import com.krashkrosh748199.GFolksy.utils.DownloadFileFromHTTP
import com.krashkrosh748199.GFolksy.utils.MySharedPreference
import com.krashkrosh748199.GFolksy.utils.Utility
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.Charset

class ChatActivity : AppCompatActivity(), AttachmentInterface {
    lateinit var message: EditText
    lateinit var btnSend:Button
    var phone:String =""
    lateinit var  sharedPreference: MySharedPreference
    lateinit var messages:ArrayList<Message>

    lateinit var rv:RecyclerView
    lateinit var adapter:ChatsAdapter

    lateinit var imgAttachment:ImageView

    var base64 :String = ""
    var attachmentName:String = ""
    var extension:String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        imgAttachment = findViewById(R.id.imgAttachment)
        imgAttachment.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "*/*"
            startActivityForResult(intent,565)
        }

        messages = ArrayList()
        sharedPreference = MySharedPreference()

        message = findViewById(R.id.message)
        btnSend=findViewById(R.id.btnSend)

        rv = findViewById(R.id.rv)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = ChatsAdapter(ArrayList(),this)
         rv.adapter = adapter

        if (intent.hasExtra("phone")
            && intent.hasExtra("name")){
           phone = intent.getStringExtra("phone").toString()
            phone = URLEncoder.encode(phone,"UTF-8")

            getData()

            title = intent.getStringExtra("name").toString()

            btnSend.setOnClickListener {
                btnSend.isEnabled = false

                val queue = Volley.newRequestQueue(this)
                val url = Utility.apiUrl + "/chats/send"

                val requestBody = "message=" + message.text + "&phone=" +phone+
                        "&base64=" + base64 + "&attachmentName=" + attachmentName + "&extension=" + extension

                val stringRequest:StringRequest = object : StringRequest(
                    Method.POST,url,
                    Response.Listener {response ->
                     // Log.i("mylog",response)

                        btnSend.isEnabled = true

                        message.setText("")
                        base64=""
                        attachmentName=""
                        extension=""

                        val sendMessageModel:SendMessageModel = Gson().fromJson(response,SendMessageModel::class.java)
                        if (sendMessageModel.status == "success"){

                            adapter.appendData(sendMessageModel.messageObj)

                        } else{
                            Utility.showAlert(this,"Error",sendMessageModel.message)
                        }
                    },
                    Response.ErrorListener {error->
                      Log.i("mylog",error.toString())
                    }
                ){
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers:HashMap<String,String> = HashMap()
                        headers["Authorization"] = "Bearer " + sharedPreference.getAccessToken(applicationContext)
                        return  headers
                    }

                    override fun getBody(): ByteArray {
                        return requestBody.toByteArray(Charset.defaultCharset())
                    }
                }
                queue.add(stringRequest)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == 565 ){
            val data:Uri? = data?.data
                 base64 = ""
            try {
               val bytes = data?.let {
                    contentResolver.openInputStream(it)?.readBytes()
                }
                base64 = Base64.encodeToString(bytes,Base64.URL_SAFE)

            } catch (exp:IOException){

            }
            data?.let {
                attachmentName = Utility.getFileName(contentResolver,it)
            }
            data?.let {
               extension = Utility.getExtension(contentResolver,it)
            }
//            Log.i("mylog",base64)
//            Log.i("mylog",attachmentName)
//            Log.i("mylog",extension)

        }
    }

    fun getData(){
       val queue = Volley.newRequestQueue(this)
        val url = Utility.apiUrl + "/chats/fetch"
        val requestBody = "phone=" + phone

        val stringRequest:StringRequest = object : StringRequest(
            Method.POST,url,
            Response.Listener {response ->  
               // Log.i("mylog",response)
                val fetchMessagesModel:FetchMessagesModel = Gson().fromJson(response,FetchMessagesModel::class.java)

                if (fetchMessagesModel.status == "success"){

                    messages = fetchMessagesModel.data
                    adapter.setData(messages)

                }else{
                  Utility.showAlert(this,"Error",fetchMessagesModel.message)
                }
            },
            Response.ErrorListener { error ->  

            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers:HashMap<String,String> = HashMap()
                headers["Authorization"] = "Bearer " + sharedPreference.getAccessToken(applicationContext)
                return headers
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray(Charset.defaultCharset())
            }
        }
        queue.add(stringRequest)
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDownload(view: View) {
       val position = rv.getChildAdapterPosition(view)
        val messages:ArrayList<Message> = adapter.getData()

        if(messages.size > position){

            Dexter.withContext(this)
                .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object: PermissionListener{
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        DownloadFileFromHTTP(
                            messages[position].attachment,
                            messages[position].attachmentName,
                            this@ChatActivity
                        ).execute()
                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: PermissionRequest?,
                        p1: PermissionToken?
                    ) {

                    }

                }).check()


        }
    }

    override fun onDownloaded(path: String) {
        Toast.makeText(this,path,Toast.LENGTH_LONG).show()
    }
}