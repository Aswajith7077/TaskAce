package com.example.tasker

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tasker.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class HomeFragment : Fragment() {

    private lateinit var homeFragmentBinding: FragmentHomeBinding
    private lateinit var auth:FirebaseAuth

    private lateinit var database:DatabaseReference
//    private var storageReference = Firebase.storage.reference

//    private var progressiveArrayList:ArrayList<ProgressiveTaskElements> = arrayListOf()
    private var homeProgressiveArrayList:ArrayList<HomeProgressiveAdapter.HomeProgressiveElements> = arrayListOf()
    private var otherTaskArrayList:ArrayList<OtherTaskAdapter.OtherTaskElements> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeFragmentBinding = FragmentHomeBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        database = Firebase.database.reference
        homeFragmentBinding.progressiveRecyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        homeFragmentBinding.otherRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val user = auth.currentUser
        homeFragmentBinding.userName.text = user?.email

        val normalTaskListener = object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value == null)
                    return
                val temp = snapshot.value as HashMap<*,*>
                if(temp.isEmpty()){
                    homeFragmentBinding.noTaskText.visibility = View.VISIBLE
                    homeFragmentBinding.noTaskImageView.visibility = View.VISIBLE
                    homeFragmentBinding.addOtherTaskButton.visibility = View.VISIBLE
                    return
                }else{
                    homeFragmentBinding.noTaskText.visibility = View.GONE
                    homeFragmentBinding.noTaskImageView.visibility = View.GONE
                    homeFragmentBinding.addOtherTaskButton.visibility = View.GONE
                }

                if(temp.contains("taskId")){
                    mapToOtherList(temp)
                }else{
                    temp.forEach{
                        mapToOtherList(it.value as HashMap<*,*>)
                    }
                }

            }
            override fun onCancelled(error: DatabaseError) {}
        }

        val postListener = object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
