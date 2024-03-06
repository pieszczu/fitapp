package com.example.myapplicationpls

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth

class signUpUserProfile : AppCompatActivity() {
    private lateinit var sName: EditText
    private lateinit var sHeight: EditText
    private lateinit var sWeight: EditText
    private lateinit var sBMI: TextView
    private lateinit var sAge: EditText
    private lateinit var sKcal: EditText
    private lateinit var sFats: EditText
    private lateinit var sCarbs: EditText
    private lateinit var sProtein: EditText
    private lateinit var sKM: Spinner
    private lateinit var sAL: Spinner
    private lateinit var sButton: Button




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup2)
        sName = findViewById(R.id.sName)
        sHeight = findViewById(R.id.sHeight)
        sWeight = findViewById(R.id.sWeight)
        sBMI = findViewById(R.id.sBMI)
        sKcal = findViewById(R.id.sKcal)
        sProtein = findViewById(R.id.sProtein)
        sFats = findViewById(R.id.sFats)
        sCarbs = findViewById(R.id.sCarbs)
        sKM = findViewById(R.id.sKM)
        sAge = findViewById(R.id.sAge)
        sAL = findViewById(R.id.sAL)
        sButton = findViewById(R.id.btn3s)

        sHeight.addTextChangedListener(textWatcher)
        sWeight.addTextChangedListener(textWatcher)

        val categorySpinner = findViewById<Spinner>(R.id.sKM)
        val categories = resources.getStringArray(R.array.SEX)
        val adapter = ArrayAdapter.createFromResource(this, R.array.SEX, R.layout.spinner_item_food)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        val categorySpinner2 = findViewById<Spinner>(R.id.sAL)
        val categories2 = resources.getStringArray(R.array.Activity_level)
        val adapter2 = ArrayAdapter.createFromResource(this, R.array.Activity_level, R.layout.spinner_item_food)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner2.adapter = adapter2

        val sWeightEditText = findViewById<EditText>(R.id.sWeight)
        val sHeightEditText = findViewById<EditText>(R.id.sHeight)

        sWeightEditText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.post {
                    val rect = Rect()
                    view.getGlobalVisibleRect(rect)
                    view.requestRectangleOnScreen(rect, false)
                }
            }
        }

        sHeightEditText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.post {
                    val rect = Rect()
                    view.getGlobalVisibleRect(rect)
                    view.requestRectangleOnScreen(rect, false)
                }
            }
        }

        sButton.setOnClickListener{
            val database = FirebaseDatabase.getInstance("https://fitapp-loginbase-default-rtdb.europe-west1.firebasedatabase.app")

            val sName = sName.text.toString()
            val sAge = sAge.text.toString()
            val sHeight = sHeight.text.toString()
            val sWeight = sWeight.text.toString()
            val sBMI = sBMI.text.toString()

            val sKcal = sKcal.text.toString()
            val sProtein = sProtein.text.toString()
            val sFats = sFats.text.toString()
            val sCarbs = sCarbs.text.toString()

            val sKM = sKM.selectedItem.toString()
            val sAL = sAL.selectedItem.toString()
            var strideLength = 0.0


            val auth = FirebaseAuth.getInstance()
            val userC = auth.currentUser


            if(sAge.isEmpty() || sHeight.isEmpty() || sWeight.isEmpty() || sName.isEmpty() || sKcal.isEmpty() || sProtein.isEmpty() || sFats.isEmpty() || sCarbs.isEmpty()) {

                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            }
            else{
                val wzrostText = sHeight.toString().toDoubleOrNull()
                if(sKM == "Male")
                {
                    val calcStrideLength = (wzrostText ?: 0.0) * 0.415
                    strideLength = String.format("%.2f", calcStrideLength).toDouble()
                }
                else if(sKM == "Female")
                {
                    val calcStrideLength = (wzrostText ?: 0.0) * 0.413
                    strideLength = String.format("%.2f", calcStrideLength).toDouble()
                }

                val age: Int = sAge.toInt()
                val height: Double = sHeight.toDoubleOrNull() ?: 0.0
                val weight: Double = sWeight.toDoubleOrNull() ?: 0.0
                val bmi: Double = sBMI.toDoubleOrNull() ?: 0.0

                val kcal: Double = sKcal.toDoubleOrNull() ?: 0.0
                val carbs: Double = sCarbs.toDoubleOrNull() ?: 0.0
                val protein: Double = sProtein.toDoubleOrNull() ?: 0.0
                val fats: Double = sFats.toDoubleOrNull() ?: 0.0
                if (userC != null) {
                    val uid = userC.uid
                    val usersRef = database.getReference("Users").child(uid)
                    val user = UserData(
                        sName,
                        age,
                        height,
                        weight,
                        bmi,
                        kcal,
                        carbs,
                        protein,
                        fats,
                        sKM,
                        sAL,
                        strideLength
                    )
                    usersRef.setValue(user).addOnSuccessListener {
                        Log.d("MyApp", "Product saved successfully")
                        Toast.makeText(this, "User saved successfully, log in!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, loginCompat::class.java)
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener { error ->
                        Log.e("MyApp", "Failed to save product: ${error.message}")
                        Toast.makeText(this, "Failed to add user", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            calculateBMI()
            calculateKCAL()
        }
    }

    data class UserData(
        val nickname: String,
        val age: Int,
        val height: Double,
        val weight: Double,
        val bmi: Double,
        val kcal: Double,
        val carbohydrates: Double,
        val protein: Double,
        val fat: Double,
        val sex: String,
        val activityLevel: String,
        val strideLength: Double
    )




    private fun calculateKCAL() {
        val wzrostText = sHeight.text.toString().toDoubleOrNull()
        val wagaText = sWeight.text.toString().toDoubleOrNull()
        val ageText = sAge.text.toString().toDoubleOrNull()
        val selKM = sKM.selectedItem.toString()
        val selAL = sAL.selectedItem.toString()
        var Kcal = 0.0
        var Protein = 0.0
        var Fats = 0.0
        var Carbs = 0.0

        if (wzrostText != null && wagaText != null && wzrostText > 0 && wagaText > 0 && ageText != null && ageText > 0) {
            if(selKM == "Male"){
                if(selAL == "non-physical work, low physical activity") { //1.4
                    Kcal = (66.47 + (13.75 * (wagaText ?: 0.0)) + (5 * (wzrostText ?: 0.0)) - (6.75 * (ageText ?: 0.0))) * 1.2
                    Protein = Kcal / 100 * 20 / 4
                    Fats = Kcal / 100 * 30 / 9
                    Carbs = Kcal / 100 * 50 / 4
                }
                else if (selAL == "non-physical work, training 2 times a week") //1.6
                {
                    Kcal = (66.47 + (13.75 * (wagaText ?: 0.0)) + (5 * (wzrostText ?: 0.0)) - (6.75 * (ageText ?: 0.0))) * 1.4
                    Protein = Kcal / 100 * 20 / 4
                    Fats = Kcal / 100 * 30 / 9
                    Carbs = Kcal / 100 * 50 / 4
                }
                else if (selAL == "light physical work, training 3 or 4 times a week") //1.8
                {
                    Kcal = (66.47 + (13.75 * (wagaText ?: 0.0)) + (5 * (wzrostText ?: 0.0)) - (6.75 * (ageText ?: 0.0))) * 1.6
                    Protein = Kcal / 100 * 20 / 4
                    Fats = Kcal / 100 * 30 / 9
                    Carbs = Kcal / 100 * 50 / 4
                }
                else if (selAL == "physical work, training 5 times a week") //1.8
                {
                    Kcal = (66.47 + (13.75 * (wagaText ?: 0.0)) + (5 * (wzrostText ?: 0.0)) - (6.75 * (ageText ?: 0.0))) * 1.75
                    Protein = Kcal / 100 * 20 / 4
                    Fats = Kcal / 100 * 30 / 9
                    Carbs = Kcal / 100 * 50 / 4
                }
                else if (selAL == "heavy physical work, training everyday") //1.8
                {
                    Kcal = (66.47 + (13.75 * (wagaText ?: 0.0)) + (5 * (wzrostText ?: 0.0)) - (6.75 * (ageText ?: 0.0))) * 2
                    Protein = Kcal / 100 * 20 / 4
                    Fats = Kcal / 100 * 30 / 9
                    Carbs = Kcal / 100 * 50 / 4
                }
            }
            else if(selKM == "Female"){

                if(selAL == "non-physical work, low physical activity") { //0.9
                    Kcal = (666.09 + (9.56 * (wagaText ?: 0.0)) + (1.85 * (wzrostText ?: 0.0)) - (4.67 * (ageText ?: 0.0))) * 1.2
                    Protein = Kcal / 100 * 20 / 4
                    Fats = Kcal / 100 * 30 / 9
                    Carbs = Kcal / 100 * 50 / 4
                }
                else if (selAL == "non-physical work, training 2 times a week") //1.1
                {
                    Kcal = (666.09 + (9.56 * (wagaText ?: 0.0)) + (1.85 * (wzrostText ?: 0.0)) - (4.67 * (ageText ?: 0.0))) * 1.4
                    Protein = Kcal / 100 * 20 / 4
                    Fats = Kcal / 100 * 30 / 9
                    Carbs = Kcal / 100 * 50 / 4
                }
                else if (selAL == "light physical work, training 3 or 4 times a week") //1.3
                {
                    Kcal = (666.09 + (9.56 * (wagaText ?: 0.0)) + (1.85 * (wzrostText ?: 0.0)) - (4.67 * (ageText ?: 0.0))) * 1.6
                    Protein = Kcal / 100 * 20 / 4
                    Fats = Kcal / 100 * 30 / 9
                    Carbs = Kcal / 100 * 50 / 4
                }
                else if (selAL == "physical work, training 5 times a week") //1.5
                {
                    Kcal = (666.09 + (9.56 * (wagaText ?: 0.0)) + (1.85 * (wzrostText ?: 0.0)) - (4.67 * (ageText ?: 0.0))) * 1.75
                    Protein = Kcal / 100 * 20 / 4
                    Fats = Kcal / 100 * 30 / 9
                    Carbs = Kcal / 100 * 50 / 4
                }
                else if (selAL == "heavy physical work, training everyday") //1.8
                {
                    Kcal = (666.09 + (9.56 * (wagaText ?: 0.0)) + (1.85 * (wzrostText ?: 0.0)) - (4.67 * (ageText ?: 0.0))) * 2
                    Protein = Kcal / 100 * 20 / 4
                    Fats = Kcal / 100 * 30 / 9
                    Carbs = Kcal / 100 * 50 / 4
                }
            }
            sKcal.setText(String.format("%.0f", Kcal))
            sProtein.setText(String.format("%.0f", Protein))
            sFats.setText(String.format("%.0f", Fats))
            sCarbs.setText(String.format("%.0f", Carbs))
        }



        sKM.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateKcal()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        sAL.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateKcal()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

    }

    private fun calculatePFC()
    {

    }
    private fun updateKcal() {
        calculateKCAL()
    }


    private fun calculateBMI() {
        val wzrostText = sHeight.text.toString().toDoubleOrNull()
        val wagaText = sWeight.text.toString().toDoubleOrNull()

        if (wzrostText != null && wagaText != null && wzrostText > 0 && wagaText > 0) {
            val wzrostMetry = wzrostText / 100.0
            val bmi = wagaText / (wzrostMetry * wzrostMetry)
            val limitedBMI = when {
                bmi < 16 -> 16.0
                bmi > 40 -> 40.0
                else -> bmi
            }

            sBMI.setText(String.format("%.2f", limitedBMI))
            if (limitedBMI > 15.99 && limitedBMI < 16.99) {
                sBMI.setTextColor(Color.RED)
            }
            else if (limitedBMI > 17 && limitedBMI < 18.49) {
                sBMI.setTextColor(Color.rgb(255,165,0))
            } else if (limitedBMI > 18.50 && limitedBMI < 24.99){

                sBMI.setTextColor(Color.GREEN)
            }
            else if (limitedBMI > 25 && limitedBMI < 29.89){
                sBMI.setTextColor(Color.YELLOW)
            }
            else if (limitedBMI > 29.9 && limitedBMI < 34.89){
                sBMI.setTextColor(Color.rgb(255,165,0))
            }
            else if (limitedBMI > 35 && limitedBMI < 40.1)
                sBMI.setTextColor(Color.RED)
        } else {
            sBMI.setText("BMI")
            sBMI.setTextColor(Color.YELLOW)
        }
    }
}