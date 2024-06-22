package com.example.tasker

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.renderscript.Sampler.Value
import android.text.Editable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tasker.databinding.ActivityCreateProgressiveTaskBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

@Suppress("CAST_NEVER_SUCCEEDS")
class CreateProgressiveTask(private var currentSelectedItem: Int = 0) : AppCompatActivity(),
    DataUpdateCallback {

    private var startTime: String? = null
    private var endTime: String? = null
    private var startDate: String? = null
    private var endDate: String? = null
    private lateinit var taskArrayAdapter: ArrayAdapter<String>
    private lateinit var taskDropDownArray: ArrayList<String>
    private lateinit var dailyArrayAdapter: ArrayAdapter<String>
    private lateinit var dailyDropDownArray: ArrayList<String>
    private var descriptionEnabled: Boolean = false
    private var prioritySelected: Int = 0
    private var noError = false
    private var currentDailyTaskSelected: Int = 1

    private var isEditTask:Boolean = false
    private var taskId:String? = null

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var storageReference = Firebase.storage(UserDetails.fileStorageLink).reference

    private lateinit var createProgressiveTaskBinding: ActivityCreateProgressiveTaskBinding

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        database = Firebase.database.reference
        auth = Firebase.auth
        createProgressiveTaskBinding = ActivityCreateProgressiveTaskBinding.inflate(layoutInflater)
        setContentView(createProgressiveTaskBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }




        //Initializations

        taskDropDownArray = arrayListOf(
            SubTaskElements.TASK_TYPE_PROGRESSIVE,
            SubTaskElements.TASK_TYPE_DAILY,
            SubTaskElements.TASK_TYPE_NORMAL,
        )
        dailyDropDownArray = arrayListOf(
            "Daily",
            "Date Range"
        )
        taskArrayAdapter =
            ArrayAdapter<String>(this, R.layout.drop_down_layout, R.id.textView, taskDropDownArray)
        dailyArrayAdapter =
            ArrayAdapter<String>(this, R.layout.drop_down_layout, R.id.textView, dailyDropDownArray)

        createProgressiveTaskBinding.taskDropDown.adapter = taskArrayAdapter
        createProgressiveTaskBinding.taskDropDown.setSelection(currentSelectedItem)

        createProgressiveTaskBinding.dialyDropDown.adapter = dailyArrayAdapter
        createProgressiveTaskBinding.dialyDropDown.setSelection(currentDailyTaskSelected)

        createProgressiveTaskBinding.startDateText.text = startDate
        createProgressiveTaskBinding.endDateText.text = endDate
        createProgressiveTaskBinding.subTaskRecyclerView.layoutManager = LinearLayoutManager(this)
        createProgressiveTaskBinding.attachmentsRecyclerView.layoutManager =
            GridLayoutManager(this, 2)
        createProgressiveTaskBinding.referalsRecyclerView.layoutManager = LinearLayoutManager(this)
        referalArrayList = arrayListOf()
        subTaskArrayList = arrayListOf()
        attachmentUris = arrayListOf()
        attachmentArrayList = arrayListOf()

        val bundle = intent.extras
        if (bundle != null){
            taskId = bundle.getString(ProgressiveTaskElements.ID_KEY)
            val taskType = bundle.getString("TYPE")

            createProgressiveTaskBinding.createTaskHeading.text = "Edit Task"
            isEditTask = true
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val temp = snapshot.value!! as HashMap<*,*>
                    if(temp.containsKey(ProgressiveTaskElements.ID_KEY) && temp[ProgressiveTaskElements.ID_KEY]==taskId) {
                        initialize(temp)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            }


            database.child(SubTaskElements.TASK_TYPE_PROGRESSIVE).child(auth.currentUser!!.uid).child(taskId!!).addValueEventListener(listener)


        }


        // Function Calls

        setDataCallback(this)


        // Event Handlers
        createProgressiveTaskBinding.startTimeButton.setOnClickListener {
            timePicker(createProgressiveTaskBinding.startTimeButton, true)

        }

        createProgressiveTaskBinding.endTimeButton.setOnClickListener {
            timePicker(createProgressiveTaskBinding.endTimeButton, false)
        }

        createProgressiveTaskBinding.subTaskRecyclerView.setOnClickListener {
            if (isElementRemoved)
                createProgressiveTaskBinding.subTaskRecyclerView.adapter =
                    CreateTaskSubTaskAdapter(this, subTaskArrayList)
        }

        createProgressiveTaskBinding.taskDropDown.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    currentSelectedItem = position
                    when (position) {
                        0 -> {

                            createProgressiveTaskBinding.dateLayout.visibility = View.VISIBLE
                            createProgressiveTaskBinding.setDateButton.visibility = View.VISIBLE
                            createProgressiveTaskBinding.subTaskLayout.visibility = View.VISIBLE
                            createProgressiveTaskBinding.dailyDropDownLayout.visibility = View.GONE
                            createProgressiveTaskBinding.addSubTaskButton.visibility = View.VISIBLE
                            createProgressiveTaskBinding.alarmLayout.visibility = View.GONE
                            createProgressiveTaskBinding.timerLayout.visibility = View.GONE
                            if(!isEditTask) {
                                startDate = null
                                endDate = null
                            }
                        }

                        1 -> {
                            createProgressiveTaskBinding.subTaskLayout.visibility = View.GONE
                            createProgressiveTaskBinding.dailyDropDownLayout.visibility =
                                View.VISIBLE
                            createProgressiveTaskBinding.alarmLayout.visibility = View.VISIBLE
                            createProgressiveTaskBinding.timerLayout.visibility = View.VISIBLE
                        }

                        else -> {
                            createProgressiveTaskBinding.dateLayout.visibility = View.VISIBLE
                            createProgressiveTaskBinding.setDateButton.visibility = View.VISIBLE
                            createProgressiveTaskBinding.subTaskLayout.visibility = View.GONE
                            createProgressiveTaskBinding.alarmLayout.visibility = View.VISIBLE
                            createProgressiveTaskBinding.timerLayout.visibility = View.VISIBLE
                            createProgressiveTaskBinding.addSubTaskButton.visibility = View.GONE
                            createProgressiveTaskBinding.dailyDropDownLayout.visibility = View.GONE
                            if(!isEditTask) {
                                startDate = null
                                endDate = null
                            }
                        }
                    }
                    validateDate(startTime, endTime, startDate, endDate)
                }


                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        createProgressiveTaskBinding.dialyDropDown.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                @SuppressLint("SimpleDateFormat")
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    currentDailyTaskSelected = position
                    when (position) {
                        0 -> {
                            createProgressiveTaskBinding.dateLayout.visibility = View.GONE
                            createProgressiveTaskBinding.setDateButton.visibility = View.GONE
                            val date = Date()
                            val SDF = SimpleDateFormat("MM-dd-yyyy")
                            startDate = SDF.format(date)
                            endDate = SDF.format(date)
                        }

                        else -> {
                            createProgressiveTaskBinding.dateLayout.visibility = View.VISIBLE
                            createProgressiveTaskBinding.setDateButton.visibility = View.VISIBLE
                            if(!isEditTask){
                            startDate = null
                            endDate = null
                                }
                        }
                    }
                    validateDate(startTime, endTime, startDate, endDate)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}

            }

        createProgressiveTaskBinding.saveAndCreate.setOnClickListener {
            validateAllElements()
            if (noError) {
                val taskName = createProgressiveTaskBinding.taskNameInput.text.toString()
                var taskDescription: String? = null
                if (descriptionEnabled)
                    taskDescription =
                        createProgressiveTaskBinding.taskDescriptionInput.text.toString()
                when (currentSelectedItem) {
                    0 -> {

                        // Given Task is a Progressive Task
                        if(!isEditTask) {
                            taskId =
                                database.child(SubTaskElements.TASK_TYPE_PROGRESSIVE)
                                    .child(auth.currentUser!!.uid)
                                    .push().key
                        }
                        val progressiveElement = ProgressiveTaskElements(
                            taskId = taskId,
                            taskName = taskName,
                            taskDescription = taskDescription,
                            taskStartTime = startTime,
                            taskEndTime = endTime,
                            taskStartDate = startDate,
                            taskEndDate = endDate,
                            taskProgress = 0,
                            taskDuration = createProgressiveTaskBinding.durationText.text.toString(),
                            taskPriority = prioritySelected,
                            taskAttachments = attachmentArrayList,
                            taskReferals = referalArrayList,
                            isNotificationEnabled =
                            createProgressiveTaskBinding.timerSwitch.isChecked
                        )

                        for (i in subTaskArrayList.indices) {

                            database.child(SubTaskElements.TASK_TYPE_SUB_TASK)
                                .child(auth.currentUser!!.uid).child(taskId!!).removeValue()
                            
                            if(subTaskArrayList[i].subTaskId != null) {
                                subTaskArrayList[i].subTaskId =
                                    database.child(SubTaskElements.TASK_TYPE_SUB_TASK)
                                        .child(auth.currentUser!!.uid).child(taskId!!)
                                        .push().key!!
//                                database.child(SubTaskElements.TASK_TYPE_SUB_TASK)
//                                    .child(auth.currentUser!!.uid).child(taskId!!)
//                                    .child(subTaskArrayList[i].subTaskId!!).setValue(subTaskArrayList[i])

                            }
                            database.child(SubTaskElements.TASK_TYPE_SUB_TASK)
                                .child(auth.currentUser!!.uid).child(taskId!!)
                                .child(subTaskArrayList[i].subTaskId!!).setValue(subTaskArrayList[i])
                        }

                        for (i in attachmentUris.indices) {
                            storageReference.child("Attachments").child(SubTaskElements.TASK_TYPE_PROGRESSIVE)
                                .child(auth.currentUser!!.uid).child(taskId!!).child(attachmentArrayList[i].fileName.toString()).putFile(attachmentUris[i])
                        }

                        database.child(SubTaskElements.TASK_TYPE_PROGRESSIVE).child(auth.currentUser!!.uid)
                            .child(taskId!!).setValue(progressiveElement)
                    }

                    1 -> {
                        if(!isEditTask) {
                            taskId =
                                database.child(SubTaskElements.TASK_TYPE_DAILY)
                                    .child(auth.currentUser!!.uid).push().key
                        }
                        val dailyTaskElement = DailyTaskElements(
                            taskId = taskId,
                            taskDescription = taskDescription,
                            taskName = taskName,
                            taskStartTime = startTime,
                            taskEndTime = endTime,
                            taskRangeType = currentDailyTaskSelected,
                            taskStartDate = startDate,
                            taskEndDate = endDate,
                            taskPriority = prioritySelected,
                            taskDuration = createProgressiveTaskBinding.durationText.text.toString(),
                            attachmentList = attachmentArrayList,
                            referalList = referalArrayList,
                            isSetTimer = createProgressiveTaskBinding.timerSwitch.isChecked,
                            isNotificationEnabled = createProgressiveTaskBinding.notificationSwitch.isChecked,
                            isAlarmBeforeTenMinutesEnables = createProgressiveTaskBinding.alarmSwitch.isChecked
                        )

                        for (i in attachmentUris.indices) {
                            storageReference.child("Attachments").child(SubTaskElements.TASK_TYPE_DAILY)
                                .child(auth.currentUser!!.uid).child(taskId!!).child(attachmentArrayList[i].fileName.toString()).putFile(attachmentUris[i])
                        }

                        database.child(SubTaskElements.TASK_TYPE_DAILY).child(auth.currentUser!!.uid)
                            .child(taskId!!).setValue(dailyTaskElement)
                    }
                    else -> {
                        if(!isEditTask) {
                            taskId =
                                database.child(SubTaskElements.TASK_TYPE_DAILY)
                                    .child(auth.currentUser!!.uid).push().key
                        }
                        val normalTaskElement = NormalTaskElements(
                            taskId = taskId,
                            taskDescription = taskDescription,
                            taskName = taskName,
                            taskStartTime = startTime,
                            taskEndTime = endTime,
                            taskStartDate = startDate,
                            taskEndDate = endDate,
                            taskPriority = prioritySelected,
                            taskDuration = createProgressiveTaskBinding.durationText.text.toString(),
                            attachmentList = attachmentArrayList,
                            referalList = referalArrayList,
                            isSetTimer = createProgressiveTaskBinding.timerSwitch.isChecked,
                            isAlarmEnabled = createProgressiveTaskBinding.alarmSwitch.isChecked,
                            isNotificationEnabled = createProgressiveTaskBinding.notificationSwitch.isChecked
                        )

                        for (i in attachmentUris.indices) {
                            storageReference.child("Attachments").child(SubTaskElements.TASK_TYPE_NORMAL)
                                .child(auth.currentUser!!.uid).child(taskId!!).child(attachmentArrayList[i].fileName.toString()).putFile(attachmentUris[i])
                        }

                        database.child(SubTaskElements.TASK_TYPE_NORMAL).child(auth.currentUser!!.uid)
                            .child(taskId!!).setValue(normalTaskElement)
                    }
                }
                finish()
            }
        }

        createProgressiveTaskBinding.setDateButton.setOnClickListener {
            // Set up Date Picker
            val datePickerRange = MaterialDatePicker.Builder
                .dateRangePicker()
                .setTitleText("Select Dates")
                .build()

            datePickerRange.show(supportFragmentManager, "date_range_picker")
            datePickerRange.addOnPositiveButtonClickListener {
                startDate = longToDateString(it.first)
                endDate = longToDateString(it.second)

                createProgressiveTaskBinding.startDateText.text = startDate
                createProgressiveTaskBinding.endDateText.text = endDate
                createProgressiveTaskBinding.startDateText.setTextColor(0xFFFFFFFF.toInt())
                createProgressiveTaskBinding.endDateText.setTextColor(0xFFFFFFFF.toInt())

                validateDate(startTime, endTime, startDate, endDate)
            }
        }


        createProgressiveTaskBinding.addReferalsButton.setOnClickListener {
            createURLPopUp()
        }



        createProgressiveTaskBinding.addDescriptionButton.setOnClickListener {
            if (!descriptionEnabled) {
                descriptionEnabled = true
                createProgressiveTaskBinding.taskDescriptionHeading.visibility = View.VISIBLE
                createProgressiveTaskBinding.taskDescriptionLayout.visibility = View.VISIBLE
            }
        }
        createProgressiveTaskBinding.removeDescriptionButton.setOnClickListener {
            if (descriptionEnabled) {
                descriptionEnabled = false
                createProgressiveTaskBinding.taskDescriptionHeading.visibility = View.GONE
                createProgressiveTaskBinding.taskDescriptionLayout.visibility = View.GONE
            }
        }

        // Add Sub Task Button
        createProgressiveTaskBinding.addSubTaskButton.setOnClickListener {
            if (overallEndDate != null && subTaskEndTime != null && overallEndTime != null && subTaskEndDate != null) {
                createProgressiveTaskBinding.addSubTaskErrorText.visibility = View.GONE
                val subTaskBottomSheet = SubTaskBottomSheet()
                subTaskBottomSheet.show(supportFragmentManager, subTaskBottomSheet.tag)
            } else {
                createProgressiveTaskBinding.addSubTaskErrorText.text =
                    "Required Date and Time before Creation"
                createProgressiveTaskBinding.addSubTaskErrorText.visibility = View.VISIBLE
            }
        }


        // Add Attachment button
        createProgressiveTaskBinding.addAttachmentsButton.setOnClickListener {
            showFileSystem()
        }

        // Priority Buttons
        createProgressiveTaskBinding.priorityLowButton.setOnClickListener {
            createProgressiveTaskBinding.priorityLowButton.setBackgroundResource(R.drawable.priority_bg1)
            createProgressiveTaskBinding.priorityMediumButton.setBackgroundResource(R.drawable.unselected_priority)
            createProgressiveTaskBinding.priorityHighButton.setBackgroundResource(R.drawable.unselected_priority)
            prioritySelected = SubTaskElements.PRIORITY_LOW
        }

        createProgressiveTaskBinding.priorityMediumButton.setOnClickListener {
            createProgressiveTaskBinding.priorityLowButton.setBackgroundResource(R.drawable.unselected_priority)
            createProgressiveTaskBinding.priorityMediumButton.setBackgroundResource(R.drawable.priority_bg2)
            createProgressiveTaskBinding.priorityHighButton.setBackgroundResource(R.drawable.unselected_priority)
            prioritySelected = SubTaskElements.PRIORITY_MEDIUM
        }

        createProgressiveTaskBinding.priorityHighButton.setOnClickListener {
            createProgressiveTaskBinding.priorityLowButton.setBackgroundResource(R.drawable.unselected_priority)
            createProgressiveTaskBinding.priorityMediumButton.setBackgroundResource(R.drawable.unselected_priority)
            createProgressiveTaskBinding.priorityHighButton.setBackgroundResource(R.drawable.priority_bg3)
            prioritySelected = SubTaskElements.PRIORITY_HIGH
        }



        supportFragmentManager.setFragmentResultListener("SUB_TASK", this) { _, bundle ->
            val subTaskName = bundle.getString(SubTaskElements.subTaskNameKey)
            val subTaskDescription = bundle.getString(SubTaskElements.subTaskDescriptionKey)
            val subTaskStartTime1 = bundle.getString(SubTaskElements.subTaskStartTimeKey)
            val subTaskEndTime1 = bundle.getString(SubTaskElements.subTaskEndTimeKey)
            val subTaskStartDate1 = bundle.getString(SubTaskElements.subTaskStartDateKey)
            val subTaskEndDate1 = bundle.getString(SubTaskElements.subTaskEndDateKey)
            val subTaskDuration = bundle.getString(SubTaskElements.subTaskDurationKey)
            val subTaskPriority = bundle.getInt(SubTaskElements.subTaskPriorityKey)
            val subTaskIsNotificationEnabled =
                bundle.getBoolean(SubTaskElements.subTaskIsNotificationEnabled)

            subTaskEndTime = subTaskEndTime1
            subTaskEndDate = subTaskEndDate1

            val subTaskElement = SubTaskElements(
                subTaskName!!,
                subTaskDescription!!,
                subTaskStartTime1!!,
                subTaskEndTime1!!,
                subTaskStartDate1!!,
                subTaskEndDate1!!,
                subTaskDuration!!,
                subTaskPriority,
                subTaskIsNotificationEnabled
            )
            subTaskArrayList.add(0, subTaskElement)
            createProgressiveTaskBinding.subTaskRecyclerView.adapter =
                CreateTaskSubTaskAdapter(this, subTaskArrayList)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })

    }

    private fun initialize(temp: HashMap<*,*>) {

        createProgressiveTaskBinding.taskNameInput.text =
            Editable.Factory.getInstance().newEditable(temp[ProgressiveTaskElements.NAME_KEY].toString())
        descriptionEnabled = temp.containsKey(ProgressiveTaskElements.DESCRIPTION_KEY)
        if (temp.containsKey(ProgressiveTaskElements.DESCRIPTION_KEY)) {
            createProgressiveTaskBinding.taskDescriptionInput.text =
                Editable.Factory.getInstance().newEditable(temp[ProgressiveTaskElements.DESCRIPTION_KEY].toString())
            createProgressiveTaskBinding.taskDescriptionInput.visibility = View.VISIBLE
        }

        createProgressiveTaskBinding.startDateText.text = temp[ProgressiveTaskElements.START_DATE_KEY].toString()
        createProgressiveTaskBinding.startDateText.setTextColor(0xFFFFFFFF.toInt())

        createProgressiveTaskBinding.endDateText.text = temp[ProgressiveTaskElements.END_DATE_KEY].toString()
        createProgressiveTaskBinding.endDateText.setTextColor(0xFFFFFFFF.toInt())

        createProgressiveTaskBinding.startTimeButton.text = temp[ProgressiveTaskElements.START_TIME_KEY].toString()
        createProgressiveTaskBinding.startTimeButton.setTextColor(0xFFFFFFFF.toInt())

        createProgressiveTaskBinding.endTimeButton.text = temp[ProgressiveTaskElements.END_TIME_KEY].toString()
        createProgressiveTaskBinding.endTimeButton.setTextColor(0xFFFFFFFF.toInt())


        startDate = temp[ProgressiveTaskElements.START_DATE_KEY].toString()
        endDate = temp[ProgressiveTaskElements.END_DATE_KEY].toString()
        startTime = temp[ProgressiveTaskElements.START_TIME_KEY].toString()
        endTime = temp[ProgressiveTaskElements.END_TIME_KEY].toString()

        val listener = object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val hash = snapshot.value!! as HashMap<*,*>
                if(hash.isEmpty())
                    return
                else if(hash.containsKey(ProgressiveTaskElements.ID_KEY)){
                    initializeSubTask(hash)
                }else{
                    hash.forEach {
                        initializeSubTask(it.value!! as HashMap<*,*>)
                    }
                }
                onDataUpdated()
            }

            override fun onCancelled(error: DatabaseError) {}

        }

        val attachmentListener = object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value == null)
                    return
                val hash = snapshot.value!! as List<*>
                if(hash.isEmpty())
                    return
                else {
                    for(i in hash.indices) {
                        initializeAttachment(hash[i] as HashMap<*,*>)
                    }
                }
                onDataUpdated()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        val referalListener = object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value == null)
                    return
                val hash = snapshot.value!! as List<*>
                if(hash.isEmpty())
                    return
                else {
                    for(i in hash.indices) {
                        referalArrayList.add(hash[i] as String?)
                    }
                }
                onDataUpdated()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        when(temp[ProgressiveTaskElements.PRIORITY_KEY].toString().toInt()){
            SubTaskElements.PRIORITY_HIGH -> {
                createProgressiveTaskBinding.priorityLowButton.setBackgroundResource(R.drawable.unselected_priority)
                createProgressiveTaskBinding.priorityMediumButton.setBackgroundResource(R.drawable.unselected_priority)
                createProgressiveTaskBinding.priorityHighButton.setBackgroundResource(R.drawable.priority_bg3)
                prioritySelected = SubTaskElements.PRIORITY_HIGH
            }
            SubTaskElements.PRIORITY_MEDIUM -> {
                createProgressiveTaskBinding.priorityLowButton.setBackgroundResource(R.drawable.unselected_priority)
                createProgressiveTaskBinding.priorityMediumButton.setBackgroundResource(R.drawable.priority_bg2)
                createProgressiveTaskBinding.priorityHighButton.setBackgroundResource(R.drawable.unselected_priority)
                prioritySelected = SubTaskElements.PRIORITY_MEDIUM
            }
            else -> {
                createProgressiveTaskBinding.priorityLowButton.setBackgroundResource(R.drawable.priority_bg1)
                createProgressiveTaskBinding.priorityMediumButton.setBackgroundResource(R.drawable.unselected_priority)
                createProgressiveTaskBinding.priorityHighButton.setBackgroundResource(R.drawable.unselected_priority)
                prioritySelected = SubTaskElements.PRIORITY_LOW
            }
        }
        prioritySelected = temp[ProgressiveTaskElements.PRIORITY_KEY].toString().toInt()

        createProgressiveTaskBinding.notificationSwitch.isChecked = temp[ProgressiveTaskElements.NOTIFICATION_KEY].toString().toBoolean()


        database.child(SubTaskElements.TASK_TYPE_PROGRESSIVE).child(auth.currentUser!!.uid).child(taskId!!).child(ProgressiveTaskElements.ATTACHMENT_KEY).addValueEventListener(attachmentListener)
        database.child(SubTaskElements.TASK_TYPE_PROGRESSIVE).child(auth.currentUser!!.uid).child(taskId!!).child(ProgressiveTaskElements.REFERAL_KEY).addValueEventListener(referalListener)
        database.child(SubTaskElements.TASK_TYPE_SUB_TASK).child(auth.currentUser!!.uid).child(taskId!!).addValueEventListener(listener)





    }

    private fun initializeAttachment(hash: HashMap<*, *>) {
        val fileName = hash[AttachmentElements.NAME_KEY].toString()
        val filePath = hash[AttachmentElements.PATH_KEY].toString()
        val fileType = hash[AttachmentElements.TYPE_KEY].toString().toInt()


        val attachmentElement = AttachmentElements(
            fileName = fileName,
            filePath = filePath,
            fileType = fileType
        )
        attachmentArrayList.add(attachmentElement)
    }

    private fun initializeSubTask(hashMap: HashMap<*, *>) {
        
        val taskName = hashMap["subTaskName"].toString()
        val taskDescription = hashMap["subTaskDescription"].toString()
        val startTime = hashMap["subTaskStartTime"].toString()
        val endTime = hashMap["subTaskEndTime"].toString()
        val startDate = hashMap["subTaskStartDate"].toString()
        val endDate = hashMap["subTaskEndDate"].toString()
        val priority = hashMap["priority"].toString().toInt()
        val duration = hashMap["duration"].toString()
        val isNotification = hashMap["notificationEnabled"].toString().toBoolean()


        val subTaskElement = SubTaskElements(
            subTaskId = hashMap["subTaskId"].toString(),
            subTaskName = taskName,
            subTaskDescription = taskDescription,
            subTaskStartDate = startDate,
            subTaskStartTime = startTime,
            subTaskEndDate = endDate,
            subTaskEndTime = endTime,
            priority = priority,
            duration = duration,
            isNotificationEnabled = isNotification
        )
        subTaskArrayList.add(subTaskElement)
    }

    private fun showExitDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.pop_up)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val discardButton = dialog.findViewById<AppCompatButton>(R.id.pop_up_discard_button)
        val cancelButton = dialog.findViewById<AppCompatButton>(R.id.pop_up_cancel_button)

        discardButton.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    @SuppressLint("InflateParams")
    private fun createURLPopUp() {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.url_input)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val editText = dialog.findViewById<EditText>(R.id.referal_url_input)
        val saveButton = dialog.findViewById<AppCompatButton>(R.id.referal_save_button)
        val discardButton = dialog.findViewById<AppCompatButton>(R.id.referal_discard_button)

        editText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        }
        saveButton.setOnClickListener {
            if (!URLUtil.isValidUrl(editText.text.toString())) {
                dialog.dismiss()
                Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show()
            } else {
                val link = editText.text.toString()
                referalArrayList.add(link)
                createProgressiveTaskBinding.referalsRecyclerView.adapter = ReferalAdapter(
                    this,
                    referalArrayList
                )
                dialog.dismiss()
            }
        }

        discardButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showFileSystem() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a file"), 100)
        } catch (exception: Exception) {
            Toast.makeText(this, "Please Install a file manager $exception", Toast.LENGTH_SHORT)
                .show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun validateAllElements() {
        val taskName: String = createProgressiveTaskBinding.taskNameInput.text.toString()
        var taskDescription: String? = null
        val taskType = taskDropDownArray[currentSelectedItem]


        if (taskName == "") {
            noError = false
            createProgressiveTaskBinding.taskNameErrorText.text = "Required Task Name"
            createProgressiveTaskBinding.taskNameErrorText.visibility = View.VISIBLE
            return
        } else {
            noError = true
            createProgressiveTaskBinding.taskNameErrorText.visibility = View.GONE
        }

        if (descriptionEnabled) {
            taskDescription = createProgressiveTaskBinding.taskDescriptionInput.text.toString()
            if (taskDescription == "") {
                createProgressiveTaskBinding.taskDescriptionErrorText.text =
                    "Required task Description"
                createProgressiveTaskBinding.taskDescriptionErrorText.visibility = View.VISIBLE
                noError = false
                return
            } else {
                noError = true
                createProgressiveTaskBinding.taskDescriptionErrorText.visibility = View.GONE
            }
        }

        if (prioritySelected == 0) {
            noError = false
            createProgressiveTaskBinding.priorityErrorText.text = "Required Priority"
            createProgressiveTaskBinding.priorityErrorText.visibility = View.VISIBLE
            return
        } else {
            noError = true
            createProgressiveTaskBinding.priorityErrorText.visibility = View.GONE
        }

        validateDate(startTime, endTime, startDate, endDate)
    }

    @SuppressLint("NewApi")
    private fun timePicker(buttonView: AppCompatButton, isStartTime: Boolean) {
        val dialog = TimePickerDialog(
            this,
            { view, hourOfDay, minute ->
                var hourString = ""
                var minuteString = ""
                hourString = if (hourOfDay < 10)
                    "0$hourOfDay"
                else
                    "$hourOfDay"

                minuteString = if (minute < 10)
                    "0$minute"
                else
                    "$minute"
                if (isStartTime) {
                    startTime = "$hourString:$minuteString"
                    buttonView.text = startTime
                } else {
                    endTime = "$hourString:$minuteString"
                    buttonView.text = endTime
                }

                validateDate(startTime, endTime, startDate, endDate)
                buttonView.setTextColor(0xFFFFFFFF.toInt())
                if (currentSelectedItem == 1 && currentDailyTaskSelected == 0)
                    validateDate(startTime, endTime, startDate, endDate)
            },
            buttonView.text.substring(0, 2).toInt(),
            buttonView.text.substring(3, 5).toInt(),
            true

        )
        dialog.show()
    }

    private fun longToDateString(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
        return format.format(date)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun validateDate(time1: String?, time2: String?, date1: String?, date2: String?) {


        if (date1 == null || date2 == null) {
            noError = false
            createProgressiveTaskBinding.durationText.visibility = View.VISIBLE
            createProgressiveTaskBinding.timeErrorText.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.red
                )
            )
            createProgressiveTaskBinding.durationText.text = "Required Dates"
            return
        } else {
            noError = true
            createProgressiveTaskBinding.timeErrorText.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.white
                )
            )
            createProgressiveTaskBinding.durationText.visibility = View.GONE
        }


        if (time1 == null && time2 == null) {
            noError = false
            createProgressiveTaskBinding.timeErrorText.text = "Required Starting and Ending Time"
            createProgressiveTaskBinding.timeErrorText.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.red
                )
            )
            createProgressiveTaskBinding.timeErrorText.visibility = View.VISIBLE

            return
        } else if (time1 == null) {
            noError = false
            createProgressiveTaskBinding.timeErrorText.text = "Required Starting Time"
            createProgressiveTaskBinding.timeErrorText.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.red
                )
            )
            createProgressiveTaskBinding.timeErrorText.visibility = View.VISIBLE

            return
        } else if (time2 == null) {
            noError = false
            createProgressiveTaskBinding.timeErrorText.text = "Required Ending Time"
            createProgressiveTaskBinding.timeErrorText.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.red
                )
            )
            createProgressiveTaskBinding.timeErrorText.visibility = View.VISIBLE

            return
        } else {
            noError = true
            createProgressiveTaskBinding.timeErrorText.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.white
                )
            )
            createProgressiveTaskBinding.timeErrorText.visibility = View.GONE

        }


        val SDF = SimpleDateFormat("MM-dd-yyyy")
        val parsedOverAllStartDate = SDF.parse(date1)!!
        val parsedOverAllEndDate = SDF.parse(date2)!!


        val currentDate = SDF.parse(SDF.format(Date()))

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val parsedOverAllStartTime = LocalTime.parse(time1, timeFormatter)
        val parsedOverAllEndTime = LocalTime.parse(time2, timeFormatter)
        val duration = Duration.between(parsedOverAllStartTime, parsedOverAllEndTime)


        // OverAllStartDate > CurrentDate
        if (parsedOverAllStartDate < currentDate) {
            noError = false
            createProgressiveTaskBinding.durationText.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.red
                )
            )
            createProgressiveTaskBinding.durationText.text =
                "Required Start Date Greater that current date"
            createProgressiveTaskBinding.durationText.visibility = View.VISIBLE

        } else {
            noError = true
            createProgressiveTaskBinding.durationText.visibility = View.GONE
            startDate = date1
            endDate = date2
            subTaskEndDate = startDate
            overallEndDate = endDate
            createProgressiveTaskBinding.durationText.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.white
                )
            )
        }

        // If Start And End Date are same then compare Times
        if (parsedOverAllStartDate.compareTo(parsedOverAllEndDate) == 0 && (parsedOverAllEndTime < parsedOverAllStartTime || duration.toHours() < 1)) {
            noError = false
            createProgressiveTaskBinding.timeErrorText.text =
                "Invalid time or duration is less than an hour for the given date"
            createProgressiveTaskBinding.timeErrorText.visibility = View.VISIBLE
            createProgressiveTaskBinding.durationText.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.red
                )
            )
        } else {
            noError = true
            createProgressiveTaskBinding.durationText.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.white
                )
            )
            val difference = parsedOverAllEndDate.time - parsedOverAllStartDate.time
            var days = TimeUnit.MILLISECONDS.toDays(difference)
            var hours = duration.toHours()
            var minutes = duration.toMinutes() % 60
            var durationText: String = "Duration : "

            if (duration.toHours() < 0) {
                hours += 24
                days--
            }
            if (duration.toMinutes() < 0) {
                duration.toMinutes() % 60 + 60
                hours--
            }




            if (days.toInt() != 0)
                durationText += "$days days "
            if (hours.toInt() != 0)
                durationText += "$hours hours "
            if (duration.toMinutes().toInt() % 60 != 0)
                durationText += "$minutes minutes"
            createProgressiveTaskBinding.durationText.text = durationText
            createProgressiveTaskBinding.durationText.visibility = View.VISIBLE
            startTime = time1
            endTime = time2
            subTaskEndTime = startTime
            overallEndTime = endTime

        }
    }

    @SuppressLint("Recycle", "Range")
    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            val uri = data.data!!
            val path = uri.path.toString()

            var fileNameLocal: String? = null
            if (uri.scheme == "content") {
                val cursor = applicationContext.contentResolver.query(uri, null, null, null, null)
                cursor.use {
                    if (cursor != null && cursor.moveToFirst())
                        fileNameLocal =
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }


            val fileType = AttachmentElements.findAttachmentIcon(fileNameLocal!!)


            val attachmentElement = AttachmentElements(fileType, fileNameLocal, path)
            attachmentUris.add(uri)
            attachmentArrayList.add(attachmentElement)
            createProgressiveTaskBinding.attachmentsRecyclerView.adapter = AttachmentAdapter(
                activity = this,
                attachments = attachmentArrayList,
                attachmentUri = attachmentUris
            )
        }


        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDataUpdated() {
        createProgressiveTaskBinding.subTaskRecyclerView.adapter =
            CreateTaskSubTaskAdapter(this, subTaskArrayList)
        createProgressiveTaskBinding.attachmentsRecyclerView.adapter = AttachmentAdapter(
            activity = this,
            attachments = attachmentArrayList,
            attachmentUri = attachmentUris
        )
        createProgressiveTaskBinding.referalsRecyclerView.adapter =
            ReferalAdapter(this, referalArrayList)
    }

    companion object {
        var subTaskEndTime: String? = null
        var subTaskEndDate: String? = null
        var overallEndDate: String? = null
        var overallEndTime: String? = null

        private var functionCallBack: DataUpdateCallback? = null
        var isElementRemoved = false

        var attachmentUris: ArrayList<Uri> = arrayListOf()
        var subTaskArrayList: ArrayList<SubTaskElements> = arrayListOf()
        var attachmentArrayList: ArrayList<AttachmentElements> = arrayListOf()
        var referalArrayList: ArrayList<String?> = arrayListOf()

        fun setDataCallback(callback: DataUpdateCallback) {
            this.functionCallBack = callback
        }

        fun checkIfExists(taskName: String): Boolean {
            for (element in subTaskArrayList) {
                if (element.subTaskName == taskName)
                    return true
            }
            return false
        }

        fun removeSubTaskElement() {
            if (subTaskArrayList.size != 0) {
                subTaskArrayList.removeAt(0)
                isElementRemoved = true
            }
            functionCallBack?.onDataUpdated()
        }

        fun removeAttachmentElement(position: Int) {
            if (0 <= position && position < attachmentArrayList.size) {
                attachmentArrayList.removeAt(position)
            }
            functionCallBack?.onDataUpdated()
        }

        fun removeReferalElement(position: Int) {
            if (0 <= position && position < referalArrayList.size) {
                referalArrayList.removeAt(position)
            }
            functionCallBack?.onDataUpdated()
        }

    }


}

interface DataUpdateCallback {
    fun onDataUpdated()
}