//                Log.e("sample","${snapshot.value as HashMap<*,*>}")
                if(snapshot.value == null)
                    return
                val map = snapshot.value as HashMap<*,*>

                if(map.isEmpty()){
                    homeFragmentBinding.noProgressiveText.visibility = View.VISIBLE
                    homeFragmentBinding.noProgressiveImageView.visibility = View.VISIBLE
                    homeFragmentBinding.addNewProgressiveButton.visibility = View.VISIBLE
                    return
                }else{
                    homeFragmentBinding.noProgressiveText.visibility = View.GONE
                    homeFragmentBinding.noProgressiveImageView.visibility = View.GONE
                    homeFragmentBinding.addNewProgressiveButton.visibility = View.GONE
                }

                if(map.containsKey("taskId")){
                    mapToArrayList(map)
                }
                else{
                    map.forEach{
                        if(it.value !=null) {
                            val temp = it.value as HashMap<*, *>
                            mapToArrayList(temp)
                        }
                    }
                }
                homeFragmentBinding.progressiveRecyclerView.adapter = HomeProgressiveAdapter(requireActivity() as MainActivity,homeProgressiveArrayList)
            }

            override fun onCancelled(error: DatabaseError) {}

        }

        val dailyTaskListener = object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value == null)
                    return

                val temp = snapshot.value as HashMap<*,*>
                if(temp.isEmpty()){
                    homeFragmentBinding.noTaskText.visibility = View.VISIBLE
                    homeFragmentBinding.noTaskImageView.visibility = View.VISIBLE
                    homeFragmentBinding.addOtherTaskButton.visibility = View.VISIBLE
                    return
                }else{
                    homeFragmentBinding.noTaskText.visibility = View.GONE
                    homeFragmentBinding.noTaskImageView.visibility = View.GONE
                    homeFragmentBinding.addOtherTaskButton.visibility = View.GONE
                }

                if(temp.contains("taskId")){
                    mapToDailyOtherList(temp)
                }else{
                    temp.forEach{
                        mapToDailyOtherList(it.value as HashMap<*,*>)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        }

        database.child(SubTaskElements.TASK_TYPE_DAILY).child(auth.currentUser!!.uid).addValueEventListener(dailyTaskListener)
        database.child(SubTaskElements.TASK_TYPE_NORMAL).child(auth.currentUser!!.uid).addValueEventListener(normalTaskListener)
        database.child(SubTaskElements.TASK_TYPE_PROGRESSIVE).child(auth.currentUser!!.uid).addValueEventListener(postListener)

        homeFragmentBinding.otherRecyclerView.adapter = OtherTaskAdapter(otherTaskArrayList)



        return homeFragmentBinding.root
    }

    private fun mapToDailyOtherList(temp: HashMap<*, *>) {
        val taskId = temp[ProgressiveTaskElements.ID_KEY].toString()
        val taskName = temp[ProgressiveTaskElements.NAME_KEY].toString()
        val dailyType = temp[DailyTaskElements.DAILY_TYPE_KEY].toString().toInt()
        val taskStartTime = temp[ProgressiveTaskElements.START_TIME_KEY].toString()
        val taskEndTime = temp[ProgressiveTaskElements.END_TIME_KEY].toString()
        val duration = temp[ProgressiveTaskElements.DURATION_KEY].toString()

        val otherTaskElement = OtherTaskAdapter.OtherTaskElements(
            taskId = taskId,
            taskName = taskName,
            isDailyTask = true,
            duration = duration
        )
        if(dailyType == DailyTaskElements.TYPE_RANGE){
            val taskStartDate = temp[ProgressiveTaskElements.START_DATE_KEY].toString()
            val taskEndDate = temp[ProgressiveTaskElements.END_DATE_KEY].toString()

            otherTaskElement.taskStartingAt = "$taskStartDate $taskStartTime"
            otherTaskElement.taskEndingAt = "$taskEndDate $taskEndTime"
        }else{
            otherTaskElement.taskStartingAt = taskStartTime
            otherTaskElement.taskEndingAt = taskEndTime
        }
    }

    private fun mapToOtherList(temp: HashMap<*, *>) {
        val taskId = temp[ProgressiveTaskElements.ID_KEY].toString()
        val taskName = temp[ProgressiveTaskElements.NAME_KEY].toString()
        val taskStartTime = temp[ProgressiveTaskElements.START_TIME_KEY].toString()
        val taskEndTime = temp[ProgressiveTaskElements.END_TIME_KEY].toString()
        val taskStartDate = temp[ProgressiveTaskElements.START_DATE_KEY].toString()
        val taskEndDate = temp[ProgressiveTaskElements.END_DATE_KEY].toString()
        val taskDuration = temp[ProgressiveTaskElements.DURATION_KEY].toString()

        val startingFrom = "$taskStartDate $taskStartTime"
        val endingAt = "$taskEndDate $taskEndTime"

        val otherTaskElement = OtherTaskAdapter.OtherTaskElements(
            taskId = taskId,
            taskName = taskName,
            taskStartingAt = startingFrom,
            taskEndingAt = endingAt,
            duration = taskDuration
        )
        otherTaskArrayList.add(otherTaskElement)

    }

    private fun mapToArrayList(map: HashMap<*, *>) {
        val taskName:String = map[ProgressiveTaskElements.NAME_KEY].toString()
        val taskDuration:String = map[ProgressiveTaskElements.DURATION_KEY].toString()
        val taskProgress:String = map[ProgressiveTaskElements.PROGRESS_KEY].toString()
        val taskPriority:String = map[ProgressiveTaskElements.PRIORITY_KEY].toString()
        val taskId:String = map[ProgressiveTaskElements.ID_KEY].toString()

        val homeProgressiveElement = HomeProgressiveAdapter.HomeProgressiveElements(
            taskName = taskName,
            taskDuration = taskDuration,
            taskProgress = taskProgress,
            taskPriority = taskPriority,
            taskId = taskId
        )
        homeProgressiveArrayList.add(homeProgressiveElement)
    }


}