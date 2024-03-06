package com.example.myapplicationpls

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.CheckBox
import android.widget.Toast
import com.example.myapplicationpls.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class loginCompat: AppCompatActivity() {
    private lateinit var loginbtn: Button
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var remember: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize remember checkbox
        remember = findViewById(R.id.checkBoxRem)

        val preferences = getSharedPreferences("checkbox", Context.MODE_PRIVATE)
        val isChecked = preferences.getBoolean("isChecked", false)
        remember.isChecked = isChecked


        if (isChecked) {
            val intent = Intent(this, MenuPageCompat::class.java)
            startActivity(intent)
            finish()
        }

        binding.signupT.setOnClickListener {
            val intent = Intent(this, signUpFirebase::class.java)
            startActivity(intent)
        }

        remember.setOnCheckedChangeListener { _, isChecked ->
            val editor = preferences.edit()
            editor.putBoolean("isChecked", isChecked)
            editor.apply()
        }

        binding.buttonLogin.setOnClickListener {
            val login = binding.Lusername.text.toString()
            val pass = binding.Lpassword.text.toString()

            if (login.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(login, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, MenuPageCompat::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "User doesn't exist or wrong password!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Empty fields are not allowed!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
