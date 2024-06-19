package com.example.tasker

data class SubTaskElements(
    var subTaskName: String,
    var subTaskDescription: String,
    var subTaskStartTime: String,
    var subTaskEndTime: String,
    var subTaskStartDate: String,
    var subTaskEndDate: String,
    var duration: String,
    var priority: Int,
    var isNotificationEnabled:Boolean,
    var subTaskId:String? = null
){
    companion object{
        val PRIORITY_LOW = 19283
        val PRIORITY_HIGH = -29374
        val PRIORITY_MEDIUM = -129037


        const val TASK_TYPE_PROGRESSIVE = "Progressive Task"
        const val TASK_TYPE_DAILY = "Daily Task"
        const val TASK_TYPE_NORMAL = "Normal Task"
        const val TASK_TYPE_SUB_TASK = "Sub Task"


        const val subTaskIdKey = "subTaskId"
        const val subTaskNameKey = "SUB_TASK_NAME"
        const val subTaskDescriptionKey = "SUB_TASK_DESCRIPTION"
        const val subTaskStartTimeKey = "SUB_TASK_START_TIME"
        const val subTaskEndTimeKey = "SUB_TASK_END_TIME"
        const val subTaskStartDateKey = "SUB_TASK_START_DATE"
        const val subTaskEndDateKey = "SUB_TASK_END_DATE"
        const val subTaskDurationKey = "SUB_TASK_DURATION_KEY"
        const val subTaskIsNotificationEnabled = "SUB_TASK_IS_NOTIFICATION_ENABLED"
        const val subTaskPriorityKey = "SUB_TASK_PRIORITY"
    }
}
