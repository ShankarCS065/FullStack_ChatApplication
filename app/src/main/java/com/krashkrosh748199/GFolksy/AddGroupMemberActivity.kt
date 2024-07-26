package com.krashkrosh748199.GFolksy

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.krashkrosh748199.GFolksy.models.GeneralResponse
import com.krashkrosh748199.GFolksy.utils.MySharedPreference
import com.krashkrosh748199.GFolksy.utils.Utility
import java.net.URLEncoder
import java.nio.charset.Charset

class AddGroupMemberActivity : AppCompatActivity() {
    var _id:String = ""
    var name:String = ""

     lateinit var phone:EditText
     lateinit var btnInviteMember:Button
     lateinit var sharedPreference:MySharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_group_member)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        sharedPreference = MySharedPreference()

        if(intent.hasExtra("_id")&& intent.hasExtra("name")){
            _id = intent.getStringExtra("_id").toString()
            name = intent.getStringExtra("name").toString()

            title =  name

            phone = findViewById(R.id.phone)
            btnInviteMember = findViewById(R.id.btnInviteMember)

            btnInviteMember.setOnClickListener {
                val queue = Volley.newRequestQueue(this)
                val url = Utility.apiUrl + "/groups/inviteMember"
                val requestBody = "_id=" + _id + "&phone=" + URLEncoder.encode(phone.text.toString(),"UTF-8")

                val stringRequest: StringRequest = object : StringRequest(
                    Method.POST,url,
                    Response.Listener { response ->
                        val generalResponse:GeneralResponse= Gson().fromJson(response,GeneralResponse::class.java)
                        Utility.showAlert(this,"Invite member",generalResponse.message)
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

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}