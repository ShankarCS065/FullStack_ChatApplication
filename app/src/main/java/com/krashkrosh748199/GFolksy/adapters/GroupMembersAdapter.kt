package com.krashkrosh748199.GFolksy.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.krashkrosh748199.GFolksy.R
import com.krashkrosh748199.GFolksy.interfaces.RVInterface
import com.krashkrosh748199.GFolksy.models.GroupMember

class GroupMembersAdapter(
    var members:ArrayList<GroupMember>,
    var isAdmin:Boolean,
    var rvInterface: RVInterface
): RecyclerView.Adapter<GroupMembersAdapter.ViewHolder>(){
    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val phone:TextView = itemView.findViewById(R.id.phone)
        val status:TextView = itemView.findViewById(R.id.status)
        val btnRemove:Button = itemView.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val view:View = LayoutInflater.from(parent.context).inflate(R.layout.single_member,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val member = members[position]

        holder.name.text = member.user.name
        holder.phone.text = member.user.phone
        holder.status.text = member.status

        if (isAdmin){
            holder.btnRemove.isVisible = true
            holder.btnRemove.setOnClickListener {
                rvInterface.onClick(holder.itemView)
            }
        } else{
            holder.btnRemove.isVisible = false
        }
    }

    fun getData():ArrayList<GroupMember>{
        return members
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(members: ArrayList<GroupMember>, isAdmin: Boolean){
        this.members.clear()
        this.members.addAll(members)
        this.isAdmin = isAdmin
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
      return members.size
    }
}