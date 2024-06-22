package com.example.tasker

data class DailyTaskElements(
    var taskId:String? = null,
    var taskName:String? = null,
    var taskDescription:String? = null,
    var taskStartTime:String? = null,
    var taskEndTime:String? = null,
    var taskStartDate:String? = null,
    var taskEndDate:String? = null,
    var taskRangeType:Int = 0,
    var attachmentList:ArrayList<AttachmentElements> = arrayListOf(),
    var referalList:ArrayList<String?> = arrayListOf(),
    var isNotificationEnabled:Boolean = false,
    var taskPriority:Int = 0,
    var taskDuration: String? = null,
    var isAlarmBeforeTenMinutesEnables:Boolean = false,
    var isSetTimer:Boolean = false,
    var isTaskCompleted:Boolean = false,
){
    companion object{
        const val TYPE_DAILY = -72919
        const val TYPE_RANGE = 253048

        const val DAILY_TYPE_KEY = "taskRangeType"
    }
}
