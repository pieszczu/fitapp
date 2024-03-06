package com.example.myapplicationpls

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import com.example.myapplicationpls.databinding.SignupBinding
import com.google.firebase.auth.FirebaseAuth

class signUpFirebase : AppCompatActivity() {

    private lateinit var binding: SignupBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.btn3s.setOnClickListener{
            val email = binding.sUsername.text.toString()
            val pass = binding.sPass.text.toString()
            val cpass = binding.sPassConfirm.text.toString()

            if(email.isNotEmpty() && pass.isNotEmpty() && cpass.isNotEmpty()){
                if(pass==cpass){
                    firebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener {
                        if(it.isSuccessful){
                            val intent = Intent(this, signUpUserProfile::class.java)
                            startActivity(intent)
                        }
                    }
                }
                else{
                    Toast.makeText(this,"Password are not matching!", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this,"Empty fields are nt allowed!", Toast.LENGTH_SHORT).show()
            }
        }
        }
    }
