package com.example.tasker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tasker.databinding.ActivitySignInPageBinding
import com.github.furkankaplan.fkblurview.FKBlurView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import java.util.regex.Pattern
import kotlin.math.sign

class SignInPage : AppCompatActivity() {

    private lateinit var signInBinding: ActivitySignInPageBinding
    private var showNewPassword: Boolean = false
    private var showConfirmPassword: Boolean = false
    private lateinit var auth: FirebaseAuth
    private var validConfirm:Boolean = false
    private var validNew:Boolean = false


    private lateinit var database:DatabaseReference

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser!=null) {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        signInBinding = ActivitySignInPageBinding.inflate(layoutInflater)
        database = Firebase.database.reference

        setContentView(signInBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val blurView: FKBlurView = findViewById(R.id.blur_view)
        blurView.setBlur(this, blurView)
        auth = Firebase.auth


        signInBinding.confirmPasswordToggle.setOnClickListener {
            if (showConfirmPassword) {
                showConfirmPassword = false
                signInBinding.confirmPasswordInput.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                signInBinding.confirmPasswordToggle.setImageResource(R.drawable.view)
            } else {
                showConfirmPassword = true
                signInBinding.confirmPasswordInput.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                signInBinding.confirmPasswordToggle.setImageResource(R.drawable.hide_password_icon)
            }
        }

        signInBinding.newPasswordToggle.setOnClickListener {
            if (showNewPassword) {
                showNewPassword = false
                signInBinding.newPasswordInput.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                signInBinding.newPasswordToggle.setImageResource(R.drawable.view)
            } else {
                showNewPassword = true
                signInBinding.newPasswordInput.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                signInBinding.newPasswordToggle.setImageResource(R.drawable.hide_password_icon)
            }
        }


        signInBinding.newPasswordInput.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.let{
                    passwordComplexity(it.toString())
                }
            }
        })

        signInBinding.apply{
            confirmPasswordInput.addTextChangedListener(object:TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                @SuppressLint("SetTextI18n")
                override fun afterTextChanged(s: Editable?) {
                    val newPassword = signInBinding.newPasswordInput.text.toString()
                    if(newPassword!=s?.toString()) {
                        signInBinding.confirmPasswordError.text = "Confirm Password does not match"
                        signInBinding.confirmPasswordErrorLayout.visibility = View.VISIBLE
                        validConfirm = false
                    }
                    else {
                        signInBinding.confirmPasswordErrorLayout.visibility = View.INVISIBLE
                        validConfirm = true
                    }
                }
            })
        }

        signInBinding.loginLink.setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
            finish()
        }

        signInBinding.signInButton.setOnClickListener {
            if(validConfirm && validNew){
                val email = signInBinding.emailInput.text
                val password = signInBinding.newPasswordInput.text
                auth.createUserWithEmailAndPassword(email!!.toString(),password!!.toString())
                    .addOnCompleteListener(this) {task ->
                        if(task.isSuccessful){

                            val userID = auth.currentUser!!.uid
                            var sum = 0
                            for( i in userID.length - 7..<userID.length)
                                sum = (sum*10) + userID[i].toInt()

                            val user = UserDetails(userID, "GuestUser$sum",auth.currentUser!!.email)
                            database.child("users").child(userID).setValue(user)



                            val intent = Intent(this,LoginPage::class.java)
                            startActivity(intent)
                            finish()
                        }else{
                            Toast.makeText(this,"Authentication Failed",Toast.LENGTH_SHORT).show()
                        }
                    }
            }else{
                Toast.makeText(this,"InValid Password",Toast.LENGTH_SHORT).show()
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun passwordComplexity(text:String):Int?{

        val atLeastOneLowerCase = Pattern.compile(".*[a-z]")
        val atLeastOneUpperCase = Pattern.compile(".*[A-Z]")
        val noSpace = Pattern.compile(" ")
        val atLeastOneSpecial = Pattern.compile(".*[!@#$%&*?,.;:=+/|~-]")
        val atLeastOneDigit = Pattern.compile(".*[0-9]")
        val atLeastEightCharLength = Pattern.compile(".{8,}")
        val noInjection = Regex(".*['`{}()<>]")


        if(!text.contains(atLeastOneUpperCase.toRegex())) {
            signInBinding.newPasswordError.text = "Should contain at least one Upper Case Character"
            signInBinding.newPasswordErrorLayout.visibility = View.VISIBLE
            validNew = false
        }
        else if(!text.contains(atLeastOneLowerCase.toRegex())){
            signInBinding.newPasswordError.text = "Should contain at least one Lower Case Character"
            signInBinding.newPasswordErrorLayout.visibility = View.VISIBLE
            validNew = false
        }
        else if(!text.contains(atLeastOneSpecial.toRegex())){
            signInBinding.newPasswordError.text = "Should contain at least one Special Character"
            signInBinding.newPasswordErrorLayout.visibility = View.VISIBLE
            validNew = false
        }
        else if(!text.contains(atLeastOneDigit.toRegex())){
            signInBinding.newPasswordError.text = "Should contain at least one Digit"
            signInBinding.newPasswordErrorLayout.visibility = View.VISIBLE
            validNew = false
        }
        else if(text.contains(noSpace.toRegex())){
            signInBinding.newPasswordError.text = "Should not contain any spaces"
            signInBinding.newPasswordErrorLayout.visibility = View.VISIBLE
            validNew = false
        }
        else if(!text.contains(atLeastEightCharLength.toRegex())){
            signInBinding.newPasswordError.text = "Should contain at least 8 Characters"
            signInBinding.newPasswordErrorLayout.visibility = View.VISIBLE
            validNew = false
        }
        else if(text.contains(noInjection)){
            signInBinding.newPasswordError.text = "Should not contain any Injection characters"
            signInBinding.newPasswordErrorLayout.visibility = View.VISIBLE
            validNew = false
        }
        else{
            signInBinding.newPasswordErrorLayout.visibility = View.INVISIBLE
            validNew = true
            if(text.length <=9)
                return WEAK_PASSWORD
            else if(text.length in 10..12)
                return MEDIUM_PASSWORD
            else if(text.length in 13..16)
                return STRONG_PASSWORD
            else
                return SUPER_STRONG_PASSWORD
        }
        return null
    }
    companion object{
        const val WEAK_PASSWORD = 281
        const val MEDIUM_PASSWORD = -29
        const val STRONG_PASSWORD = 92
        const val SUPER_STRONG_PASSWORD = -18
    }

}