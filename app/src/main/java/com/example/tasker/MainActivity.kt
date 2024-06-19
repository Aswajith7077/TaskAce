package com.example.tasker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.tasker.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private var currentlySelectedFragment:Int = HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        replaceFragment(HomeFragment())
        mainBinding.homeButton.setImageResource(R.drawable.selected_home)
        mainBinding.dashHome.visibility = View.VISIBLE

        mainBinding.profileButton.setOnClickListener {
            if(currentlySelectedFragment != PROFILE) {
                replaceFragment(MyProfilePage())
                mainBinding.profileButton.setImageResource(R.drawable.selected_user)
                mainBinding.homeButton.setImageResource(R.drawable.home)
                mainBinding.dashHome.visibility = View.GONE
                mainBinding.dashProfile.visibility = View.VISIBLE
                currentlySelectedFragment = HOME
                currentlySelectedFragment = PROFILE
            }
        }

        mainBinding.homeButton.setOnClickListener {
            if(currentlySelectedFragment != HOME){
                replaceFragment(HomeFragment())
                mainBinding.profileButton.setImageResource(R.drawable.user)
                mainBinding.homeButton.setImageResource(R.drawable.selected_home)
                mainBinding.dashHome.visibility = View.VISIBLE
                mainBinding.dashProfile.visibility = View.GONE
                currentlySelectedFragment = HOME
            }
        }


        mainBinding.newButton.setOnClickListener{
            val intent = Intent(this,CreateProgressiveTask::class.java)
            startActivity(intent)
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(mainBinding.frameLayout.id,fragment)
            .commit()
    }
    companion object{
        const val PROFILE = 3082
        const val HOME = -2837
    }
}