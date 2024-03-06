package com.example.myapplicationpls

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.math.BigDecimal
import java.math.RoundingMode


class settingsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val updateButton = view.findViewById<Button>(R.id.updateButton)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance("https://fitapp-loginbase-default-rtdb.europe-west1.firebasedatabase.app")

        updateButton.setOnClickListener {
            updateUserProfile()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = auth.currentUser
        val userNicknameEditText = view.findViewById<EditText>(R.id.userNickname)
        val userWeightEditText = view.findViewById<EditText>(R.id.userWeight)
        val userHeightEditText = view.findViewById<EditText>(R.id.userHeight)
        val userLengthEditText = view.findViewById<EditText>(R.id.userLength)
        val userBMIEditText = view.findViewById<TextView>(R.id.userBMI)
        var sex = ""
        user?.let { currentUser ->
            val uid = currentUser.uid
            val databaseRef = database.getReference("Users").child(uid)

            databaseRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userData = dataSnapshot.value as? Map<String, Any>
                    userData?.let {
                        userNicknameEditText.setText(userData["nickname"].toString())
                        userWeightEditText.setText(userData["weight"].toString())
                        userHeightEditText.setText(userData["height"].toString())
                        userLengthEditText.setText(userData["strideLength"].toString())
                        sex = (userData["sex"].toString())
                        val weight = userData["weight"].toString().toDouble()
                        val height = userData["height"].toString().toDouble()
                        val bmi = calculateBMI(weight, height)
                        val roundedBmi = BigDecimal(bmi).setScale(2, RoundingMode.HALF_EVEN).toDouble()
                        userBMIEditText.text = roundedBmi.toString()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("MyApp", "Error reading user data: ${databaseError.message}")
                }
            })
        }
    }

    private fun updateUserProfile() {
        val userNicknameEditText = view?.findViewById<EditText>(R.id.userNickname)
        val userWeightEditText = view?.findViewById<EditText>(R.id.userWeight)
        val userHeightEditText = view?.findViewById<EditText>(R.id.userHeight)
        val userLengthEditText = view?.findViewById<EditText>(R.id.userLength)
        val userBMIEditText = view?.findViewById<TextView>(R.id.userBMI)

        val user = auth.currentUser
        user?.let { currentUser ->
            val uid = currentUser.uid
            val databaseRef = database.getReference("Users").child(uid)

            val nickname = userNicknameEditText?.text.toString()
            val weight = userWeightEditText?.text.toString().toDouble()
            val height = userHeightEditText?.text.toString().toDouble()
            val length = userLengthEditText?.text.toString().toDouble()

            databaseRef.child("nickname").setValue(nickname)
            databaseRef.child("weight").setValue(weight)
            databaseRef.child("height").setValue(height)
            databaseRef.child("strideLength").setValue(length)

            val bmi = calculateBMI(weight, height)
            val roundedBmi = BigDecimal(bmi).setScale(2, RoundingMode.HALF_EVEN).toDouble()
            databaseRef.child("bmi").setValue(roundedBmi)

            userBMIEditText?.text = roundedBmi.toString()
        }
        Toast.makeText(requireContext(), "Updated!", Toast.LENGTH_LONG).show()
    }

    private fun calculateBMI(weight: Double, height: Double): Double {
        val height = height / 100.0
        return weight / (height * height)
    }
}