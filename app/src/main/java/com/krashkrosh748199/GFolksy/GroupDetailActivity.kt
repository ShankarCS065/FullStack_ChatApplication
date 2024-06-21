package com.krashkrosh748199.GFolksy

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import com.krashkrosh748199.GFolksy.models.GeneralResponse
import com.krashkrosh748199.GFolksy.models.Message
import com.krashkrosh748199.GFolksy.models.SendMessageModel
import com.krashkrosh748199.GFolksy.utils.DownloadFileFromHTTP
import com.krashkrosh748199.GFolksy.utils.MySharedPreference
import com.krashkrosh748199.GFolksy.utils.Utility
import java.io.IOException
import java.nio.charset.Charset

class GroupDetailActivity : AppCompatActivity(),AttachmentInterface {
    var _id:String = ""
    var name:String = ""
    lateinit var sharedPreference:MySharedPreference

    lateinit var message: EditText
    lateinit var btnSend: Button
    lateinit var messages:ArrayList<Message>

    lateinit var rv: RecyclerView
    lateinit var adapter: ChatsAdapter

    lateinit var imgAttachment: ImageView

    var base64 :String = ""
    var attachmentName:String = ""
    var extension:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_detail)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        sharedPreference = MySharedPreference()

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

        if(intent.hasExtra("_id")&& intent.hasExtra("name")){
           _id = intent.getStringExtra("_id").toString()
            name = intent.getStringExtra("name").toString()

            getData()

            title =  name

            btnSend.setOnClickListener {
                btnSend.isEnabled = false

                val queue = Volley.newRequestQueue(this)
                val url = Utility.apiUrl + "/chats/sendInGroup"

                val requestBody = "message=" + message.text + "&_id=" + _id +
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

                        val sendMessageModel: SendMessageModel = Gson().fromJson(response,
                            SendMessageModel::class.java)
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
            val data: Uri? = data?.data
            base64 = ""
            try {
                val bytes = data?.let {
                    contentResolver.openInputStream(it)?.readBytes()
                }
                base64 = Base64.encodeToString(bytes, Base64.URL_SAFE)

            } catch (exp: IOException){

            }
            data?.let {
                attachmentName = Utility.getFileName(contentResolver,it)
            }
            data?.let {
                extension = Utility.getExtension(contentResolver,it)
            }
        }
    }

    fun getData(){
        val queue = Volley.newRequestQueue(this)
        val url = Utility.apiUrl + "/chats/fetchGroup"
        val requestBody = "_id=" + _id

        val stringRequest:StringRequest = object : StringRequest(
            Method.POST,url,
            Response.Listener {response ->
                // Log.i("mylog",response)
                val fetchMessagesModel: FetchMessagesModel = Gson().fromJson(response,
                    FetchMessagesModel::class.java)

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


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.members){
            val intent:Intent = Intent(this,GroupMembersActivity::class.java)
            intent.putExtra("_id",_id)
            intent.putExtra("name",name)
            startActivity(intent)
            return true
        }
        if (item.itemId==R.id.leave){
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to leave this group ?")
                .setCancelable(false)
                .setPositiveButton("Yes"){dialog,id->

                    val queue = Volley.newRequestQueue(this)
                    val url = Utility.apiUrl + "/groups/leaveGroup"
                    val requestBody = "_id=" + _id

                    val stringRequest: StringRequest = object : StringRequest(
                        Method.POST,url,
                        Response.Listener { response ->
                            val generalResponse: GeneralResponse = Gson().fromJson(response,
                                GeneralResponse::class.java)
                            if(generalResponse.status=="success"){
                                    Utility.showAlert(this,"Leave group",generalResponse.message)
                            }else{
                                    Utility.showAlert(this,"Error",generalResponse.message)
                            }
                        },
                        Response.ErrorListener { error->

                        }
                    ){

                        override fun getBody(): ByteArray {
                            return requestBody.toByteArray(Charset.defaultCharset())
                        }

                        override fun getHeaders(): MutableMap<String, String> {
                            val headers:HashMap<String,String> = HashMap()
                            headers["Authorization"] = "Bearer " + sharedPreference.getAccessToken(applicationContext)
                            return headers
                        }
                    }
                    queue.add(stringRequest)
                }
                .setNegativeButton("No"){dialog,id->
                    dialog.dismiss()
                }
            val alert=builder.create()
            alert.show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.groups_menu,menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
       startActivity(Intent(this,HomeActivity::class.java))
        finish()
        return true
    }

    override fun onDownload(view: View) {
        val position = rv.getChildAdapterPosition(view)
        val messages:ArrayList<Message> = adapter.getData()

        if(messages.size > position){

            Dexter.withContext(this)
                .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object: PermissionListener {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        DownloadFileFromHTTP(
                            messages[position].attachment,
                            messages[position].attachmentName,
                            this@GroupDetailActivity
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
        Toast.makeText(this,path, Toast.LENGTH_LONG).show()
    }
}