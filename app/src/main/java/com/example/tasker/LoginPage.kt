package com.example.tasker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tasker.databinding.ActivityLoginPageBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import java.util.regex.Pattern

class LoginPage : AppCompatActivity() {

    private lateinit var loginPageBinding: ActivityLoginPageBinding
    private var showPassword: Boolean = false
    private var validPassword:Boolean = false
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        loginPageBinding = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(loginPageBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = Firebase.auth

        loginPageBinding.blurView.setBlur(this, loginPageBinding.blurView)

        loginPageBinding.passwordToggle.setOnClickListener {
            if (showPassword) {
                showPassword = false
                loginPageBinding.passwordInput.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                loginPageBinding.passwordToggle.setImageResource(R.drawable.view)
            } else {
                showPassword = true
                loginPageBinding.passwordInput.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                loginPageBinding.passwordToggle.setImageResource(R.drawable.hide_password_icon)
            }
        }

        loginPageBinding.signInLink.setOnClickListener {
            val intent = Intent(this, SignInPage::class.java)
            startActivity(intent)
            finish()
        }

        loginPageBinding.apply {
            passwordInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                @SuppressLint("SetTextI18n")
                override fun afterTextChanged(s: Editable?) {
                    s?.let {
                        passwordComplexity(it.toString())
                    }
                }
            })

        }

        loginPageBinding.loginButton.setOnClickListener {
            val email = loginPageBinding.emailInput.text.toString()
            val password = loginPageBinding.passwordInput.text.toString()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Email is empty", Toast.LENGTH_SHORT).show()
            } else if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Password is empty", Toast.LENGTH_SHORT).show()
            }else {
                auth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this){task ->
                        if(task.isSuccessful){
                            val intent = Intent(this,MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else{
                            Toast.makeText(this,"Authentication Failed",Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun passwordComplexity(text: String): Int? {

        val atLeastOneLowerCase = Pattern.compile(".*[a-z]")
        val atLeastOneUpperCase = Pattern.compile(".*[A-Z]")
        val noSpace = Pattern.compile(" ")
        val atLeastOneSpecial = Pattern.compile(".*[!@#$%&*?,.;:=+/|~-]")
        val atLeastOneDigit = Pattern.compile(".*[0-9]")
        val atLeastEightCharLength = Pattern.compile(".{8,}")
        val noInjection = Regex(".*['`{}()<>]")


        if (!text.contains(atLeastOneUpperCase.toRegex())) {
            loginPageBinding.passwordError.text = "Should contain at least one Upper Case Character"
//            loginPageBinding.passwordErrorLayout.visibility = View.INVISIBLE
            validPassword = false
        } else if (!text.contains(atLeastOneLowerCase.toRegex())) {
            loginPageBinding.passwordError.text = "Should contain at least one Lower Case Character"
//            loginPageBinding.passwordErrorLayout.visibility = View.VISIBLE
            validPassword = false
        } else if (!text.contains(atLeastOneSpecial.toRegex())) {
            loginPageBinding.passwordError.text = "Should contain at least one Special Character"
//            loginPageBinding.passwordErrorLayout.visibility = View.VISIBLE
            validPassword = false
        } else if (!text.contains(atLeastOneDigit.toRegex())) {
            loginPageBinding.passwordError.text = "Should contain at least one Digit"
//            loginPageBinding.passwordErrorLayout.visibility = View.VISIBLE
            validPassword = false
        } else if (text.contains(noSpace.toRegex())) {
            loginPageBinding.passwordError.text = "Should not contain any spaces"
//            loginPageBinding.passwordErrorLayout.visibility = View.VISIBLE
            validPassword = false
        } else if (!text.contains(atLeastEightCharLength.toRegex())) {
            loginPageBinding.passwordError.text = "Should contain at least 8 Characters"
//            loginPageBinding.passwordErrorLayout.visibility = View.VISIBLE
            validPassword = false
        } else if (text.contains(noInjection)) {
            loginPageBinding.passwordError.text = "Should not contain any Injection characters"
//            loginPageBinding.passwordErrorLayout.visibility = View.VISIBLE
            validPassword = false
        } else {
//            loginPageBinding.passwordErrorLayout.visibility = View.INVISIBLE
            validPassword = true
            if (text.length <= 9)
                return WEAK_PASSWORD
            else if (text.length in 10..12)
                return MEDIUM_PASSWORD
            else if (text.length in 13..16)
                return STRONG_PASSWORD
            else
                return SUPER_STRONG_PASSWORD
        }
        return null
    }

    companion object {
        const val WEAK_PASSWORD = 281
        const val MEDIUM_PASSWORD = -29
        const val STRONG_PASSWORD = 92
        const val SUPER_STRONG_PASSWORD = -18
    }
}