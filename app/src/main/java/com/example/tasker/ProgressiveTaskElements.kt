package com.example.tasker

import android.os.Parcel
import android.os.Parcelable

data class ProgressiveTaskElements(
    var taskId:String? = null,
    var taskName:String? = null,
    var taskDescription:String? = null,
    var taskStartTime:String? = null,
    var taskEndTime:String? = null,
    var taskStartDate:String? = null,
    var taskEndDate:String? = null,
    var taskProgress:Int = 0,
    var taskPriority:Int = 0,
    var taskAttachments: ArrayList<AttachmentElements> = arrayListOf(),
    var taskReferals: ArrayList<String?> = arrayListOf(),
    var taskDuration: String? = null,
    var isNotificationEnabled:Boolean = false,
    var isTaskCompleted:Boolean = false,
) {
    companion object{
        const val ID_KEY = "taskId"
        const val NAME_KEY = "taskName"
        const val DESCRIPTION_KEY = "taskDescription"
        const val START_TIME_KEY = "taskStartTime"
        const val END_TIME_KEY = "taskEndTime"
        const val START_DATE_KEY = "taskStartDate"
        const val END_DATE_KEY = "taskEndDate"
        const val PROGRESS_KEY = "taskProgress"
        const val PRIORITY_KEY = "taskPriority"
        const val ATTACHMENT_KEY = "taskAttachments"
        const val REFERAL_KEY = "taskReferals"
        const val DURATION_KEY = "taskDuration"
        const val NOTIFICATION_KEY = "isNotificationEnabled"
        const val COMPLETED_KEY = "isTaskCompleted"
    }
}
