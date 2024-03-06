package com.example.myapplicationpls

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class PedometerPrefsManager2(context: Context) { // problem with stepsCounter - it doesn't work, because main stepsCounter in MainFragment.kt is connecting with this sensor
    private val sharedPreferences2 =
        context.getSharedPreferences("pedometer_prefss", Context.MODE_PRIVATE)

    fun saveTotalSteps(totalSteps: Float) {
        sharedPreferences2.edit().putFloat("total_stepss", totalSteps).apply()
    }

    fun loadTotalSteps(): Float {
        return sharedPreferences2.getFloat("total_stepss", 0f)
    }
}
class actionTraining : Fragment(), SensorEventListener {
    private lateinit var timerTextView: TextView
    private lateinit var startButton: ImageButton
    private lateinit var stopButton: ImageButton
    private lateinit var resetButton: Button
    private lateinit var calorieTextView: TextView

    private lateinit var spinnerTypeTraining: Spinner
    private lateinit var spinnerTypeexercise: Spinner
    private lateinit var seriesTraining: EditText
    private lateinit var repsTraining: EditText
    private lateinit var kcalTraining: TextView
    private lateinit var addTrainingButton: Button
    private lateinit var auth: FirebaseAuth

    private lateinit var sensorManager2: SensorManager
    private var stepSensor: Sensor? = null
    private var currentSteps2 = 0f
    private var previousSteps2 = 0f
    private var running = false

    private lateinit var prefsManager2: PedometerPrefsManager2
    private var isRunning = false
    private var startTime = 0L
    private var elapsedTime = 0L
    private val handler = Handler()
    private var caloriesBurned = 0
    private val calorieBurnInterval = 9000 // 9 seconds

    private var stepCount = 0
    private lateinit var distance: TextView

