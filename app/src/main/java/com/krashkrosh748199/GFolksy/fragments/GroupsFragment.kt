package com.krashkrosh748199.GFolksy.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.krashkrosh748199.GFolksy.CreateGroupActivity
import com.krashkrosh748199.GFolksy.GroupDetailActivity
import com.krashkrosh748199.GFolksy.R
import com.krashkrosh748199.GFolksy.adapters.GroupsAdapter
import com.krashkrosh748199.GFolksy.interfaces.RVInterface
import com.krashkrosh748199.GFolksy.models.FetchGroupsModel
import com.krashkrosh748199.GFolksy.models.GeneralResponse
import com.krashkrosh748199.GFolksy.models.SingleGroup
import com.krashkrosh748199.GFolksy.utils.MySharedPreference
import com.krashkrosh748199.GFolksy.utils.Utility
import java.nio.charset.Charset

class GroupsFragment:Fragment(),RVInterface {

    lateinit var btnCreateGroup: FloatingActionButton
    lateinit var sharedPreference:MySharedPreference

    lateinit var rv:RecyclerView
    lateinit var adapter:GroupsAdapter

    private val onAcceptInvite:RVInterface = object : RVInterface {
        override fun onClick(view: View) {
            val position = rv.getChildAdapterPosition(view)
            val groups:ArrayList<SingleGroup> = adapter.getData()

            if (position < groups.size){
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Are you sure you want to accept this invitation?")
                    .setCancelable(false)
                    .setPositiveButton("Yes"){ dialog,id ->

                        val queue = Volley.newRequestQueue(context)
                        val url = Utility.apiUrl + "/groups/acceptInvite"
                        val requestBody = "_id=" + groups[position]._id

                        val stringRequest: StringRequest = object : StringRequest(
                            Method.POST,url,
                            Response.Listener { response ->
                                val generalResponse:GeneralResponse= Gson().fromJson(response,GeneralResponse::class.java)
                                if(generalResponse.status=="success"){
                                    context?.let {
                                        Utility.showAlert(it,"Invitation accepted",generalResponse.message)
                                    }

                                }else{
                                    context?.let {
                                        Utility.showAlert(it,"Error",generalResponse.message)
                                    }
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
                                headers["Authorization"] = "Bearer " + context?.let {
                                    sharedPreference.getAccessToken(it)
                                }
                                return headers
                            }
                        }
                        queue.add(stringRequest)

                    }
                    .setNegativeButton("No"){ dialog,id ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       return inflater.inflate(R.layout.fragment_groups,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreference = MySharedPreference()

        btnCreateGroup = view.findViewById(R.id.btnCreateGroup)
        btnCreateGroup.setOnClickListener{
            startActivity(Intent(context, CreateGroupActivity::class.java))
        }
        rv= view.findViewById(R.id.rv)
        rv.layoutManager = LinearLayoutManager(context)

        adapter = GroupsAdapter(ArrayList(),this,onAcceptInvite)
        rv.adapter = adapter

    }

    fun getData(){
        val queue = Volley.newRequestQueue(context)
        val url = Utility.apiUrl + "/groups/fetch"

        val stringRequest: StringRequest = object : StringRequest(
            Method.POST,url,
            Response.Listener { response ->
                 val fetchGroupsModel:FetchGroupsModel= Gson().fromJson(response,FetchGroupsModel::class.java)
                if(fetchGroupsModel.status=="success"){
                   adapter.setData(fetchGroupsModel.groups)
                }else{
                   context?.let {
                       Utility.showAlert(it,"Error",fetchGroupsModel.message)
                   }
                }
                },
            Response.ErrorListener { error->

            }
        ){

            override fun getHeaders(): MutableMap<String, String> {
                val headers:HashMap<String,String> = HashMap()
                headers["Authorization"] = "Bearer " + context?.let {
                    sharedPreference.getAccessToken(it)
                }
                return headers
            }
        }
        queue.add(stringRequest)
    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    override fun onClick(view: View) {
        val position = rv.getChildAdapterPosition(view)
        val groups:ArrayList<SingleGroup> = adapter.getData()

        if(position < groups.size ){
            val intent:Intent = Intent(activity, GroupDetailActivity::class.java)
            intent.putExtra("_id",groups[position]._id)
            intent.putExtra("name",groups[position].name)
            startActivity(intent)
            activity?.finish()
        }

    }
}