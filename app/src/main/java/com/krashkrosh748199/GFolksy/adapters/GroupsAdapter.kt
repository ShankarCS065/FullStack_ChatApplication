package com.krashkrosh748199.GFolksy.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.krashkrosh748199.GFolksy.R
import com.krashkrosh748199.GFolksy.interfaces.RVInterface
import com.krashkrosh748199.GFolksy.models.SingleGroup
import com.krashkrosh748199.GFolksy.utils.FetchImageFromInternet

class GroupsAdapter(
                    var groups:ArrayList<SingleGroup> = ArrayList(),
                    var rvInterface: RVInterface,
                    var onAcceptInvite:RVInterface
) :RecyclerView.Adapter<GroupsAdapter.ViewHolder>() {
    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val image:ImageView = itemView.findViewById(R.id.image)
        val name:TextView = itemView.findViewById(R.id.name)
        val btnAccept:Button = itemView.findViewById(R.id.btnAccept)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view:View =   LayoutInflater.from(parent.context).inflate(R.layout.single_group,parent,false)
        return ViewHolder(view)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
     val group = groups[position]

        holder.name.text = group.name

        if (group.attachment.isEmpty()){
            holder.image.isVisible = false
        }else{
            holder.image.isVisible = true
            FetchImageFromInternet(holder.image).execute(group.attachment)
        }

        if (group.hasInvite){
            holder.btnAccept.isVisible = true
            holder.btnAccept.setOnClickListener {
                onAcceptInvite.onClick(holder.itemView)
            }
        } else{
            holder.btnAccept.isVisible = false
        }

        if (group.unread){
            holder.itemView.setBackgroundColor(R.color.purple_200)
        }

        holder.itemView.setOnClickListener{
           rvInterface.onClick(holder.itemView)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }

    @SuppressLint("NotifyDataSetChanged")
  fun setData(groups:ArrayList<SingleGroup>){
      this.groups.clear()
      this.groups.addAll(groups)
      notifyDataSetChanged()
  }
    fun getData():ArrayList<SingleGroup>{
        return this.groups
    }

    override fun getItemCount(): Int {
       return groups.size
    }
}