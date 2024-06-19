package com.example.tasker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator

class HomeProgressiveAdapter(private val activity: MainActivity,private val homeProgressiveArrayList:ArrayList<HomeProgressiveElements>):
RecyclerView.Adapter<HomeProgressiveAdapter.ProgressiveViewHolder>(){
    class ProgressiveViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val taskName = itemView.findViewById<TextView>(R.id.title)
        val taskDuration = itemView.findViewById<TextView>(R.id.due_date)
        val taskProgress = itemView.findViewById<TextView>(R.id.progress)
        val progressBar = itemView.findViewById<LinearProgressIndicator>(R.id.horizontal_progress_bar)
        val taskPriorityButton = itemView.findViewById<TextView>(R.id.priority_button)
        val layout = itemView.findViewById<RelativeLayout>(R.id.progressive_layout)
    }

    data class HomeProgressiveElements(
        var taskName:String? = null,
        var taskDuration:String? = null,
        var taskProgress:String? = null,
        var taskPriority:String? = null,
        var taskId:String? = null,
    )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProgressiveViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.progressive_task,parent,false)
        return ProgressiveViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: ProgressiveViewHolder,
        position: Int
    ) {
        val currentIndex = homeProgressiveArrayList[position]
        holder.taskName.text = currentIndex.taskName
        holder.taskDuration.text = currentIndex.taskDuration
        holder.taskProgress.text = "${currentIndex.taskProgress}%"
        holder.progressBar.progress = currentIndex.taskProgress!!.toInt()
        when(currentIndex.taskPriority){
            SubTaskElements.PRIORITY_HIGH.toString() -> {
                holder.taskPriorityButton.text = "High"
                holder.taskPriorityButton.setBackgroundResource(R.drawable.priority_bg3)
            }
            SubTaskElements.PRIORITY_MEDIUM.toString() -> {
                holder.taskPriorityButton.text = "Medium"
                holder.taskPriorityButton.setBackgroundResource(R.drawable.priority_bg2)
            }
            else -> {
                holder.taskPriorityButton.text = "Low"
                holder.taskPriorityButton.setBackgroundResource(R.drawable.priority_bg1)
            }
        }

        holder.layout.setOnClickListener {
            val bundle = Bundle().apply{
                putString(KEY,currentIndex.taskId)
                putString("TYPE", TYPE_PROGRESSIVE)
            }
            val intent = Intent(activity,TaskViewer::class.java).apply{
                putExtras(bundle)
            }
            activity.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return homeProgressiveArrayList.size
    }

    companion object {
        const val KEY = "ProgressiveTaskKeys"
        const val TYPE_PROGRESSIVE = "PROGRESSIVE TASKS"
    }

}