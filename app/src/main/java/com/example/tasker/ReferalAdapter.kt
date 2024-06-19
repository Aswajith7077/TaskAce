package com.example.tasker

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReferalAdapter(val activity:CreateProgressiveTask,private var referalLinks:ArrayList<String?>):
    RecyclerView.Adapter<ReferalAdapter.ReferalViewHolder>() {
    class ReferalViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        val link = itemView.findViewById<TextView>(R.id.referal_link)
        val copyButton = itemView.findViewById<ImageView>(R.id.referal_copy_button)
        val removeButton = itemView.findViewById<ImageView>(R.id.referal_delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReferalViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.referal_link_layout,parent,false)
        return ReferalViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return referalLinks.size
    }

    @SuppressLint("ServiceCast")
    override fun onBindViewHolder(holder: ReferalViewHolder, position: Int) {

        holder.link.text = referalLinks[position]
        holder.link.setOnClickListener {
            val uri = Uri.parse(referalLinks[position])
            val intent = Intent(Intent.ACTION_VIEW,uri)
            activity.startActivity(intent)
        }
        holder.copyButton.setOnClickListener {
            val clipBoard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("simple text",holder.link.text)
            clipBoard.setPrimaryClip(clipData)
        }
        holder.removeButton.setOnClickListener {
            CreateProgressiveTask.removeReferalElement(position)
        }
    }

}