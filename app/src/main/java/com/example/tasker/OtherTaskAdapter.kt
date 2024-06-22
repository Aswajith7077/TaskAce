package com.example.tasker

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView

class OtherTaskAdapter(private val otherTaskArrayList:ArrayList<OtherTaskElements>):
RecyclerView.Adapter<OtherTaskAdapter.OtherTaskViewHolder>(){
    class OtherTaskViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val taskHeading = itemView.findViewById<TextView>(R.id.other_task_heading)
        val expandableButton = itemView.findViewById<ImageView>(R.id.other_task_expand_button)
        val startingFrom = itemView.findViewById<TextView>(R.id.other_task_starting_from)
        val endingAt = itemView.findViewById<TextView>(R.id.other_task_ending_at)
        val duration = itemView.findViewById<TextView>(R.id.other_task_duration)
        val viewMoreButton = itemView.findViewById<AppCompatButton>(R.id.other_task_view_more_button)
    }

    data class OtherTaskElements(
        var taskId:String? = null,
        var taskName:String? = null,
        var taskStartingAt:String? = null,
        var taskEndingAt:String? = null,
        var isDailyTask:Boolean = false,
        var duration:String? = null,
    )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OtherTaskAdapter.OtherTaskViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.other_task_layout,parent,false)
        return OtherTaskViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OtherTaskAdapter.OtherTaskViewHolder, position: Int) {
        val currentIndex = otherTaskArrayList[position]
        holder.taskHeading.text = currentIndex.taskName
        if(currentIndex.isDailyTask){
            if(currentIndex.taskStartingAt != null && currentIndex.taskEndingAt!=null){
                holder.startingFrom.text = "Starting At : ${currentIndex.taskStartingAt}"
                holder.endingAt.text = "Ending At : ${currentIndex.taskEndingAt}"
                holder.duration.text = "Duration : ${currentIndex.duration}"
            }
        }else{
            holder.startingFrom.text = "Starting At : ${currentIndex.taskStartingAt}"
            holder.endingAt.text = "Ending At : ${currentIndex.taskEndingAt}"
            holder.duration.text = "Duration : ${currentIndex.duration}"
        }

        holder.expandableButton.setOnClickListener {
            holder.expandableButton.rotation = 180F
            if(holder.viewMoreButton.visibility == View.GONE){
                holder.viewMoreButton.visibility = View.VISIBLE
                holder.startingFrom.visibility = View.VISIBLE
                holder.endingAt.visibility = View.VISIBLE
                holder.duration.visibility = View.VISIBLE
            }else{
                holder.viewMoreButton.visibility = View.GONE
                holder.startingFrom.visibility = View.GONE
                holder.endingAt.visibility = View.GONE
                holder.duration.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return otherTaskArrayList.size
    }
}