package com.example.myapplicationpls

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*


private lateinit var auth: FirebaseAuth
private lateinit var database: FirebaseDatabase
private lateinit var weightRef: DatabaseReference
private lateinit var weightEditText: EditText
private lateinit var currentDate: String
private lateinit var lineChart: LineChart

class weightFragment : Fragment() {

    val database = FirebaseDatabase.getInstance("https://fitapp-loginbase-default-rtdb.europe-west1.firebasedatabase.app")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_weight, container, false)

        currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val datePick = view.findViewById<TextView>(R.id.datePick)
        datePick.text = currentDate
        val nextDate = view.findViewById<ImageButton>(R.id.nextDate)
        nextDate.visibility = View.INVISIBLE
        val backDate = view.findViewById<ImageButton>(R.id.backDate)
        lineChart = view.findViewById<LineChart>(R.id.testChart)
        generateWeightChart()
        val btnUpdate = view.findViewById<Button>(R.id.updateBTN)
        val welcomeUserText = view.findViewById<TextView>(R.id.welcomeUser)

        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid

            val nickRef = database.getReference("Users").child(uid).child("nickname")
            nickRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val nick = dataSnapshot.getValue(String::class.java)
                    welcomeUserText.text = "Hello $nick,"
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MyApp", "Error reading nickname: ${error.message}")
                }
            })
        }

        backDate.setOnClickListener {
            changeDate(-1)
            nextDate.visibility = View.VISIBLE
        }
        nextDate.setOnClickListener {
            changeDate(+1)
            if (currentDate == SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())) {
                nextDate.visibility = View.INVISIBLE
                btnUpdate.visibility = View.VISIBLE
            }
        }

        weightEditText = view.findViewById(R.id.currentlyWeight)
        auth = Firebase.auth

        if (user != null) {
            val uid = user.uid
            val weightRef = database.getReference("Users").child(uid).child("weight")
            weightRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val weight = dataSnapshot.getValue(Double::class.java)
                    weightEditText.setText("$weight")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MyApp", "Error reading weight: ${error.message}")
                }
            })
        }

        btnUpdate.setOnClickListener {
            Toast.makeText(requireContext(), "Updated!", Toast.LENGTH_LONG).show()
            if (user != null) {
                val uid = user.uid
                val weightRef = database.getReference("Users").child(uid).child("weightUpdate")
                    .child(currentDate)
                weightRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val weight = dataSnapshot.getValue(Double::class.java)
                        val newWeight = weightEditText.text.toString().toDouble()
                        weightRef.setValue(newWeight)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("MyApp", "Error reading weight: ${error.message}")
                    }
                })

                val weightRef2 = database.getReference("Users").child(uid).child("weight")
                weightRef2.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val weight = dataSnapshot.getValue(Double::class.java)
                        val newWeight = weightEditText.text.toString().toDouble()
                        weightRef2.setValue(newWeight)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("MyApp", "Error reading weight: ${error.message}")
                    }
                })
                data class User(
                    val activityLevel: String? = null,
                    val age: Int? = null,
                    val bmi: Double? = null,
                    val carbohydrates: Double? = null,
                    val fat: Double? = null,
                    val height: Double? = null,
                    val kcal: Double? = null,
                    val nickname: String? = null,
                    val protein: Double? = null,
                    val sex: String? = null,
                    val weight: Double? = null
                ) {
                    constructor() : this(null, null, null, null, null, null, null, null, null, null, null)
                }
                val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid)

                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val user = dataSnapshot.getValue(User::class.java)
                            val activityLevel = user?.activityLevel
                            val age = user?.age
                            val weight = user?.weight ?:0.0
                            val height = user?.height ?:0.0
                            val bmi = weight / (height*height) * 10000
                            val roundedBMI = String.format("%.2f", bmi)

                            val bmiRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("bmi")
                            bmiRef.setValue(roundedBMI.toDouble())
                                .addOnSuccessListener {
                                    Log.d("UpdateBMI", "BMI updated successfully.")
                                }
                                .addOnFailureListener {
                                    Log.e("UpdateBMI", "Failed to update BMI: ${it.message}")
                                }

                            Log.d("UserData", "Activity Level: $activityLevel")
                            Log.d("UserData", "Age: $age")
                            Log.d("UserData", "BMI: $bmi")
                            // i tak dalej...
                        } else {
                            Log.d("UserData", "User with UID $uid not found.")
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("UserData", "Error reading user data: ${databaseError.message}")
                    }
                })

            }

        }
        return view
    }
    private fun generateWeightChart() {
        val weights = listOf(
            Pair(1, 82.5f),
            Pair(2, 81.4f),
            Pair(3, 83.6f),
            Pair(4, 80.2f),
            Pair(5, 82.5f)
        )
        val today = Calendar.getInstance()
        val entries = ArrayList<Entry>()
        weights.forEach { pair ->
            entries.add(Entry(pair.first.toFloat(), pair.second))
        }

        val dataSet = LineDataSet(entries, "Weight")
        dataSet.color = Color.YELLOW
        dataSet.setCircleColor(Color.RED)
        dataSet.setDrawValues(false)

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        val description = Description()
        description.text = "Weight Over Time"
        lineChart.description = description

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.textColor = Color.YELLOW
        xAxis.valueFormatter = object : ValueFormatter() {
            private val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

            override fun getFormattedValue(value: Float): String {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = today.timeInMillis - ((weights.size - value.toInt()) * 24 * 60 * 60 * 1000)

                return sdf.format(calendar.time)
            }
        }

        val yAxis = lineChart.axisLeft
        yAxis.textColor = Color.YELLOW
        yAxis.setDrawGridLines(false)

        lineChart.axisRight.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.description.isEnabled = true

        lineChart.invalidate()
    }

    private fun updateWeight(selectedDate: String) {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val weightRef = database.getReference("Users").child(uid).child("weightUpdate").child(selectedDate)
            weightRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val weight = dataSnapshot.getValue(Double::class.java)
                    weightEditText.setText("$weight")

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MyApp", "Error reading weight: ${error.message}")
                }
            })

        }
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
        updateWeight(currentDate)

        val btnUpdate = view?.findViewById<Button>(R.id.updateBTN)
        if(currentDate != SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()))
        {
            btnUpdate?.visibility = View.INVISIBLE

        }
    }
}