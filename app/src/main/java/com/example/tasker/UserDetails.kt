package com.example.tasker

import android.net.Uri

data class UserDetails(
    val userId: String? = null,
    val userName: String? = null,
    val userEmail: String? = null,
    val userBio: String? = null,
    val userProfileImage: String? = null,
    val userBackgroundImage: String? = null,
    val mutual: Int = 0,
    val followers: Int = 0,
    val following: Int = 0,
    val totalTasks: Int = 0,
    val totalCompletedTasks: Int = 0,
    val todayTotalTasks: Int = 0,
    val todayCompletedTasks: Int = 0
) {
    companion object {
        const val fileStorageLink = "gs://task-ace.appspot.com"
    }
}