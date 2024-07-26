package com.krashkrosh748199.GFolksy

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.krashkrosh748199.GFolksy.adapters.GroupMembersAdapter
import com.krashkrosh748199.GFolksy.interfaces.RVInterface
import com.krashkrosh748199.GFolksy.models.GeneralResponse
import com.krashkrosh748199.GFolksy.models.GroupDetailResponse
import com.krashkrosh748199.GFolksy.models.GroupMember
import com.krashkrosh748199.GFolksy.utils.MySharedPreference
import com.krashkrosh748199.GFolksy.utils.Utility
import java.nio.charset.Charset

class GroupMembersActivity : AppCompatActivity(), RVInterface {
    var _id:String = ""
    var name:String = ""
    var isAdmin:Boolean = false

    lateinit var btnAddMember:FloatingActionButton
    lateinit var sharedPreference:MySharedPreference

    lateinit var rv:RecyclerView
    lateinit var adapter:GroupMembersAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_members)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        // Enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        sharedPreference= MySharedPreference()
        rv = findViewById(R.id.rv)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = GroupMembersAdapter(ArrayList(),false,this)
        rv.adapter = adapter

        if(intent.hasExtra("_id")&& intent.hasExtra("name")){
            _id = intent.getStringExtra("_id").toString()
            name = intent.getStringExtra("name").toString()

            title = name

            btnAddMember = findViewById(R.id.btnAddMember)

            btnAddMember.setOnClickListener{
                val intent: Intent = Intent(this,AddGroupMemberActivity::class.java)
                intent.putExtra("_id",_id)
                intent.putExtra("name",name)
                startActivity(intent)
            }
        }

    }
    fun getData(){

        val queue = Volley.newRequestQueue(this)
        val url = Utility.apiUrl + "/groups/detail"
        val requestBody = "_id=" + _id

        val stringRequest: StringRequest = object : StringRequest(
            Method.POST,url,
            Response.Listener { response ->
                val groupDetailResponse: GroupDetailResponse = Gson().fromJson(response,
                    GroupDetailResponse::class.java)
                if(groupDetailResponse.status=="success"){
                    isAdmin = groupDetailResponse.isAdmin
                    adapter.setData(groupDetailResponse.group.members,isAdmin)
                }else{
                    Utility.showAlert(this,"Error",groupDetailResponse.message)
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    override fun onClick(view: View) {
       val position = rv.getChildAdapterPosition(view)
        val members:ArrayList<GroupMember> = adapter.getData()

        if (position < members.size){
           val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to remove this member ?")
                .setCancelable(false)
                .setPositiveButton("Yes"){ dialog,id->
                    val queue = Volley.newRequestQueue(this)
                    val url = Utility.apiUrl + "/groups/removeMember"
                    val requestBody = "_id=" + members[position].user._id + "&groupId=" + _id

                    val stringRequest: StringRequest = object : StringRequest(
                        Method.POST,url,
                        Response.Listener { response ->
                            val generalResponse: GeneralResponse = Gson().fromJson(response,
                                GeneralResponse::class.java)
                            if(generalResponse.status=="success"){
                                   val newMembers:ArrayList<GroupMember> = ArrayList()
                                for (member in members){
                                    if (member.user._id != members[position].user._id){
                                         newMembers.add(member)
                                    }
                                }
                                adapter.setData(newMembers,isAdmin)
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
            val alert = builder.create()
            alert.show()
        }
    }
}