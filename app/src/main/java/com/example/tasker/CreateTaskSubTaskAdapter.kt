package com.example.tasker

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class CreateTaskSubTaskAdapter(private var activity: CreateProgressiveTask, private var subTaskArrayList:ArrayList<SubTaskElements>): RecyclerView.Adapter<CreateTaskSubTaskAdapter.SubTaskViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CreateTaskSubTaskAdapter.SubTaskViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.create_task_sub_task_layout,parent,false)
        return SubTaskViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: CreateTaskSubTaskAdapter.SubTaskViewHolder,
        position: Int
    ) {
        val currentIndex = subTaskArrayList[position]
        holder.taskHeader.text = currentIndex.subTaskName
        holder.duration.text = currentIndex.duration

        val startTime = LocalTime.parse(currentIndex.subTaskStartTime)
        val endTime = LocalTime.parse(currentIndex.subTaskEndTime)


        val defaultZoneId = ZoneId.systemDefault()

        val currentDate = ZonedDateTime.now(defaultZoneId).toLocalDate()
        val zonedStartDateTime = ZonedDateTime.of(currentDate, startTime, defaultZoneId)
        val zonedEndDateTime = ZonedDateTime.of(currentDate,endTime,defaultZoneId)

        val formatter = DateTimeFormatter.ofPattern("'GMT' XXX HH:mm ")
        val formattedStartTime = zonedStartDateTime.format(formatter)
        val formattedEndTime = zonedEndDateTime.format(formatter)


        holder.startFrom.text = "${currentIndex.subTaskStartDate} $formattedStartTime"
        holder.endingAt.text = "${currentIndex.subTaskEndDate} $formattedEndTime"
        holder.editButton.setOnClickListener{
            val bundle = Bundle().apply{
                putString(SubTaskElements.subTaskNameKey, currentIndex.subTaskName)
                putString(SubTaskElements.subTaskDescriptionKey, currentIndex.subTaskDescription)
                putString(SubTaskElements.subTaskStartTimeKey, currentIndex.subTaskStartTime)
                putString(SubTaskElements.subTaskEndTimeKey, currentIndex.subTaskEndTime)
                putString(SubTaskElements.subTaskStartDateKey, currentIndex.subTaskStartDate)
                putString(SubTaskElements.subTaskEndDateKey, currentIndex.subTaskEndDate)
                putString(SubTaskElements.subTaskDurationKey, currentIndex.duration)
                putInt(SubTaskElements.subTaskPriorityKey, currentIndex.priority)
                putBoolean(SubTaskElements.subTaskIsNotificationEnabled, currentIndex.isNotificationEnabled)
            }
            val subTaskBottomSheet = SubTaskBottomSheet().apply {
                arguments = bundle
            }
            subTaskBottomSheet.show(activity.supportFragmentManager, subTaskBottomSheet.tag)
        }
        if(position == 0){
            holder.removeButton.visibility = View.VISIBLE
            holder.removeButton.setOnClickListener {
                CreateProgressiveTask.removeSubTaskElement()
            }
        }
        else{
            holder.removeButton.visibility = View.INVISIBLE
        }
        when(currentIndex.priority){
            SubTaskElements.PRIORITY_LOW -> {
                holder.priorityView.text = "Low"
                holder.priorityView.setBackgroundResource(R.drawable.priority_bg1)
            }
            SubTaskElements.PRIORITY_MEDIUM -> {
                holder.priorityView.text = "Medium"
                holder.priorityView.setBackgroundResource(R.drawable.priority_bg2)
            }
            SubTaskElements.PRIORITY_HIGH -> {
                holder.priorityView.text = "High"
                holder.priorityView.setBackgroundResource(R.drawable.priority_bg3)
            }
        }
    }

    override fun getItemCount(): Int {
        return subTaskArrayList.size
    }

    class SubTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val taskHeader = itemView.findViewById<TextView>(R.id.create_task_recycler_layout_task_heading)
        val startFrom = itemView.findViewById<TextView>(R.id.create_task_recycler_layout_starting_from)
        val priorityView = itemView.findViewById<TextView>(R.id.create_task_recycler_layout_priority_button)
        val endingAt = itemView.findViewById<TextView>(R.id.create_task_recycler_layout_ending_at)
        val duration = itemView.findViewById<TextView>(R.id.create_task_recycler_layout_task_duration)
        val editButton = itemView.findViewById<AppCompatButton>(R.id.create_task_recycler_layout_edit_sub_task_button)
        val removeButton = itemView.findViewById<AppCompatButton>(R.id.create_task_recycler_layout_remove_sub_task_button)
    }
}