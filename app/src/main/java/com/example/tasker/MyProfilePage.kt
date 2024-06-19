package com.example.tasker

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.tasker.databinding.FragmentMyProfilePageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class MyProfilePage : Fragment() {


    private lateinit var myProfilePageBinding: FragmentMyProfilePageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var storage = Firebase.storage(UserDetails.fileStorageLink)

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myProfilePageBinding = FragmentMyProfilePageBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        database = Firebase.database.reference
        Log.e("currentUser", auth.currentUser.toString())



        database.child("users").child(auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                Log.e("value", "${it.value}")
                val temp = it.value as HashMap<*, *>

                myProfilePageBinding.myProfileUserName.text = temp["userName"].toString()
                myProfilePageBinding.myProfileUserEmail.text = temp["userEmail"].toString()

                val mutual = temp["mutual"]
                val followers = temp["followers"]
                val following = temp["following"]
                val storageReference = storage.reference


                myProfilePageBinding.myProfileMutualConnections.text = "$mutual\nMutual"
                myProfilePageBinding.myProfileFollowers.text = "$followers\nFollowers"
                myProfilePageBinding.myProfileFollowing.text = "$following\nFollowing"
                allSpannable()

                if(!temp.containsKey("userBio")){
                    myProfilePageBinding.myProfileAboutMeHeading.visibility = View.GONE
                    myProfilePageBinding.myProfileAboutMeText.visibility = View.GONE
                    myProfilePageBinding.myProfileAddBioLayout.visibility = View.VISIBLE
                }else{
                    myProfilePageBinding.myProfileAboutMeHeading.visibility = View.VISIBLE
                    myProfilePageBinding.myProfileAboutMeText.visibility = View.VISIBLE
                    myProfilePageBinding.myProfileAddBioLayout.visibility = View.GONE
                }


                if (!temp.containsKey("userProfileImage"))
                    myProfilePageBinding.myProfileUserProfile.setImageResource(R.drawable.profile_user)
                else {
                    storageReference.child("users/").child("${temp["userId"]}/${temp["userProfileImage"]}").downloadUrl
                        .addOnSuccessListener { uri ->
                            myProfilePageBinding.myProfileUserProfile.setImageURI(uri)
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(),"Failed to load Profile Image",Toast.LENGTH_SHORT).show()
                        }
                }


                if(!temp.containsKey("userBackgroundImage"))
                    myProfilePageBinding.myProfileUserBackground.setImageResource(R.drawable.bio_image)
                else{
                    storageReference.child("users/").child("${temp["userId"]}/${temp["userBackgroundImage"]}").downloadUrl
                        .addOnSuccessListener { uri ->
                            myProfilePageBinding.myProfileUserProfile.setImageURI(uri)
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(),"Failed to load Background Image",Toast.LENGTH_SHORT).show()
                        }
                }


//
//                storageReference.child()


//                myProfilePageBinding.myProfileUserProfile.setImageResource()
            }
            .addOnFailureListener {
                Log.e("value", "Failed")
            }



        allSpannable()




        myProfilePageBinding.myProfileLogOutButton.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(requireActivity(), LoginPage::class.java)
            startActivity(intent)
            requireActivity().finish()
        }





        return myProfilePageBinding.root
    }

    private fun allSpannable() {
        setSpannableString(myProfilePageBinding.myProfileMutualConnections)
        setSpannableString(myProfilePageBinding.myProfileFollowers)
        setSpannableString(myProfilePageBinding.myProfileFollowing)
    }


    @SuppressLint("SetTextI18n")
    private fun setSpannableString(view: TextView) {
        var index = 0
        for (i in view.text.indices) {
            if (view.text[i] == '\n') {
                index = i
                break
            }
        }
        val spannableStringMutual = SpannableString(view.text)
        spannableStringMutual.setSpan(
            RelativeSizeSpan(2f),
            0,
            index,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        view.text = spannableStringMutual
    }
}