    val database = FirebaseDatabase.getInstance("https://fitapp-loginbase-default-rtdb.europe-west1.firebasedatabase.app")
    val currentDate = SimpleDateFormat(
        "dd-MM-yyyy",
        Locale.getDefault()
    ).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager2 = this.requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager2.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        prefsManager2 = PedometerPrefsManager2(this.requireContext())
        previousSteps2 = prefsManager2.loadTotalSteps()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.action_training, container, false)

        timerTextView = view.findViewById(R.id.chronometer)
        startButton = view.findViewById(R.id.startTime)
        stopButton = view.findViewById(R.id.pauseTime)
        resetButton = view.findViewById(R.id.resetTime)
        calorieTextView = view.findViewById(R.id.caloriesCounter)
        val spinnerTraining = view.findViewById<Spinner>(R.id.typeOfTraining)
        val spinnerExercises = view.findViewById<Spinner>(R.id.typeOfExercise)

        startButton.setOnClickListener { startTimer() } // chronometer buttons
        stopButton.setOnClickListener { stopTimer() }
        resetButton.setOnClickListener { resetTimer()
            resetSteps()}


        val zoneGymWorkout = view.findViewById<LinearLayout>(R.id.gymExercisesZone)

        val adapter = ArrayAdapter.createFromResource(requireContext(), R.array.type_of_training, R.layout.spinner_item_food) // list type of training
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTraining.adapter = adapter

        val adapter2 = ArrayAdapter.createFromResource(requireContext(), R.array.video_options, R.layout.spinner_item_food) // list type of exercise
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerExercises.adapter = adapter2


        val gymDetails = view.findViewById<LinearLayout>(R.id.gymDetails)
        val runningDetails = view.findViewById<LinearLayout>(R.id.runningDetails)

        spinnerTraining.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener { // manipulate view
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                val chosenTraining = parentView.getItemAtPosition(position).toString()

                if (chosenTraining == "running") {
                    zoneGymWorkout.visibility = View.INVISIBLE
                    gymDetails.visibility = View.INVISIBLE
                    runningDetails.visibility = View.VISIBLE
                } else {
                    zoneGymWorkout.visibility = View.VISIBLE
                    runningDetails.visibility = View.INVISIBLE
                    gymDetails.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
            }
        })

        addTrainingButton = view.findViewById(R.id.addTraining)

        addTrainingButton.setOnClickListener{ // add training to Firebase
            spinnerTypeTraining = view.findViewById(R.id.typeOfTraining)
            val typeOfTraining = spinnerTypeTraining.selectedItem.toString()
            if(typeOfTraining=="gym workout")
            {
                spinnerTypeexercise = view.findViewById(R.id.typeOfExercise)
                timerTextView = view.findViewById(R.id.chronometer)
                seriesTraining = view.findViewById(R.id.series)
                repsTraining = view.findViewById(R.id.reps)
                kcalTraining = view.findViewById(R.id.caloriesCounter)

                auth = Firebase.auth
                val user = auth.currentUser
                val uid = user?.uid.toString()
                if (user != null) {
                    val trainingRef = FirebaseDatabase.getInstance().getReference("Users")
                        .child(uid)
                        .child("Training")
                        .child(currentDate)

                    val key = trainingRef.push().key ?: ""
                    val typeOfExercise = spinnerTypeexercise.selectedItem.toString()
                    val time = timerTextView.text.toString()
                    val series = seriesTraining.text.toString().toInt()
                    val reps = repsTraining.text.toString().toInt()
                    val kcal = kcalTraining.text.toString().toInt()

                    // class training to save data
                    val training = Training(
                        key,
                        typeOfExercise,
                        time,
                        series,
                        reps,
                        kcal,
                    )

                    // save data
                    trainingRef.child(key).setValue(training).addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Training added successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        seriesTraining.setText("3") // set default values
                        repsTraining.setText("12")
                        kcalTraining.text = "0"
                        resetTimer()
                    }.addOnFailureListener { error ->
                        Toast.makeText(
                            requireContext(),
                            "Failed to add product",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            else if(typeOfTraining=="running"){ // section running is in progress - doesn't work!
                Toast.makeText(
                    requireContext(),
                    "Change on Gym Workout",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        running = true
        val stepDetectorSensor = sensorManager2?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepDetectorSensor == null) {
            Toast.makeText(requireContext(), "Step detector sensor not available on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager2?.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        running = false
        sensorManager2?.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for step counter sensor
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (running) {
            currentSteps2 = event?.values?.get(0) ?: previousSteps2
            val currentStepCount = (currentSteps2 - previousSteps2).toInt()
            val stepsTextView = view?.findViewById<TextView>(R.id.distanceDetails)
            stepsTextView?.text = currentStepCount.toString()
        }
    }

    fun resetSteps() {
        previousSteps2 = currentSteps2
        currentSteps2 = 0f
        prefsManager2.saveTotalSteps(previousSteps2)
        Toast.makeText(requireContext(), "Steps reset to 0", Toast.LENGTH_SHORT).show()
    }

    private fun startTimer() {
        if (!isRunning) {
            val currentTime = SystemClock.elapsedRealtime()
            startTime = currentTime - elapsedTime
            timerTextView.visibility = View.VISIBLE
            isRunning = true
            handler.postDelayed(updateElapsedTime, 10)
            handler.postDelayed(updateCaloriesRunnable, calorieBurnInterval.toLong())
        }
    }

    private fun stopTimer() {
        if (isRunning) {
            isRunning = false
            handler.removeCallbacks(updateElapsedTime)
            handler.removeCallbacks(updateCaloriesRunnable)
        }
    }

    private fun updateCalories() {
        if (isRunning) {
            caloriesBurned++
            calorieTextView.text = caloriesBurned.toString()
        }
    }

    private val updateCaloriesRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                caloriesBurned++
                calorieTextView.text = caloriesBurned.toString()
                handler.postDelayed(this, calorieBurnInterval.toLong())
            }
        }
    }

    private fun resetTimer() {
        elapsedTime = 0
        caloriesBurned = 0
        calorieTextView.text = "0"
        if (isRunning) {
            val currentTime = SystemClock.elapsedRealtime()
            startTime = currentTime
        } else {
            updateTimerText()
        }
        resetSteps()
    }

    private fun updateTimerText() { // chronometer
        val minutes = (elapsedTime / 60000) % 60
        val seconds = (elapsedTime / 1000) % 60
        val milliseconds = elapsedTime % 1000

        val formattedTime = String.format(
            Locale.getDefault(),
            "%02d:%02d:%03d",
            minutes,
            seconds,
            milliseconds
        )
        timerTextView.text = formattedTime
    }

    private val updateElapsedTime = object : Runnable {
        override fun run() {
            if (isRunning) {
                val currentTime = SystemClock.elapsedRealtime()
                elapsedTime = currentTime - startTime
                updateTimerText()
                handler.postDelayed(this, 10)
            }
        }
    }

    data class Training(
        val key: String = "",
        val typeOfExercise: String,
        val time: String,
        val series: Int,
        val reps: Int,
        val kcal: Int,
    )
}
