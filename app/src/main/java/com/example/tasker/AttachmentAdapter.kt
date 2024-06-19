package com.example.tasker

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

class AttachmentAdapter(private val activity:CreateProgressiveTask,private var attachments: ArrayList<AttachmentElements>,private var attachmentUri:ArrayList<Uri>) :
    RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder>() {

    class AttachmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val fileName = itemView.findViewById<TextView>(R.id.attachment_file_name1)
        val fileIcon = itemView.findViewById<ImageView>(R.id.attachment_icon1)
        val removeButton = itemView.findViewById<AppCompatButton>(R.id.attachment_remove_button1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.attachment_layout, parent, false)
        return AttachmentViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return attachments.size
    }

    @SuppressLint("SetTextI18n", "IntentReset")
    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        val currentIndex = attachments[position]

        holder.fileName.setOnClickListener{
                val intent = Intent(Intent.ACTION_VIEW).apply{
                    data = attachmentUri[position]
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
            activity.startActivity(intent)

        }

        if (currentIndex.fileName!!.length > 25) {
            holder.fileName.text = currentIndex.fileName!!.substring(0,25) + "..."
        } else {
            holder.fileName.text = currentIndex.fileName
        }
        holder.fileIcon.setImageResource(currentIndex.fileType!!)

        holder.removeButton.setOnClickListener {
            CreateProgressiveTask.removeAttachmentElement(position)
        }
    }
}