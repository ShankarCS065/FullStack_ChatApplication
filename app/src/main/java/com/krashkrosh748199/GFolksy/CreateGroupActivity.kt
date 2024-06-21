package com.krashkrosh748199.GFolksy

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.krashkrosh748199.GFolksy.models.GeneralResponse
import com.krashkrosh748199.GFolksy.utils.MySharedPreference
import com.krashkrosh748199.GFolksy.utils.Utility
import java.nio.charset.Charset
import java.util.Base64

class CreateGroupActivity : AppCompatActivity() {

    lateinit var groupName:EditText
    lateinit var btnSetImage:Button
    lateinit var image:ImageView
    lateinit var btnCreateGroup:Button

    var base64:String = ""
    var attachmentName:String = ""
    var extension:String = ""

    lateinit var sharedPreference: MySharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        sharedPreference = MySharedPreference()

        groupName = findViewById(R.id.groupName)
        btnSetImage =  findViewById(R.id.btnSetImage)
        image = findViewById(R.id.image)
        btnCreateGroup = findViewById(R.id.btnCreateGroup)

        btnSetImage.setOnClickListener {
            val intent:Intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,565)

        }

        btnCreateGroup.setOnClickListener {
           val queue = Volley.newRequestQueue(this)
            val url = Utility.apiUrl + "/groups/create"
            val requestBody = "name=" + groupName.text + "&base64=" + base64 + "&attachmentName=" + attachmentName +
                    "&extension=" + extension

            val stringRequest:StringRequest = object : StringRequest(
                Method.POST,url,
                Response.Listener { response ->
                    val responseModel:GeneralResponse = Gson().fromJson(response,GeneralResponse::class.java)
                     if (responseModel.status == "success" ){
                        groupName.setText("")
                        base64 = ""
                        attachmentName = ""
                        extension = ""

                    }
                    Utility.showAlert(this,"Create group",responseModel.message)
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
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == 565 ){
            val uri:Uri? = data?.data
            image.setImageURI(uri)

            base64 = ""
            try {
                val bytes = uri?.let {
                    contentResolver.openInputStream(it)?.readBytes()
                }
                base64 = Base64.getUrlEncoder().encodeToString(bytes)
            }
            catch (exp:Exception){

            }
            attachmentName = uri?.let {
                Utility.getFileName(contentResolver,it)
            }.toString()

            extension = uri?.let {
                Utility.getExtension(contentResolver,it)
            }.toString()

        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}