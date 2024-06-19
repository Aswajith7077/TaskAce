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
    private var storageReference = Firebase.storage.reference

//    private var progressiveArrayList:ArrayList<ProgressiveTaskElements> = arrayListOf()
    private var homeProgressiveArrayList:ArrayList<HomeProgressiveAdapter.HomeProgressiveElements> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeFragmentBinding = FragmentHomeBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        database = Firebase.database.reference
        homeFragmentBinding.progressiveRecyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)


        val user = auth.currentUser
        homeFragmentBinding.userName.text = user?.email

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
                }else{
                    homeFragmentBinding.noProgressiveText.visibility = View.GONE
                    homeFragmentBinding.noProgressiveImageView.visibility = View.GONE
                    homeFragmentBinding.addNewProgressiveButton.visibility = View.GONE
                }

                if(map.isEmpty())
                    return
                else if(map.containsKey("taskId")){
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
        database.child(SubTaskElements.TASK_TYPE_PROGRESSIVE).child(auth.currentUser!!.uid).addValueEventListener(postListener)

        return homeFragmentBinding.root
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