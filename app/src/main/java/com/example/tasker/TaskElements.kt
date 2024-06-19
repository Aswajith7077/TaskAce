package com.example.tasker

data class TaskElements(
    val taskId:String? = null,
    val taskName:String = "",
    val taskDescription:String = "",
    val taskType:Int = 0,
    val taskStartTime:String = "",
    val taskEndTime:String = "",
    val taskStartDate:String = "",
    val taskEndDate:String = "",
    val subtaskList:List<SubTaskElements> = listOf(),
    val attachmentList:List<AttachmentElements> = listOf(),
    val referalList:List<String> = listOf(),
    val isNotificationEnabled:Boolean = false,
    val isTaskCompleted:Boolean = false,
){
    companion object{
        const val TYPE_PROGRESSIVE = -8294
        const val TYPE_DAILY = 28439
        const val TYPE_NORMAL = 59763
        const val TYPE_SUB_TASK = -92842
    }
}
