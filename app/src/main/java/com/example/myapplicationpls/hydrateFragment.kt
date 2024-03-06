package com.example.myapplicationpls

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

private lateinit var database: FirebaseDatabase
private lateinit var auth: FirebaseAuth
private lateinit var waterDaily: TextView
private lateinit var waterRef: DatabaseReference
private lateinit var plusButton: ImageButton
private lateinit var minusButton: ImageButton
private lateinit var backDate: ImageButton
private lateinit var currentDate: String

class DateAxisValueFormatter : ValueFormatter() {
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val date = Date(value.toLong())
        return dateFormat.format(date)
    }
}
class HydrateFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hydrate, container, false)
        database = FirebaseDatabase.getInstance()
        auth = Firebase.auth
        waterDaily = view.findViewById(R.id.waterDaily)

        currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val datePick = view.findViewById<TextView>(R.id.datePick)
        datePick.text = currentDate
        updateWaterIntake(currentDate)
        val nextDate = view.findViewById<ImageButton>(R.id.nextDate)
        nextDate.visibility = View.INVISIBLE
        val backDate = view.findViewById<ImageButton>(R.id.backDate)

        val plusButton = view?.findViewById<ImageButton>(R.id.addWater)
        plusButton?.setOnClickListener {
            incrementWaterIntake(0.25)
        }

        val minusButton = view?.findViewById<ImageButton>(R.id.deleteWater)
        minusButton?.setOnClickListener {
            decrementWaterIntake(0.25)
        }

        backDate.setOnClickListener {
            changeDate(-1)
            nextDate.visibility = View.VISIBLE
        }

        nextDate.setOnClickListener{
            changeDate(+1)
            if(currentDate == SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()))
            {
                nextDate.visibility = View.INVISIBLE
            }
        }
        return view
    }

    private fun changeDate(daysToAdd: Int) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDateDate = dateFormat.parse(currentDate)

        calendar.time = currentDateDate
        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd)

        currentDate = dateFormat.format(calendar.time)

        val datePick = view?.findViewById<TextView>(R.id.datePick)
        datePick?.text = currentDate
        updateWaterIntake(currentDate)

        val plusButton = view?.findViewById<ImageButton>(R.id.addWater)
        plusButton?.setOnClickListener {
            incrementWaterIntake(0.25)
        }

        val minusButton = view?.findViewById<ImageButton>(R.id.deleteWater)
        minusButton?.setOnClickListener {
            decrementWaterIntake(0.25)
        }

        if(currentDate != SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()))
        {
            plusButton?.visibility = View.INVISIBLE
            minusButton?.visibility = View.INVISIBLE
        }
        else
        {
            plusButton?.visibility = View.VISIBLE
            minusButton?.visibility = View.VISIBLE
        }
    }

    private fun updateWaterIntake(selectedDate: String) {
        val database =
            FirebaseDatabase.getInstance("https://fitapp-loginbase-default-rtdb.europe-west1.firebasedatabase.app")
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid

            waterRef =
                database.getReference("Users").child(uid).child("waterIntake").child(selectedDate)
            waterRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        waterRef.setValue(0)

                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })

            waterRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val waterIntake = dataSnapshot.getValue(Double::class.java) ?: 0.0
                    val progressBar = view?.findViewById<ProgressBar>(R.id.progressBar)
                    waterDaily.text = "$waterIntake/2l"
                    val konwersjaWater = waterIntake * 100.0
                    progressBar?.progress = konwersjaWater.toInt()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }
    }

    private fun incrementWaterIntake(amount: Double) {
        waterRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentWaterIntake = dataSnapshot.getValue(Double::class.java) ?: 0.0
                val newWaterIntake = currentWaterIntake + amount
                waterRef.setValue(newWaterIntake)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun decrementWaterIntake(amount: Double) {
        waterRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentWaterIntake = dataSnapshot.getValue(Double::class.java) ?: 0.0
                val newWaterIntake = maxOf(0.0, currentWaterIntake - amount)
                waterRef.setValue(newWaterIntake)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }
}