package com.example.tasker

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReferalReadAdapter(private val activity:TaskViewer,private val referals:ArrayList<String>):
RecyclerView.Adapter<ReferalReadAdapter.ReferalReadViewHolder>(){
    class ReferalReadViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val textView = itemView.findViewById<TextView>(R.id.referal_read_link)
        val textLayout = itemView.findViewById<RelativeLayout>(R.id.referal_read_link_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReferalReadViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.referal_read_layout,parent,false)
        return ReferalReadViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return referals.size
    }

    override fun onBindViewHolder(holder: ReferalReadViewHolder, position: Int) {
        holder.textView.text = referals[position]
        holder.textLayout.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,Uri.parse(referals[position]))
            activity.startActivity(intent)
        }
    }
}