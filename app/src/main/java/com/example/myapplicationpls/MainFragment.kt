package com.example.myapplicationpls

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.*
class PedometerPrefsManager(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences("pedometer_prefs", Context.MODE_PRIVATE)

    fun saveTotalSteps(totalSteps: Float) {
        sharedPreferences.edit().putFloat("total_steps", totalSteps).apply()
    }

    fun loadTotalSteps(): Float {
        return sharedPreferences.getFloat("total_steps", 0f)
    }
}

class MainFragment : Fragment(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var running = false
    private var currentSteps = 0f
    private var previousSteps = 0f
    private lateinit var prefsManager: PedometerPrefsManager

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var welcomeTextTest: TextView
    private lateinit var welcomeProteinText: TextView
    private lateinit var welcomeFatText: TextView
    private lateinit var welcomeCarbsText: TextView
    private lateinit var welcomeKcalText: TextView


    //private lateinit var showTokenText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        prefsManager = PedometerPrefsManager(requireContext())
        previousSteps = prefsManager.loadTotalSteps()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(ContentValues.TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            //  val token = task.result
            //showTokenText = view.findViewById(R.id.testowytoken)
            //showTokenText.setText(token)
        })
        val currentDate = SimpleDateFormat(
            "dd-MM-yyyy",
            Locale.getDefault()
        ).format(Date())
        auth = Firebase.auth
        val database = FirebaseDatabase.getInstance("https://fitapp-loginbase-default-rtdb.europe-west1.firebasedatabase.app") //region!!!!!!!!!!!!!!!!!!!!!!!!!!1
        welcomeTextTest = view.findViewById(R.id.testowanazwa)
        welcomeProteinText = view.findViewById(R.id.proteinDaily)
        welcomeFatText = view.findViewById(R.id.fatDaily)
        welcomeCarbsText = view.findViewById(R.id.carbsDaily)
        welcomeKcalText = view.findViewById(R.id.kcalDaily)

        val user = auth.currentUser
        if (user != null) {
            // Jeśli użytkownik jest zalogowany, pobierz jego nick z Firebase Realtime Database
            val uid = user.uid

            val nickRef = database.getReference("Users").child(uid).child("nickname")
            nickRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val nick = dataSnapshot.getValue(String::class.java)
                    welcomeTextTest.text = "Hello $nick"
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MyApp", "Error reading nickname: ${error.message}")
                    // Obsłuż błąd odczytu danych
                }
            })

            // Define the updateProteinText function outside of the ValueEventListener
            fun updateProteinText(totalProteinBreakfast: Double, totalProteinLunch: Double, totalProteinDinner: Double, targetProtein: Double?) {
                val totalProtein = totalProteinBreakfast + totalProteinLunch + totalProteinDinner
                val roundedTotalProtein = String.format("%.2f", totalProtein)
                val proteinText = if (targetProtein != null) {
                    "$roundedTotalProtein/$targetProtein g"
                } else {
                    "$roundedTotalProtein g"
                }
                welcomeProteinText.text = proteinText
            }

            fun updateFatText(totalFatBreakfast: Double, totalFatLunch: Double, totalFatDinner: Double, targetFat: Double?) {
                val totalFat = totalFatBreakfast + totalFatLunch + totalFatDinner
                val roundedTotalFat = String.format("%.2f", totalFat)
                val FatText = if (targetFat != null) {
                    "$roundedTotalFat/$targetFat g"
                } else {
                    "$roundedTotalFat g"
                }
                welcomeFatText.text = FatText
            }

            fun updateCarbsText(totalCarbsBreakfast: Double, totalCarbsLunch: Double, totalCarbsDinner: Double, targetCarbs: Double?) {
                val totalCarbs = totalCarbsBreakfast + totalCarbsLunch + totalCarbsDinner
                val roundedTotalCarbs = String.format("%.2f", totalCarbs)
                val CarbsText = if (targetCarbs != null) {
                    "$roundedTotalCarbs/$targetCarbs g"
                } else {
                    "$roundedTotalCarbs g"
                }
                welcomeCarbsText.text = CarbsText
            }

            fun updateKcalText(totalKcalBreakfast: Double, totalKcalLunch: Double, totalKcalDinner: Double, targetKcal: Double?) {
                val totalKcal = totalKcalBreakfast + totalKcalLunch + totalKcalDinner
                val roundedTotalKcal = String.format("%.2f", totalKcal)
                val KcalText = if (targetKcal != null) {
                    "$roundedTotalKcal/$targetKcal"
                } else {
                    "$roundedTotalKcal"
                }
                welcomeKcalText.text = KcalText
            }



            val proteinRef = database.getReference("Users").child(uid).child("protein")
            proteinRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val protein = dataSnapshot.getValue(Double::class.java)

                    val myRefBreakfast = database.getReference("Users").child(uid).child("Breakfast").child(currentDate)
                    val myRefLunch = database.getReference("Users").child(uid).child("Lunch").child(currentDate)
                    val myRefDinner = database.getReference("Users").child(uid).child("Dinner").child(currentDate)

                    var totalProteinBreakfast = 0.0
                    var totalProteinLunch = 0.0
                    var totalProteinDinner = 0.0

                    // ValueEventListener for Breakfast
                    myRefBreakfast.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(breakfastSnapshot: DataSnapshot) {
                            totalProteinBreakfast = 0.0
                            for (productSnapshot in breakfastSnapshot.children) {
                                val productData = productSnapshot.getValue(Product::class.java)
                                totalProteinBreakfast += productData?.protein ?: 0.0
                            }
                            updateProteinText(totalProteinBreakfast, totalProteinLunch, totalProteinDinner, protein)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e("MyApp", "Error reading breakfast data: ${error.message}")
                        }
                    })

                    // ValueEventListener for Lunch
                    myRefLunch.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(lunchSnapshot: DataSnapshot) {
                            totalProteinLunch = 0.0
                            for (productSnapshot in lunchSnapshot.children) {
                                val productData = productSnapshot.getValue(Product::class.java)
                                totalProteinLunch += productData?.protein ?: 0.0
                            }
                            updateProteinText(totalProteinBreakfast, totalProteinLunch, totalProteinDinner, protein)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e("MyApp", "Error reading lunch data: ${error.message}")
                        }
                    })

                    // ValueEventListener for Dinner
                    myRefDinner.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dinnerSnapshot: DataSnapshot) {
                            totalProteinDinner = 0.0
                            for (productSnapshot in dinnerSnapshot.children) {
                                val productData = productSnapshot.getValue(Product::class.java)
                                totalProteinDinner += productData?.protein ?: 0.0
                            }
                            updateProteinText(totalProteinBreakfast, totalProteinLunch, totalProteinDinner, protein)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e("MyApp", "Error reading dinner data: ${error.message}")
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MyApp", "Error reading protein value: ${error.message}")
                }
            })





            val fatRef = database.getReference("Users").child(uid).child("fat")
            fatRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val fat = dataSnapshot.getValue(Double::class.java)

                    val myRefBreakfast = database.getReference("Users").child(uid).child("Breakfast").child(currentDate)
                    val myRefLunch = database.getReference("Users").child(uid).child("Lunch").child(currentDate)
                    val myRefDinner = database.getReference("Users").child(uid).child("Dinner").child(currentDate)

                    var totalFatBreakfast = 0.0
                    var totalFatLunch = 0.0
                    var totalFatDinner = 0.0

                    // ValueEventListener for Breakfast
                    myRefBreakfast.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(breakfastSnapshot: DataSnapshot) {
                            totalFatBreakfast = 0.0
                            for (productSnapshot in breakfastSnapshot.children) {
                                val productData = productSnapshot.getValue(Product::class.java)
                                totalFatBreakfast += productData?.fat ?: 0.0
                            }
                            updateFatText(totalFatBreakfast, totalFatLunch, totalFatDinner, fat)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e("MyApp", "Error reading breakfast data: ${error.message}")
                        }
                    })

                    // ValueEventListener for Lunch
                    myRefLunch.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(lunchSnapshot: DataSnapshot) {
                            totalFatLunch = 0.0
                            for (productSnapshot in lunchSnapshot.children) {
                                val productData = productSnapshot.getValue(Product::class.java)
                                totalFatLunch += productData?.fat ?: 0.0
                            }
                            updateFatText(totalFatBreakfast, totalFatLunch, totalFatDinner, fat)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e("MyApp", "Error reading lunch data: ${error.message}")
                        }
                    })

                    // ValueEventListener for Dinner
                    myRefDinner.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dinnerSnapshot: DataSnapshot) {
                            totalFatDinner = 0.0
                            for (productSnapshot in dinnerSnapshot.children) {
                                val productData = productSnapshot.getValue(Product::class.java)
                                totalFatDinner += productData?.fat ?: 0.0
                            }
                            updateFatText(totalFatBreakfast, totalFatLunch, totalFatDinner, fat)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e("MyApp", "Error reading dinner data: ${error.message}")
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MyApp", "Error reading protein value: ${error.message}")
                }
            })

            val carbsRef = database.getReference("Users").child(uid).child("carbohydrates")
            carbsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val carbs = dataSnapshot.getValue(Double::class.java)

                    val myRefBreakfast = database.getReference("Users").child(uid).child("Breakfast").child(currentDate)
                    val myRefLunch = database.getReference("Users").child(uid).child("Lunch").child(currentDate)
                    val myRefDinner = database.getReference("Users").child(uid).child("Dinner").child(currentDate)

                    var totalCarbsBreakfast = 0.0
                    var totalCarbsLunch = 0.0
                    var totalCarbsDinner = 0.0

                    // ValueEventListener for Breakfast
                    myRefBreakfast.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(breakfastSnapshot: DataSnapshot) {
                            totalCarbsBreakfast = 0.0
                            for (productSnapshot in breakfastSnapshot.children) {
                                val productData = productSnapshot.getValue(Product::class.java)
                                totalCarbsBreakfast += productData?.carbohydrates ?: 0.0
                            }
                            updateCarbsText(totalCarbsBreakfast, totalCarbsLunch, totalCarbsDinner, carbs)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e("MyApp", "Error reading breakfast data: ${error.message}")
                        }
                    })

                    // ValueEventListener for Lunch
                    myRefLunch.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(lunchSnapshot: DataSnapshot) {
                            totalCarbsLunch = 0.0
                            for (productSnapshot in lunchSnapshot.children) {
                                val productData = productSnapshot.getValue(Product::class.java)
                                totalCarbsLunch += productData?.carbohydrates ?: 0.0
                            }
                            updateCarbsText(totalCarbsBreakfast, totalCarbsLunch, totalCarbsDinner, carbs)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e("MyApp", "Error reading lunch data: ${error.message}")
                        }
                    })

                    // ValueEventListener for Dinner
                    myRefDinner.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dinnerSnapshot: DataSnapshot) {
                            totalCarbsDinner = 0.0
                            for (productSnapshot in dinnerSnapshot.children) {
                                val productData = productSnapshot.getValue(Product::class.java)
                                totalCarbsDinner += productData?.carbohydrates ?: 0.0
                            }
                            updateCarbsText(totalCarbsBreakfast, totalCarbsLunch, totalCarbsDinner, carbs)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e("MyApp", "Error reading dinner data: ${error.message}")
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MyApp", "Error reading protein value: ${error.message}")
                }
            })

            val kcalRef = database.getReference("Users").child(uid).child("kcal")
            kcalRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val kcal = dataSnapshot.getValue(Double::class.java)

                    val myRefBreakfast = database.getReference("Users").child(uid).child("Breakfast").child(currentDate)
                    val myRefLunch = database.getReference("Users").child(uid).child("Lunch").child(currentDate)
                    val myRefDinner = database.getReference("Users").child(uid).child("Dinner").child(currentDate)

                    var totalKcalBreakfast = 0.0
                    var totalKcalLunch = 0.0
                    var totalKcalDinner = 0.0

                    // ValueEventListener for Breakfast
                    myRefBreakfast.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(breakfastSnapshot: DataSnapshot) {
                            totalKcalBreakfast = 0.0
                            for (productSnapshot in breakfastSnapshot.children) {
                                val productData = productSnapshot.getValue(Product::class.java)
                                totalKcalBreakfast += productData?.kcal ?: 0.0
                            }
                            updateKcalText(totalKcalBreakfast, totalKcalLunch, totalKcalDinner, kcal)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e("MyApp", "Error reading breakfast data: ${error.message}")
                        }
                    })

                    // ValueEventListener for Lunch
                    myRefLunch.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(lunchSnapshot: DataSnapshot) {
                            totalKcalLunch = 0.0
                            for (productSnapshot in lunchSnapshot.children) {
                                val productData = productSnapshot.getValue(Product::class.java)
                                totalKcalLunch += productData?.kcal ?: 0.0
                            }
                            updateKcalText(totalKcalBreakfast, totalKcalLunch, totalKcalDinner, kcal)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e("MyApp", "Error reading lunch data: ${error.message}")
                        }
                    })

                    // ValueEventListener for Dinner
                    myRefDinner.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dinnerSnapshot: DataSnapshot) {
                            totalKcalDinner = 0.0
                            for (productSnapshot in dinnerSnapshot.children) {
                                val productData = productSnapshot.getValue(Product::class.java)
                                totalKcalDinner += productData?.kcal ?: 0.0
                            }
                            updateKcalText(totalKcalBreakfast, totalKcalLunch, totalKcalDinner, kcal)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e("MyApp", "Error reading dinner data: ${error.message}")
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MyApp", "Error reading protein value: ${error.message}")
                }
            })
        }

        val tv_stepsTaken = view.findViewById<TextView>(R.id.tv_stepsTaken)
        currentSteps = previousSteps // Przywracamy wartość poprzednich kroków
        tv_stepsTaken.text = currentSteps.toString()

        val butres = view.findViewById<Button>(R.id.reset)
        butres.setOnClickListener {
            resetSteps()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        running = true
        val stepDetectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepDetectorSensor == null) {
            Toast.makeText(requireContext(), "Step detector sensor not available on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

    }

    override fun onPause() {
        super.onPause()
        running = false
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (running) {
            currentSteps = event?.values?.get(0) ?: previousSteps
            val currentStepCount = (currentSteps - previousSteps).toInt()
            updateStepsCounter(currentStepCount)
        }
    }
    data class Product(
        val uid: String = "",
        val key: String = "",
        val barcode: String = "",
        val productName: String = "",
        val kcal: Double = 0.0,
        val fat: Double = 0.0,
        val protein: Double = 0.0,
        val carbohydrates: Double = 0.0
    )

    fun updateStepsCounter(steps: Int) {
        val view = view
        val tv_stepsTaken = view?.findViewById<TextView>(R.id.tv_stepsTaken)
        val progressBar = view?.findViewById<ProgressBar>(R.id.counterStepsProgress)
        val distance =view?.findViewById<TextView>(R.id.distanceText)
        val kcal = view?.findViewById<TextView>(R.id.kcalText)

        val showSteps = steps.toInt()
        val distanceValue = showSteps * 80 / 100000.0
        val kcalValue = showSteps * 0.04
        val roundedDistance = String.format("%.3f", distanceValue).toDouble()
        val roundedKcal = String.format("%.2f", kcalValue).toDouble()
        tv_stepsTaken?.text = "$showSteps/10000"
        progressBar?.progress = showSteps
        distance?.text = "$roundedDistance km"
        kcal?.text = "$roundedKcal Kcal"

        val currentDate = SimpleDateFormat(
            "dd-MM-yyyy",
            Locale.getDefault()
        ).format(Date())
        auth = Firebase.auth
        val user = auth.currentUser

        if (user != null) {
            val uid = user.uid
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid)

            val dailyStepData = DailyStepData(
                steps = showSteps,
                distance = roundedDistance,
                caloriesBurned = roundedKcal
            )
            databaseReference.child("DailySteps").child(currentDate).setValue(dailyStepData)
        }
    }
    data class DailyStepData(
        val steps: Int = 0,
        val distance: Double = 0.0,
        val caloriesBurned: Double = 0.0
    )
    fun resetSteps() {
        previousSteps = currentSteps
        currentSteps = 0f
        updateStepsCounter(0)
        prefsManager.saveTotalSteps(previousSteps)
        Toast.makeText(requireContext(), "Steps reset to 0", Toast.LENGTH_SHORT).show()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}




