package com.example.tasker

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TimePicker
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.example.tasker.databinding.FragmentSubTaskBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class SubTaskBottomSheet : BottomSheetDialogFragment() {

    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var startTime: String? = null
    private var endTime: String? = null
    private var startDate: String? = null
    private var endDate: String? = null
    private var differenceString: String? = null

    private var noError: Boolean = false
    private var prioritySelected: Int = 0


    private lateinit var subTaskBottomSheetBinding: FragmentSubTaskBottomSheetBinding

    fun String.toEditable():Editable = Editable.Factory.getInstance().newEditable(this)
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return bottomSheetDialog
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //Initilaizations
        subTaskBottomSheetBinding =
            FragmentSubTaskBottomSheetBinding.inflate(inflater, container, false)
        initialize(arguments)
        subTaskBottomSheetBinding.createSubTaskScrollView.scrollTo(
            0,
            subTaskBottomSheetBinding.createSubTaskScrollView.bottom
        )
        subTaskBottomSheetBinding.createSubTaskScrollView.fullScroll(View.FOCUS_DOWN)


        //Event Handlers
        subTaskBottomSheetBinding.createSubTaskCloseButton.setOnClickListener {
            showExitDialog()
        }
        subTaskBottomSheetBinding.startTimeButton.setOnClickListener {
            timePicker(subTaskBottomSheetBinding.startTimeButton, true)
        }
        subTaskBottomSheetBinding.endTimeButton.setOnClickListener {
            timePicker(subTaskBottomSheetBinding.endTimeButton, false)
        }
        subTaskBottomSheetBinding.subTaskSetDateButton.setOnClickListener {
            val dateRangePicker = MaterialDatePicker.Builder
                .dateRangePicker()
                .setTitleText("Select Date")
                .build()

            dateRangePicker.show(requireFragmentManager(), "date_range_picker")
            dateRangePicker.addOnPositiveButtonClickListener {
                startDate = longToDateString(it.first)
                endDate = longToDateString(it.second)

                subTaskBottomSheetBinding.subTaskStartDate.text = startDate
                subTaskBottomSheetBinding.subTaskEndDate.text = endDate

                subTaskBottomSheetBinding.subTaskStartDate.setTextColor(0xFFFFFFFF.toInt())
                subTaskBottomSheetBinding.subTaskEndDate.setTextColor(0xFFFFFFFF.toInt())

                validateDate(startTime, endTime, startDate, endDate)
            }
        }

        subTaskBottomSheetBinding.subTaskSaveAndCreate.setOnClickListener {
            validateAllElements()
            if (noError) {
                val subTaskName = subTaskBottomSheetBinding.subTaskNameInput.text.toString()
                val subTaskDescription =
                    subTaskBottomSheetBinding.subTaskDescriptionInput.text.toString()
                var isNotificationEnabled = false
                if (subTaskBottomSheetBinding.subTaskNotificationSwitch.isChecked)
                    isNotificationEnabled = true
                val result = Bundle().apply {
                    putString(SubTaskElements.subTaskNameKey, subTaskName)
                    putString(SubTaskElements.subTaskDescriptionKey, subTaskDescription)
                    putString(SubTaskElements.subTaskStartTimeKey, startTime)
                    putString(SubTaskElements.subTaskEndTimeKey, endTime)
                    putString(SubTaskElements.subTaskStartDateKey, startDate)
                    putString(SubTaskElements.subTaskEndDateKey, endDate)
                    putString(SubTaskElements.subTaskDurationKey, differenceString)
                    putInt(SubTaskElements.subTaskPriorityKey, prioritySelected)
                    putBoolean(SubTaskElements.subTaskIsNotificationEnabled, isNotificationEnabled)
                }

                parentFragmentManager.setFragmentResult("SUB_TASK", result)
                dismiss()
            }
        }

        subTaskBottomSheetBinding.priorityLowButton.setOnClickListener {
            subTaskBottomSheetBinding.priorityLowButton.setBackgroundResource(R.drawable.priority_bg1)
            subTaskBottomSheetBinding.priorityMediumButton.setBackgroundResource(R.drawable.unselected_priority)
            subTaskBottomSheetBinding.priorityHighButton.setBackgroundResource(R.drawable.unselected_priority)
            prioritySelected = SubTaskElements.PRIORITY_LOW
        }

        subTaskBottomSheetBinding.priorityMediumButton.setOnClickListener {
            subTaskBottomSheetBinding.priorityLowButton.setBackgroundResource(R.drawable.unselected_priority)
            subTaskBottomSheetBinding.priorityMediumButton.setBackgroundResource(R.drawable.priority_bg2)
            subTaskBottomSheetBinding.priorityHighButton.setBackgroundResource(R.drawable.unselected_priority)
            prioritySelected = SubTaskElements.PRIORITY_MEDIUM
        }

        subTaskBottomSheetBinding.priorityHighButton.setOnClickListener {
            subTaskBottomSheetBinding.priorityLowButton.setBackgroundResource(R.drawable.unselected_priority)
            subTaskBottomSheetBinding.priorityMediumButton.setBackgroundResource(R.drawable.unselected_priority)
            subTaskBottomSheetBinding.priorityHighButton.setBackgroundResource(R.drawable.priority_bg3)
            prioritySelected = SubTaskElements.PRIORITY_HIGH
        }

        return subTaskBottomSheetBinding.root
    }

    private fun initialize(arguments:Bundle?) {
        if(arguments!=null){
            val subTaskName = arguments.getString(SubTaskElements.subTaskNameKey)
            val subTaskDescription = arguments.getString(SubTaskElements.subTaskDescriptionKey)
            val subTaskStartTime1 = arguments.getString(SubTaskElements.subTaskStartTimeKey)
            val subTaskEndTime1 = arguments.getString(SubTaskElements.subTaskEndTimeKey)
            val subTaskStartDate1 = arguments.getString(SubTaskElements.subTaskStartDateKey)
            val subTaskEndDate1 = arguments.getString(SubTaskElements.subTaskEndDateKey)
            val subTaskDuration = arguments.getString(SubTaskElements.subTaskDurationKey)
            val subTaskPriority = arguments.getInt(SubTaskElements.subTaskPriorityKey)
            val subTaskIsNotificationEnabled =
                arguments.getBoolean(SubTaskElements.subTaskIsNotificationEnabled)

            startTime = subTaskStartTime1
            startDate = subTaskStartDate1
            endTime = subTaskEndTime1
            endDate = subTaskEndDate1
            differenceString = subTaskDuration

            subTaskBottomSheetBinding.subTaskNameInput.text = subTaskName!!.toEditable()
            subTaskBottomSheetBinding.subTaskDescriptionInput.text = subTaskDescription!!.toEditable()
            subTaskBottomSheetBinding.startTimeButton.text = subTaskStartTime1
            subTaskBottomSheetBinding.subTaskStartDate.text = subTaskStartDate1
            subTaskBottomSheetBinding.endTimeButton.text = subTaskEndTime1
            subTaskBottomSheetBinding.subTaskEndDate.text = subTaskEndDate1
            subTaskBottomSheetBinding.subTaskDurationText.text = subTaskDuration

            when(subTaskPriority){
                SubTaskElements.PRIORITY_LOW -> {
                    subTaskBottomSheetBinding.priorityLowButton.setBackgroundResource(R.drawable.priority_bg1)
                    subTaskBottomSheetBinding.priorityMediumButton.setBackgroundResource(R.drawable.unselected_priority)
                    subTaskBottomSheetBinding.priorityHighButton.setBackgroundResource(R.drawable.unselected_priority)
                    prioritySelected = SubTaskElements.PRIORITY_LOW
                }
                SubTaskElements.PRIORITY_MEDIUM ->{
                    subTaskBottomSheetBinding.priorityLowButton.setBackgroundResource(R.drawable.unselected_priority)
                    subTaskBottomSheetBinding.priorityMediumButton.setBackgroundResource(R.drawable.priority_bg2)
                    subTaskBottomSheetBinding.priorityHighButton.setBackgroundResource(R.drawable.unselected_priority)
                    prioritySelected = SubTaskElements.PRIORITY_MEDIUM
                }
                SubTaskElements.PRIORITY_HIGH -> {
                    subTaskBottomSheetBinding.priorityLowButton.setBackgroundResource(R.drawable.unselected_priority)
                    subTaskBottomSheetBinding.priorityMediumButton.setBackgroundResource(R.drawable.unselected_priority)
                    subTaskBottomSheetBinding.priorityHighButton.setBackgroundResource(R.drawable.priority_bg3)
                    prioritySelected = SubTaskElements.PRIORITY_HIGH
                }
            }
            subTaskBottomSheetBinding.subTaskNotificationSwitch.isChecked = subTaskIsNotificationEnabled
            noError = true

        }

    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun validateAllElements() {
        val taskName: String = subTaskBottomSheetBinding.subTaskNameInput.text.toString()
        val taskDescription: String =
            subTaskBottomSheetBinding.subTaskDescriptionInput.text.toString()

        if (taskName == "") {
            noError = false
            subTaskBottomSheetBinding.subTaskNameErrorText.text = "Required Task Name"
            subTaskBottomSheetBinding.subTaskNameErrorText.visibility = View.VISIBLE
            return
        } else {
            if (CreateProgressiveTask.checkIfExists(taskName)) {
                noError = false
                subTaskBottomSheetBinding.subTaskNameErrorText.text = "Task Name Already Exists"
                subTaskBottomSheetBinding.subTaskNameErrorText.visibility = View.VISIBLE
                return
            }
            noError = true
            subTaskBottomSheetBinding.subTaskNameErrorText.visibility = View.GONE
        }



        if (taskDescription == "") {
            subTaskBottomSheetBinding.subTaskDescriptionErrorText.text = "Required task Description"
            subTaskBottomSheetBinding.subTaskDescriptionErrorText.visibility = View.VISIBLE
            noError = false
            return
        } else {
            noError = true
            subTaskBottomSheetBinding.subTaskDescriptionErrorText.visibility = View.GONE
        }


        if (prioritySelected == 0) {
            noError = false
            subTaskBottomSheetBinding.subTaskPriorityErrorText.text = "Required Priority"
            subTaskBottomSheetBinding.subTaskPriorityErrorText.visibility = View.VISIBLE
            return
        } else {
            noError = true
            subTaskBottomSheetBinding.subTaskPriorityErrorText.visibility = View.GONE
        }

        // If it is a progressive task
        validateDate(startTime,endTime,startDate,endDate)

    }

    @SuppressLint("NewApi")
    private fun timePicker(buttonView: AppCompatButton, isStartTime: Boolean) {
        val dialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
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


                buttonView.setTextColor(0xFFFFFFFF.toInt())
                validateDate(startTime,endTime,startDate,endDate)
            },
            buttonView.text.substring(0, 2).toInt(),
            buttonView.text.substring(3, 5).toInt(),
            true
        )
        dialog.show()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.isDraggable = false

        val layout: FrameLayout? = bottomSheetDialog.findViewById(R.id.bottom_sheet_layout)
        layout?.minimumHeight = Resources.getSystem().displayMetrics.heightPixels
    }


    private fun longToDateString(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
        return format.format(date)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun validateDate(time1: String?, time2: String?, date1: String?, date2: String?) {

        if (time1 == null && time2 == null) {
            subTaskBottomSheetBinding.subTaskTimeErrorText.text = "Required Start and End times"
            subTaskBottomSheetBinding.subTaskNameErrorText.visibility = View.VISIBLE
            noError = false
            return
        } else if (time1 == null) {
            subTaskBottomSheetBinding.subTaskTimeErrorText.text = "Required Start times"
            subTaskBottomSheetBinding.subTaskNameErrorText.visibility = View.VISIBLE
            noError = false
            return
        } else if (time2 == null) {
            subTaskBottomSheetBinding.subTaskTimeErrorText.text = "Required End times"
            subTaskBottomSheetBinding.subTaskNameErrorText.visibility = View.VISIBLE
            noError = false
            return
        } else {
            subTaskBottomSheetBinding.subTaskNameErrorText.visibility = View.GONE
            noError = true
        }


        if (date1 == null || date2 == null) {
            subTaskBottomSheetBinding.subTaskDurationText.text = "Required Dates"
            subTaskBottomSheetBinding.subTaskDurationText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.red
                )
            )
            subTaskBottomSheetBinding.subTaskDurationText.visibility = View.VISIBLE
            noError = false
            return
        } else {
            subTaskBottomSheetBinding.subTaskDurationText.visibility = View.GONE
            noError = true
        }


        // Initializations

        val SDF = SimpleDateFormat("MM-dd-yyyy")
        val parsedSubTaskStartDate = SDF.parse(date1)!!
        val parsedSubTaskEndDate = SDF.parse(date2)!!

        val overallEndDate = SDF.parse(CreateProgressiveTask.overallEndDate!!)
        val subTaskEndDate = SDF.parse(CreateProgressiveTask.subTaskEndDate!!)


        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val parsedSubTaskStartTime = LocalTime.parse(time1,timeFormatter)
        val parsedSubTaskEndTime = LocalTime.parse(time2,timeFormatter)

        val overallEndTime = LocalTime.parse(CreateProgressiveTask.overallEndTime,timeFormatter)
        val subTaskEndTime = LocalTime.parse(CreateProgressiveTask.subTaskEndTime,timeFormatter)


        // Conditional parts


        if (parsedSubTaskStartDate < subTaskEndDate) {
            subTaskBottomSheetBinding.subTaskDurationText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.red
                )
            )
            subTaskBottomSheetBinding.subTaskDurationText.text =
                "Required Start date greater than the previous end date or Overall start Date"
            subTaskBottomSheetBinding.subTaskDurationText.visibility = View.VISIBLE
            noError = false
            return
        }else{
            subTaskBottomSheetBinding.subTaskDurationText.visibility = View.GONE
            noError = true
        }




        if(parsedSubTaskStartTime < subTaskEndTime && parsedSubTaskStartDate == subTaskEndDate){
            noError = false
            subTaskBottomSheetBinding.subTaskTimeErrorText.text = "Required Start time greater than the previous end time or Overall start time"
            subTaskBottomSheetBinding.subTaskTimeErrorText.visibility = View.VISIBLE
            return
        }else{
            subTaskBottomSheetBinding.subTaskTimeErrorText.visibility = View.GONE
            noError = true
        }



        if(parsedSubTaskEndDate > overallEndDate){
            subTaskBottomSheetBinding.subTaskDurationText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.red
                )
            )
            subTaskBottomSheetBinding.subTaskDurationText.text =
                "Required End date lesser than the Overall End Date"
            subTaskBottomSheetBinding.subTaskDurationText.visibility = View.VISIBLE
            noError = false
            return
        }else{
            subTaskBottomSheetBinding.subTaskDurationText.visibility = View.GONE
            noError = true
        }



        if(parsedSubTaskEndTime > overallEndTime && parsedSubTaskEndDate == overallEndDate){
            noError = false
            subTaskBottomSheetBinding.subTaskTimeErrorText.text = "Required End time lesser than the Overall End time "
            subTaskBottomSheetBinding.subTaskTimeErrorText.visibility = View.VISIBLE
            return
        }else{
            subTaskBottomSheetBinding.subTaskTimeErrorText.visibility = View.GONE
            noError = true

            val duration = Duration.between(parsedSubTaskStartTime,parsedSubTaskEndTime)
            var hours = duration.toHours().toInt()
            var minutes = duration.toMinutes().toInt() % 60


            val difference = parsedSubTaskEndDate.time - parsedSubTaskStartDate.time
            var days = TimeUnit.MILLISECONDS.toDays(difference).toInt()

            if(minutes < 0 ){
                hours--
                minutes += 60
            }
            if(hours < 0){
                days--
                hours += 24
            }

            var durationString = "Duration : "
            if(days != 0)
                durationString += "$days days "
            if(hours != 0)
                durationString += "$hours hours "
            if(minutes != 0)
                durationString += "$minutes minutes"

            subTaskBottomSheetBinding.subTaskDurationText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            subTaskBottomSheetBinding.subTaskDurationText.text = durationString
            subTaskBottomSheetBinding.subTaskDurationText.visibility = View.VISIBLE

            startTime = time1
            endTime = time2
            startDate = date1
            endDate = date2
            differenceString = durationString
        }

    }
    private fun showExitDialog(){
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.pop_up)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val discardButton = dialog.findViewById<AppCompatButton>(R.id.pop_up_discard_button)
        val cancelButton = dialog.findViewById<AppCompatButton>(R.id.pop_up_cancel_button)

        discardButton.setOnClickListener {
            dialog.dismiss()
            dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    companion object {
        var monthMap = mapOf(
            1 to 31,
            2 to 28,
            3 to 31,
            4 to 30,
            5 to 31,
            6 to 30,
            7 to 31,
            8 to 31,
            9 to 30,
            10 to 31,
            11 to 30,
            12 to 31
        )
    }
}