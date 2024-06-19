package com.example.tasker

data class NormalTaskElements(
    val taskId:String? = null,
    val taskName:String? = null,
    val taskDescription:String? = null,
    val taskStartTime:String? = null,
    val taskEndTime:String? = null,
    val taskStartDate:String? = null,
    val taskEndDate:String? = null,
    var taskPriority:Int = 0,
    val isNotificationEnabled:Boolean = false,
    val isAlarmEnabled:Boolean = false,
    var isSetTimer:Boolean = false,
    val isTaskCompleted:Boolean = false,
    var taskDuration: String? = null,
    val attachmentList:ArrayList<AttachmentElements> = arrayListOf(),
    val referalList:ArrayList<String?> = arrayListOf(),
)
