package com.example.myapplicationpls

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.ScanOptions


class addFoodtoDBFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    fun String.capitalizeFirstLetter(): String {
        return if (isNotEmpty()) {
            this[0].toUpperCase() + substring(1)
        } else {
            this
        }
    }
    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_food, container, false)
        val btnScan = view.findViewById<Button>(R.id.btnScan)
        val enterBarcode = view.findViewById<EditText>(R.id.barCode)

        val scanResultLauncher: ActivityResultLauncher<Intent> =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val data = result.data
                if (result.resultCode == Activity.RESULT_OK && data != null) {
                    val result = IntentIntegrator.parseActivityResult(result.resultCode, data)
                    if (result.contents == null) {
                        Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), "Scanned: ${result.contents}", Toast.LENGTH_LONG).show()
                        enterBarcode.setText(result.contents)
                    }
                }
            }

        val categorySpinner = view.findViewById<Spinner>(R.id.categoryFood)
        val categories = resources.getStringArray(R.array.food_ComboBox)
        val adapter = ArrayAdapter.createFromResource(requireContext(), R.array.food_ComboBox, R.layout.spinner_item_food)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        btnScan.setOnClickListener {
                val scanner = IntentIntegrator(requireActivity())
                val intent = scanner.createScanIntent()
                val options = ScanOptions();
                scanResultLauncher.launch(intent)
            }

        data class Product(
            val key: String = "",
            val barcode: String,
            val productName: String,
            val category: String,
            val kcal: Double,
            val protein: Double,
            val fat: Double,
            val carbohydrates: Double
        )

        val database = FirebaseDatabase.getInstance("https://fitapp-loginbase-default-rtdb.europe-west1.firebasedatabase.app")

        val productsRef = database.getReference("Products")

        val addProduct = view.findViewById<Button>(R.id.addProduct)

        val barcodeEditText = view.findViewById<EditText>(R.id.barCode)
        val productNameEditText = view.findViewById<EditText>(R.id.ProductName)

        val kcalEditText = view.findViewById<EditText>(R.id.kcal)
        val proteinEditText = view.findViewById<EditText>(R.id.Protein)
        val fatEditText = view.findViewById<EditText>(R.id.Fat)
        val carbohydratesEditText = view.findViewById<EditText>(R.id.Carbs)

        addProduct.setOnClickListener {
            val barcode = barcodeEditText.text.toString()
            val productName = productNameEditText.text.toString().capitalizeFirstLetter()
            val category = categorySpinner.selectedItem.toString()
            val kcal = kcalEditText.text.toString()
            val protein = proteinEditText.text.toString()
            val fat = fatEditText.text.toString()
            val carbohydrates = carbohydratesEditText.text.toString()

            // Validate if any required field is empty
            if (productName.isEmpty() || kcal.isEmpty() || protein.isEmpty() || fat.isEmpty() || carbohydrates.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_LONG).show()
            } else {
                val kcal = kcalEditText.text.toString().toDouble()
                val protein = proteinEditText.text.toString().toDouble()
                val fat = fatEditText.text.toString().toDouble()
                val carbohydrates = carbohydratesEditText.text.toString().toDouble()
                val newProductId = productsRef.push().key ?: ""
                val query = productsRef.orderByChild("productName").equalTo(productName)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(
                                requireContext(),
                                "Product already exists!",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val product = Product(
                                newProductId,
                                barcode,
                                productName,
                                category,
                                kcal,
                                protein,
                                fat,
                                carbohydrates
                            )

                            productsRef.child(newProductId).setValue(product).addOnSuccessListener {
                                Log.d("MyApp", "Product saved successfully")
                                Toast.makeText(
                                    requireContext(),
                                    "Product added successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                carbohydratesEditText.setText("")
                                kcalEditText.setText("")
                                proteinEditText.setText("")
                                fatEditText.setText("")
                                barcodeEditText.setText("")
                                productNameEditText.setText("")
                            }.addOnFailureListener { error ->
                                Log.e("MyApp", "Failed to save product: ${error.message}")
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to add product",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
            }

        }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }


}