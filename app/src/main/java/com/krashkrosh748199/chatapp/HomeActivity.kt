package com.krashkrosh748199.chatapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.krashkrosh748199.chatapp.adapters.HomeTabAdapter
import com.krashkrosh748199.chatapp.models.GeneralResponse
import com.krashkrosh748199.chatapp.models.GetUserModel
import com.krashkrosh748199.chatapp.models.User
import com.krashkrosh748199.chatapp.utils.MySharedPreference
import com.krashkrosh748199.chatapp.utils.Utility
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.Charset

class HomeActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {

    lateinit var drawerLayout: DrawerLayout
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    lateinit var navigationView:NavigationView

    lateinit var sharedPreference: MySharedPreference
    lateinit var user:User

    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        sharedPreference = MySharedPreference()

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        tabLayout.addTab(
            tabLayout.newTab().setText("Contacts")
        )

        val adapter= HomeTabAdapter(this,supportFragmentManager,tabLayout.tabCount)
        viewPager.adapter = adapter

        viewPager.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(tabLayout)
        )
        tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener{
                override fun onTabSelected(tab: TabLayout.Tab) {

                    viewPager.currentItem = tab.position
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {

                }

                override fun onTabReselected(tab: TabLayout.Tab?) {

                }

            }
        )

        drawerLayout=findViewById(R.id.drawerLayoutHome)
        actionBarDrawerToggle= ActionBarDrawerToggle(
            this,drawerLayout,
            R.string.nav_open,R.string.nav_close
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        navigationView = findViewById(R.id.nav_home)

        navigationView.setNavigationItemSelectedListener(this)

        getData()

    }
    fun getData(){
        val queue= Volley.newRequestQueue(this)
        val url= Utility.apiUrl + "/getUser"
        
        val stringRequest:StringRequest = object :StringRequest(
            Method.POST,url,
            Response.Listener {response ->
                val getUserModel:GetUserModel= Gson().fromJson(response,GetUserModel::class.java)
                if (getUserModel.status == "success"){
                       user = getUserModel.user
                    val headerView:View = navigationView.getHeaderView(0)
                    val name:TextView = headerView.findViewById(R.id.name)
                    name.text = user.name

                    val phone:TextView = headerView.findViewById(R.id.phone)
                    phone.text = user.phone


                    if (!sharedPreference.hasSavedContacts(this)) {
                        getContactsPermission()
                   }

                }else{
                    Utility.showAlert(this,"Error",getUserModel.message)
                }

            },Response.ErrorListener {error ->

            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers:HashMap<String,String> = HashMap()
                headers["Authorization"] = "Bearer " +sharedPreference.getAccessToken(applicationContext)
                return headers
            }
        }
        queue.add(stringRequest)

    }

    fun getContactsPermission(){
      val permission = ContextCompat.checkSelfPermission(this,
           android.Manifest.permission.READ_CONTACTS)
        if (permission == PackageManager.PERMISSION_GRANTED){
             getContacts()
         }else{
            ActivityCompat.requestPermissions(this,
                   arrayOf(android.Manifest.permission.READ_CONTACTS),565)
        }
    }

    @SuppressLint("Range")
    fun getContacts(){
        val contacts:JSONArray = JSONArray()

       val phones = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        while (phones?.moveToNext() == true){
            val name:String= phones.getString(
                phones.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                )
            )

            val phone:String = phones.getString(
                phones.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )
            )

            val obj:JSONObject = JSONObject()
            obj.put("name",name)
            obj.put("phone",phone)
            contacts.put(obj)
        }

        phones?.close()

        Log.i("mylog",contacts.toString())

        val queue = Volley.newRequestQueue(this)
        val url = Utility.apiUrl + "/contacts/save"
        val requestBody = "contacts=" + URLEncoder.encode(contacts.toString(),"UTF-8")

        val stringRequest:StringRequest =object : StringRequest(
            Method.POST,url,
            Response.Listener {response ->
                val generalResponse:GeneralResponse =
                    Gson().fromJson(response,GeneralResponse::class.java)
                Utility.showAlert(this,"Save contacts",generalResponse.message)

                sharedPreference.setContactsSave(this)


            },Response.ErrorListener {error ->

            }
               ){
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode==565){
            if (grantResults.isEmpty() || grantResults[0]!=PackageManager.PERMISSION_GRANTED){

            }else{
                getContacts()
            }
        }
    }
    fun doLogout(){
            val queue = Volley.newRequestQueue(this)
            val url = Utility.apiUrl + "/logout"

                val stringRequest:StringRequest =
                    object : StringRequest(Method.POST,url,Response.Listener {response ->
                       val  generalResponse:GeneralResponse = Gson().fromJson(response,GeneralResponse::class.java)
                       Utility.showAlert(this,"Logout",generalResponse.message, Runnable {
                        if(generalResponse.status =="success"){
                            sharedPreference.removeAccessToken(this)
                            startActivity(Intent(this,LoginActivity::class.java))
                            finish()
                        }
                    })
                }, Response.ErrorListener {error ->

                }){
                override fun getHeaders(): MutableMap<String, String> {
                    val headers:HashMap<String,String> = HashMap()
                    headers["Authorization"] = "Bearer " +sharedPreference.getAccessToken(applicationContext)
                    return headers
                }

            }
            queue.add(stringRequest)
        }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout){
           doLogout()
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

