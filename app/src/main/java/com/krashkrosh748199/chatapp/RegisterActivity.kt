package com.krashkrosh748199.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.krashkrosh748199.chatapp.models.GeneralResponse
import com.krashkrosh748199.chatapp.utils.Utility
import java.io.UTFDataFormatException
import java.net.URLEncoder
import java.nio.charset.Charset

class RegisterActivity : AppCompatActivity() {
    lateinit var name:EditText
    lateinit var phone:EditText
    lateinit var password:EditText
    lateinit var btn_register:Button
    lateinit var login:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        title="Register"

        name = findViewById(R.id.name)
        phone = findViewById(R.id.phone)
        password = findViewById(R.id.password)
        btn_register = findViewById(R.id.btn_register)
        login=findViewById(R.id.login)

        login.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }

        btn_register.setOnClickListener {
            val queue = Volley.newRequestQueue(applicationContext)
            val url:String = Utility.apiUrl + "/registration"

            val requestBody: String = "name=" + name.text + "&phone=" + URLEncoder.encode(phone.text.toString(),"UTF-8") +
                    "&password=" + password.text

            val stringRequest: StringRequest = object : StringRequest(Method.POST,url,Response.Listener { response->

               val generalResponse:GeneralResponse= Gson().fromJson(response,GeneralResponse::class.java)
                Utility.showAlert(this,"Register",generalResponse.message)

            },Response.ErrorListener { error ->
                 Log.i("mylog",error.message.toString())
            }){
                override fun getBody(): ByteArray {
                    return requestBody.toByteArray(Charset.defaultCharset())
                }
            }
            queue.add(stringRequest)

        }




    }
}