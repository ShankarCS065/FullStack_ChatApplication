package com.krashkrosh748199.GFolksy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.krashkrosh748199.GFolksy.models.LoginModel
import com.krashkrosh748199.GFolksy.utils.MySharedPreference
import com.krashkrosh748199.GFolksy.utils.Utility
import java.net.URLEncoder
import java.nio.charset.Charset

class LoginActivity : AppCompatActivity() {
    lateinit var phone: EditText
    lateinit var password:EditText
    lateinit var login:Button
    lateinit var register:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        title="Login"

        phone = findViewById(R.id.phone)
        password=findViewById(R.id.password)


        login=findViewById(R.id.btn_login)
        login.setOnClickListener {
            val queue= Volley.newRequestQueue(this)
            val url = Utility.apiUrl + "/login"
            val requestBody = "phone="+ URLEncoder.encode(phone.text.toString(),"UTF-8")+
                    "&password="+ password.text
                val stringRequest: StringRequest = object : StringRequest(Method.POST,url,Response.Listener {response ->
                     Log.i("mylog",response)
                   val loginModel:LoginModel = Gson().fromJson(response,LoginModel::class.java)
                    if (loginModel.status=="success"){
                         val preference:MySharedPreference = MySharedPreference()
                        preference.setAccessToken(this,loginModel.accessToken)
                        startActivity(Intent(this,HomeActivity::class.java))
                        finish()

                    } else{
                        Utility.showAlert(this,"Error",loginModel.message)
                    }
                },Response.ErrorListener {error ->
                    Log.i("mylog",error.toString())
                }){
                override fun getBody(): ByteArray {
                    return requestBody.toByteArray(Charset.defaultCharset())
                }
            }
            queue.add(stringRequest)
        }

        register=findViewById(R.id.register)
        register.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
            finish()
        }

    }
}