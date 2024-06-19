package com.example.tasker

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tasker.databinding.ActivityTaskViewerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class TaskViewer : AppCompatActivity() {


    private lateinit var taskViewerBinding : ActivityTaskViewerBinding
    private lateinit var database: DatabaseReference
    private var storageReference = Firebase.storage.reference
    private lateinit var auth:FirebaseAuth
    private lateinit var taskId:String
    private lateinit var taskType:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        taskViewerBinding = ActivityTaskViewerBinding.inflate(layoutInflater)
        database = Firebase.database.reference
        auth = Firebase.auth

        taskViewerBinding.taskViewerReferalRecyclerView.layoutManager = LinearLayoutManager(this)

        setContentView(taskViewerBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bundle = intent.extras!!
        taskId = bundle.getString(HomeProgressiveAdapter.KEY)!!
        taskType = bundle.getString("TYPE")!!



        val listener = object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val temp = snapshot.value as HashMap<*,*>
                if(temp.containsKey(taskId)){
                    initialize(temp[taskId] as HashMap<*,*>)
                }

            }

            override fun onCancelled(error: DatabaseError) {}
        }

        database.child(SubTaskElements.TASK_TYPE_PROGRESSIVE).child(auth.currentUser!!.uid).addValueEventListener(listener)



        taskViewerBinding.taskViewerBackButton.setOnClickListener {
            finish()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun initialize(hashMap: HashMap<*, *>) {
        val taskName = hashMap[ProgressiveTaskElements.NAME_KEY].toString()
        var taskDescription:String? = null
        if(hashMap.contains(ProgressiveTaskElements.DESCRIPTION_KEY))
            taskDescription = hashMap[ProgressiveTaskElements.DESCRIPTION_KEY].toString()

        val startedAt = hashMap[ProgressiveTaskElements.START_DATE_KEY].toString() + " " + hashMap[ProgressiveTaskElements.START_TIME_KEY].toString()
        val endingAt = hashMap[ProgressiveTaskElements.END_DATE_KEY].toString() + " " + hashMap[ProgressiveTaskElements.END_TIME_KEY].toString()
        val duration = hashMap[ProgressiveTaskElements.DURATION_KEY].toString()
        val priority = hashMap[ProgressiveTaskElements.PRIORITY_KEY].toString()
        val progress = hashMap[ProgressiveTaskElements.PROGRESS_KEY].toString()

        taskViewerBinding.taskViewerTaskTitle.text = taskName
        if(taskDescription == null){
            taskViewerBinding.taskViewerDescriptionText.visibility = View.GONE
            taskViewerBinding.taskViewerDescriptionHeading.visibility = View.GONE
        }else{
            taskViewerBinding.taskViewerDescriptionText.visibility = View.VISIBLE
            taskViewerBinding.taskViewerDescriptionHeading.visibility = View.VISIBLE
            taskViewerBinding.taskViewerDescriptionText.text = taskDescription
        }

        taskViewerBinding.taskViewerStartedOnText.text = startedAt
        taskViewerBinding.taskViewerEndingAtText.text = endingAt
        taskViewerBinding.taskViewerDurationText.text = duration


        taskViewerBinding.taskViewerLastDate.text = hashMap[ProgressiveTaskElements.END_DATE_KEY].toString()
        taskViewerBinding.taskViewerLastTime.text = hashMap[ProgressiveTaskElements.END_TIME_KEY].toString()

//        loadAttachments()

        when(priority.toInt()){
            SubTaskElements.PRIORITY_LOW -> {
                taskViewerBinding.taskViewerPriorityButton.setBackgroundResource(R.drawable.priority_bg1)
                taskViewerBinding.taskViewerPriorityButton.text = "Low"
            }
            SubTaskElements.PRIORITY_MEDIUM -> {
                taskViewerBinding.taskViewerPriorityButton.setBackgroundResource(R.drawable.priority_bg2)
                taskViewerBinding.taskViewerPriorityButton.text = "Medium"
            }
            else -> {
                taskViewerBinding.taskViewerPriorityButton.setBackgroundResource(R.drawable.priority_bg3)
                taskViewerBinding.taskViewerPriorityButton.text = "High"
            }
        }

        taskViewerBinding.taskViewerProgressBar.progress = progress.toInt()
        taskViewerBinding.taskViewerProgressText.text = "$progress%"


        taskViewerBinding.taskViewerEditButton.setOnClickListener {
            val bundle = Bundle().apply{
                putString(ProgressiveTaskElements.ID_KEY,taskId)
                putString("TYPE",taskType)
            }
            val intent = Intent(this,CreateProgressiveTask::class.java).apply{
                putExtras(bundle)
            }
            startActivity(intent)
            finish()
        }


        if(hashMap.containsKey(ProgressiveTaskElements.ATTACHMENT_KEY)){
            val attachmentList = hashMap[ProgressiveTaskElements.ATTACHMENT_KEY] as ArrayList<AttachmentElements>
            taskViewerBinding.taskViewerAttachmentRecyclerView.adapter = AttachmentReadAdapter(attachmentList,this)
        }


        if(hashMap.containsKey(ProgressiveTaskElements.REFERAL_KEY)){
            val referalArrayList = hashMap[ProgressiveTaskElements.REFERAL_KEY] as ArrayList<String>
            taskViewerBinding.taskViewerReferalRecyclerView.adapter = ReferalReadAdapter(this,referalArrayList)
            taskViewerBinding.taskViewerReferalHeading.visibility = View.VISIBLE
            taskViewerBinding.taskViewerAvailablityText.visibility = View.GONE
        }else{
            taskViewerBinding.taskViewerAvailablityText.text = "No Referals Available"
            taskViewerBinding.taskViewerReferalHeading.visibility = View.GONE
            taskViewerBinding.taskViewerAvailablityText.visibility = View.VISIBLE
        }

    }


    companion object{
        private val attachmentUris:ArrayList<Uri> = arrayListOf()
        fun showAttachment(value:Int,activity:TaskViewer){
            val intent = Intent().apply{
                action = Intent.ACTION_VIEW
                data = attachmentUris[value]
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            activity.startActivity(intent)
        }
    }

}