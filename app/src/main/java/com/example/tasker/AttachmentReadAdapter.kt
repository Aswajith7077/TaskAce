package com.example.tasker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class AttachmentReadAdapter(
    private val attachments: ArrayList<AttachmentElements>,
    private val activity: TaskViewer
) :
    RecyclerView.Adapter<AttachmentReadAdapter.AttachmentReadViewHolder>() {


    class AttachmentReadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView = itemView.findViewById<TextView>(R.id.attachment_read_text)
        val imageView = itemView.findViewById<ImageView>(R.id.attachment_read_icon)
        val layoutView = itemView.findViewById<RelativeLayout>(R.id.attachment_read_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentReadViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.attachment_read_layout, parent, false)
        return AttachmentReadViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return attachments.size
    }

    override fun onBindViewHolder(holder: AttachmentReadViewHolder, position: Int) {
        val currentIndex = attachments[position]
        holder.textView.text = currentIndex.fileName



        holder.layoutView.setOnClickListener {
            TaskViewer.showAttachment(position,activity)
        }


        when (currentIndex.fileType) {
            AttachmentElements.PDF -> {
                holder.imageView.setImageResource(R.drawable.pdf)
            }

            AttachmentElements.DOCUMENT -> {
                holder.imageView.setImageResource(R.drawable.document)
            }

            AttachmentElements.IMAGE -> {
                holder.imageView.setImageResource(R.drawable.image)
            }

            AttachmentElements.AUDIO -> {
                holder.imageView.setImageResource(R.drawable.sound_waves)
            }

            AttachmentElements.EXCEL -> {
                holder.imageView.setImageResource(R.drawable.xls)
            }

            AttachmentElements.VIDEO -> {
                holder.imageView.setImageResource(R.drawable.video_editing)
            }

            AttachmentElements.TEXT -> {
                holder.imageView.setImageResource(R.drawable.txt)
            }

            AttachmentElements.PPT -> {
                holder.imageView.setImageResource(R.drawable.ppt_file)
            }

            else -> {
                holder.imageView.setImageResource(R.drawable.blank_page)
            }
        }
    }
}