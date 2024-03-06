package com.example.myapplicationpls

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import com.harrywhewell.scrolldatepicker.DayScrollDatePicker
import com.journeyapps.barcodescanner.ScanOptions
import org.joda.time.LocalDate
import java.text.SimpleDateFormat
import java.util.*


class AddDailyFragment : Fragment() {

    private lateinit var mPicker: DayScrollDatePicker
    private lateinit var breakfastAdapter: ProductAdapter
    private lateinit var lunchAdapter: LunchAdapter
    private lateinit var dinnerAdapter: DinnerAdapter
    private lateinit var fragmentContext: Context
    private lateinit var auth: FirebaseAuth
    private lateinit var currentDate: String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val currentDate = SimpleDateFormat(
            "dd-MM-yyyy",
            Locale.getDefault()
        ).format(Date())
        val view = inflater.inflate(R.layout.fragment_add_daily, container, false)

        mPicker = view.findViewById(R.id.dayPicker)
        val today = LocalDate()
        val startDate = today.minusDays(7)
        mPicker.setStartDate(startDate.dayOfMonth, startDate.monthOfYear, startDate.year)
        mPicker.setEndDate(today.dayOfMonth, today.monthOfYear, today.year)

        mPicker.getSelectedDate { date ->
            handleSelectedDate(date)
        }


        val autoFirebaseBreakfast = view.findViewById<AutoCompleteTextView>(R.id.autoEditText)
        val autoFirebaseLunch = view.findViewById<AutoCompleteTextView>(R.id.autoEditTextLunch)
        val autoFirebaseDinner = view.findViewById<AutoCompleteTextView>(R.id.autoEditTextDinner)

        val database = FirebaseDatabase.getInstance("https://fitapp-loginbase-default-rtdb.europe-west1.firebasedatabase.app")
        val refProducts = database.getReference("Products")
        val refUsers = database.getReference("Users")

        val scanBreakfastButton = view.findViewById<Button>(R.id.btnScanBreakfast)
        val scanLunchButton = view.findViewById<Button>(R.id.btnScanLunch)
        val scanDinnerButton = view.findViewById<Button>(R.id.btnScanDinner)
        val addProductBreakfast = view.findViewById<Button>(R.id.addProductBreakfast)
        val addProductLunch = view.findViewById<Button>(R.id.addProductLunch)
        val addProductDinner = view.findViewById<Button>(R.id.addProductDinner)
        val enterBarcode = view.findViewById<EditText>(R.id.enterBarcodeBreakfast)
        val enterBreakfastAmount = view.findViewById<EditText>(R.id.gramText)
        val enterLunchAmount = view.findViewById<EditText>(R.id.gramTextLunch)
        val enterDinnerAmount = view.findViewById<EditText>(R.id.gramTextDinner)

        val scanResultLauncherBreakfast: ActivityResultLauncher<Intent> =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val data = result.data
                if (result.resultCode == Activity.RESULT_OK && data != null) {
                    val result = IntentIntegrator.parseActivityResult(result.resultCode, data)
                    if (result.contents == null) {
                        Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), "Scanned: ${result.contents}", Toast.LENGTH_LONG).show()
                        enterBarcode.setText(result.contents)
                        val barcodeToSearch = enterBarcode.text.toString()

                        refProducts.orderByChild("barcode").equalTo(barcodeToSearch).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) { // if exists set name of product in EditText
                                    for (productSnapshot in snapshot.children) {
                                        val product = productSnapshot.getValue(Product::class.java)
                                        val productName = product?.productName
                                        autoFirebaseBreakfast.setText(productName)
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "Product not found", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                }
            }

        val scanResultLauncherLunch: ActivityResultLauncher<Intent> =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val data = result.data
                if (result.resultCode == Activity.RESULT_OK && data != null) {
                    val result = IntentIntegrator.parseActivityResult(result.resultCode, data)
                    if (result.contents == null) {
                        Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), "Scanned: ${result.contents}", Toast.LENGTH_LONG).show()
                        enterBarcode.setText(result.contents)
                        val barcodeToSearch = enterBarcode.text.toString()

                        refProducts.orderByChild("barcode").equalTo(barcodeToSearch).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    for (productSnapshot in snapshot.children) {
                                        val product = productSnapshot.getValue(Product::class.java)
                                        val productName = product?.productName
                                        autoFirebaseLunch.setText(productName)
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "Product not found", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                }
            }

        val scanResultLauncherDinner: ActivityResultLauncher<Intent> =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val data = result.data
                if (result.resultCode == Activity.RESULT_OK && data != null) {
                    val result = IntentIntegrator.parseActivityResult(result.resultCode, data)
                    if (result.contents == null) {
                        Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), "Scanned: ${result.contents}", Toast.LENGTH_LONG).show()
                        enterBarcode.setText(result.contents)
                        val barcodeToSearch = enterBarcode.text.toString()

                        refProducts.orderByChild("barcode").equalTo(barcodeToSearch).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    for (productSnapshot in snapshot.children) {
                                        val product = productSnapshot.getValue(Product::class.java)
                                        val productName = product?.productName
                                        autoFirebaseDinner.setText(productName)
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "Product not found", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                }
            }

        scanBreakfastButton.setOnClickListener { // scan options
            val scanner = IntentIntegrator(requireActivity())
            val intent = scanner.createScanIntent()
            val options = ScanOptions();
            scanResultLauncherBreakfast.launch(intent)
        }
        scanLunchButton.setOnClickListener {
            val scanner = IntentIntegrator(requireActivity())
            val intent = scanner.createScanIntent()
            val options = ScanOptions();
            scanResultLauncherLunch.launch(intent)
        }
        scanDinnerButton.setOnClickListener {
            val scanner = IntentIntegrator(requireActivity())
            val intent = scanner.createScanIntent()
            val options = ScanOptions();
            scanResultLauncherDinner.launch(intent)
        }

        addProductBreakfast.setOnClickListener{
            val productName = autoFirebaseBreakfast.text.toString()
            val amount = enterBreakfastAmount.text.toString()
            if(productName.isEmpty() || amount.isEmpty())
            {
                Toast.makeText(requireContext(), "Not found", Toast.LENGTH_SHORT).show()
            }
            else {
                auth = Firebase.auth
                val user = auth.currentUser
                if (user != null) {
                    val uid = user.uid
                    refProducts.orderByChild("productName").equalTo(productName)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    for (productSnapshot in snapshot.children) {
                                        val product = productSnapshot.getValue(Product::class.java)
                                        // if exists check all elements in database
                                        val productName = product?.productName
                                        val kcal = product?.kcal ?: 0.0
                                        val protein = product?.protein ?: 0.0
                                        val fat = product?.fat ?: 0.0
                                        val carbs = product?.carbohydrates ?: 0.0

                                        // calculate
                                        val kcalResult = kcal * amount.toDouble() / 100
                                        val proteinResult = protein * amount.toDouble() / 100
                                        val fatResult = fat * amount.toDouble() / 100
                                        val carbsResult = carbs * amount.toDouble() / 100

                                        // round values
                                        val roundedProtein = String.format("%.2f", proteinResult).toDouble()
                                        val roundedFat = String.format("%.2f", fatResult).toDouble()
                                        val roundedCarbs = String.format("%.2f", carbsResult).toDouble()
                                        val roundedKcal = String.format("%.2f", kcalResult).toDouble()

                                        // create key
                                        val breakfastKey = refProducts.push().key
                                        // create map to add values
                                        val breakfastData = hashMapOf(
                                            "uid" to uid,
                                            "key" to breakfastKey,
                                            "productName" to productName,
                                            "date" to currentDate,
                                            "kcal" to roundedKcal,
                                            "protein" to roundedProtein,
                                            "fat" to roundedFat,
                                            "carbohydrates" to roundedCarbs
                                        )
                                        refUsers.child(uid).child("Breakfast").child(currentDate).child(breakfastKey!!).setValue(breakfastData)
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Product added to breakfast!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Failed to add product to breakfast",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Product not found",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                }
            }
        }

        refProducts.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dataList = mutableListOf<String>()

                for (postSnapshot in dataSnapshot.children) {
                    val product = postSnapshot.getValue(Product::class.java)
                    product?.let {
                        dataList.add(it.productName)
                    }
                }

                // display values in list
                val adapter = ArrayAdapter(fragmentContext, R.layout.simple_list_item, R.id.textViewItem, dataList)

                // autocomplete
                val autoCompleteAdapter = ArrayAdapter(fragmentContext, android.R.layout.simple_dropdown_item_1line, dataList)
                autoFirebaseBreakfast.setAdapter(autoCompleteAdapter)
                autoFirebaseLunch.setAdapter(autoCompleteAdapter)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        addProductLunch.setOnClickListener{
            val productName = autoFirebaseLunch.text.toString()
            val amount = enterLunchAmount.text.toString()
            if(productName.isEmpty() || amount.isEmpty())
            {
                Toast.makeText(requireContext(), "Empty fields!", Toast.LENGTH_SHORT).show()
            }
            else {
                auth = Firebase.auth
                val user = auth.currentUser
                if (user != null) {
                    val uid = user.uid
                    refProducts.orderByChild("productName").equalTo(productName)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    for (productSnapshot in snapshot.children) {
                                        val product = productSnapshot.getValue(Product::class.java)

                                        val productName = product?.productName
                                        val kcal = product?.kcal ?: 0.0
                                        val protein = product?.protein ?: 0.0
                                        val fat = product?.fat ?: 0.0
                                        val carbs = product?.carbohydrates ?: 0.0


                                        val kcalResult = kcal * amount.toDouble() / 100
                                        val proteinResult = protein * amount.toDouble() / 100
                                        val fatResult = fat * amount.toDouble() / 100
                                        val carbsResult = carbs * amount.toDouble() / 100

                                        val roundedProtein = String.format("%.2f", proteinResult).toDouble()
                                        val roundedFat = String.format("%.2f", fatResult).toDouble()
                                        val roundedCarbs = String.format("%.2f", carbsResult).toDouble()
                                        val roundedKcal = String.format("%.2f", kcalResult).toDouble()


                                        val breakfastKey = refProducts.push().key

                                        val lunchData = hashMapOf(
                                            "uid" to uid,
                                            "key" to breakfastKey,
                                            "productName" to productName,
                                            "date" to currentDate,
                                            "kcal" to roundedKcal,
                                            "protein" to roundedProtein,
                                            "fat" to roundedFat,
                                            "carbohydrates" to roundedCarbs
                                        )
                                        refUsers.child(uid).child("Lunch").child(currentDate).child(breakfastKey!!).setValue(lunchData)
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Product added to lunch!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Failed to add product to lunch",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Product not found",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                }
            }
        }

        refProducts.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dataList = mutableListOf<String>()

                for (postSnapshot in dataSnapshot.children) {
                    val product = postSnapshot.getValue(Product::class.java)
                    product?.let {
                        dataList.add(it.productName)
                    }
                }


                val adapter = ArrayAdapter(fragmentContext, R.layout.simple_list_item, R.id.textViewItem, dataList)

                val autoCompleteAdapter = ArrayAdapter(fragmentContext, android.R.layout.simple_dropdown_item_1line, dataList)
                autoFirebaseBreakfast.setAdapter(autoCompleteAdapter)
                autoFirebaseLunch.setAdapter(autoCompleteAdapter)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        addProductDinner.setOnClickListener{
            val productName = autoFirebaseDinner.text.toString()
            val amount = enterDinnerAmount.text.toString()

            if(productName.isEmpty() || amount.isEmpty())
            {

                Toast.makeText(requireContext(), "Empty fields!", Toast.LENGTH_SHORT).show()
            }
            else {
                auth = Firebase.auth
                val user = auth.currentUser
                if (user != null) {
                    val uid = user.uid
                    refProducts.orderByChild("productName").equalTo(productName)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    for (productSnapshot in snapshot.children) {
                                        val product = productSnapshot.getValue(Product::class.java)

                                        val productName = product?.productName
                                        val kcal = product?.kcal ?: 0.0
                                        val protein = product?.protein ?: 0.0
                                        val fat = product?.fat ?: 0.0
                                        val carbs = product?.carbohydrates ?: 0.0

                                        val kcalResult = kcal * amount.toDouble() / 100
                                        val proteinResult = protein * amount.toDouble() / 100
                                        val fatResult = fat * amount.toDouble() / 100
                                        val carbsResult = carbs * amount.toDouble() / 100

                                        val roundedProtein = String.format("%.2f", proteinResult).toDouble()
                                        val roundedFat = String.format("%.2f", fatResult).toDouble()
                                        val roundedCarbs = String.format("%.2f", carbsResult).toDouble()
                                        val roundedKcal = String.format("%.2f", kcalResult).toDouble()

                                        val breakfastKey = refProducts.push().key

                                        val DinnerData = hashMapOf(
                                            "uid" to uid,
                                            "key" to breakfastKey,
                                            "productName" to productName,
                                            "date" to currentDate,
                                            "kcal" to roundedKcal,
                                            "protein" to roundedProtein,
                                            "fat" to roundedFat,
                                            "carbohydrates" to roundedCarbs
                                        )
                                        refUsers.child(uid).child("Dinner").child(currentDate).child(breakfastKey!!).setValue(DinnerData)
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Product added to dinner!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Failed to add product to dinner",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Product not found",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                }
            }
        }

        refProducts.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dataList = mutableListOf<String>()

                for (postSnapshot in dataSnapshot.children) {
                    val product = postSnapshot.getValue(Product::class.java)
                    product?.let {
                        dataList.add(it.productName)
                    }
                }

                val adapter = ArrayAdapter(fragmentContext, R.layout.simple_list_item, R.id.textViewItem, dataList)


                val autoCompleteAdapter = ArrayAdapter(fragmentContext, android.R.layout.simple_dropdown_item_1line, dataList)
                autoFirebaseBreakfast.setAdapter(autoCompleteAdapter)
                autoFirebaseDinner.setAdapter(autoCompleteAdapter)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

// TextWatchers to changes in autoComplete
        autoFirebaseBreakfast.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                val filterText = editable.toString()
            }
        })
        autoFirebaseLunch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                val filterText = editable.toString()
            }
        })
        autoFirebaseDinner.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                val filterText = editable.toString()
            }
        })

        // declare adapers
        breakfastAdapter = ProductAdapter(emptyList(), object : OnDeleteClickListener {
            override fun onDeleteClick(productKey: String) {
            }
        })

        lunchAdapter = LunchAdapter(emptyList(), object : OnDeleteClickListener {
            override fun onDeleteClick(productKey: String) {
            }
        })
        dinnerAdapter = DinnerAdapter(emptyList(), object : OnDeleteClickListener {
            override fun onDeleteClick(productKey: String) {
            }
        })

        val productsRef = database.getReference("Users")
        auth = Firebase.auth
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            productsRef.child(uid).child("Breakfast").child(currentDate).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val productList = mutableListOf<Product>()

                    for (productSnapshot in dataSnapshot.children) {
                        val product = productSnapshot.getValue(Product::class.java)
                        product?.let {
                            productList.add(it)
                        }
                    }
                    updateProductList(productList)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }
        if (user != null) {
            val uid = user.uid
            productsRef.child(uid).child("Lunch").child(currentDate)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val productList = mutableListOf<Product>()

                        for (productSnapshot in dataSnapshot.children) {
                            val product = productSnapshot.getValue(Product::class.java)
                            product?.let {
                                productList.add(it)
                            }
                        }
                        updateProductListLunch(productList)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
        }
        if (user != null) {
            // Jeśli użytkownik jest zalogowany, pobierz jego nick z Firebase Realtime Database
            val uid = user.uid
            productsRef.child(uid).child("Dinner").child(currentDate).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val productList = mutableListOf<Product>()

                    for (productSnapshot in dataSnapshot.children) {
                        val product = productSnapshot.getValue(Product::class.java)
                        product?.let {
                            productList.add(it)
                        }
                    }
                    updateProductListDinner(productList)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }

//recyclers
        val recyclerView: RecyclerView = view.findViewById(R.id.testing)
        recyclerView.adapter = breakfastAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val recyclerViewLunch: RecyclerView = view.findViewById(R.id.testing2)
        recyclerViewLunch.adapter = lunchAdapter
        recyclerViewLunch.layoutManager = LinearLayoutManager(requireContext())

        val recyclerViewDinner: RecyclerView = view.findViewById(R.id.testing3)
        recyclerViewDinner.adapter = dinnerAdapter
        recyclerViewDinner.layoutManager = LinearLayoutManager(requireContext())

        breakfastAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                auth = Firebase.auth
                val user = auth.currentUser
                if (user != null) {
                    val uid = user.uid
                    refUsers.child(uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val totalKcalTextView: TextView = view.findViewById(R.id.kcalDailyBreakfast)
                                    val totalCarbsTextView: TextView = view.findViewById(R.id.carbsDailyBreakfast)
                                    val totalFatTextView: TextView = view.findViewById(R.id.fatDailyBreakfast)
                                    val totalProteinTextView: TextView = view.findViewById(R.id.proteinDailyBreakfast)

                                    val carbs = snapshot.child("carbohydrates").getValue(Double::class.java)
                                    val protein = snapshot.child("protein").getValue(Double::class.java)
                                    val fat = snapshot.child("fat").getValue(Double::class.java)
                                    val kcal = snapshot.child("kcal").getValue(Double::class.java)

                                    val totalProtein = breakfastAdapter.getDailyProteinTotal()
                                    val totalFat = breakfastAdapter.getDailyFatTotal()
                                    val totalCarbs = breakfastAdapter.getDailyCarbsTotal()
                                    val totalKcal = breakfastAdapter.getDailyKcalTotal()

                                    val roundedProtein = String.format("%.2f", totalProtein).toDouble()
                                    val roundedFat = String.format("%.2f", totalFat).toDouble()
                                    val roundedCarbs = String.format("%.2f", totalCarbs).toDouble()
                                    val roundedKcal = String.format("%.2f", totalKcal).toDouble()
                                    totalProteinTextView.text = "$roundedProtein/$protein g"
                                    totalFatTextView.text = "$roundedFat/$fat g"
                                    totalCarbsTextView.text = "$roundedCarbs/$carbs g"
                                    totalKcalTextView.text = "$roundedKcal/$kcal"
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Log.e("Firebase", "Error fetching user", error.toException())
                            }
                        })
                }
            }
        })

        lunchAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                auth = Firebase.auth
                val user = auth.currentUser
                if (user != null) {
                    val uid = user.uid
                    refUsers.child(uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val totalKcalTextView: TextView = view.findViewById(R.id.kcalDailyLunch)
                                    val totalCarbsTextView: TextView = view.findViewById(R.id.carbsDailyLunch)
                                    val totalFatTextView: TextView = view.findViewById(R.id.fatDailyLunch)
                                    val totalProteinTextView: TextView = view.findViewById(R.id.proteinDailyLunch)

                                    val carbs = snapshot.child("carbohydrates").getValue(Double::class.java)
                                    val protein = snapshot.child("protein").getValue(Double::class.java)
                                    val fat = snapshot.child("fat").getValue(Double::class.java)
                                    val kcal = snapshot.child("kcal").getValue(Double::class.java)

                                    val totalProtein = lunchAdapter.getDailyProteinTotal()
                                    val totalFat = lunchAdapter.getDailyFatTotal()
                                    val totalCarbs = lunchAdapter.getDailyCarbsTotal()
                                    val totalKcal = lunchAdapter.getDailyKcalTotal()

                                    val roundedProtein = String.format("%.2f", totalProtein).toDouble()
                                    val roundedFat = String.format("%.2f", totalFat).toDouble()
                                    val roundedCarbs = String.format("%.2f", totalCarbs).toDouble()
                                    val roundedKcal = String.format("%.2f", totalKcal).toDouble()
                                    totalProteinTextView.text = "$roundedProtein/$protein g"
                                    totalFatTextView.text = "$roundedFat/$fat g"
                                    totalCarbsTextView.text = "$roundedCarbs/$carbs g"
                                    totalKcalTextView.text = "$roundedKcal/$kcal"
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Log.e("Firebase", "Error fetching user", error.toException())
                            }
                        })
                }
            }
        })

        dinnerAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                auth = Firebase.auth
                val user = auth.currentUser
                if (user != null) {
                    val uid = user.uid
                    refUsers.child(uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val totalKcalTextView: TextView = view.findViewById(R.id.kcalDailyDinner)
                                    val totalCarbsTextView: TextView = view.findViewById(R.id.carbsDailyDinner)
                                    val totalFatTextView: TextView = view.findViewById(R.id.fatDailyDinner)
                                    val totalProteinTextView: TextView = view.findViewById(R.id.proteinDailyDinner)

                                    val carbs = snapshot.child("carbohydrates").getValue(Double::class.java)
                                    val protein = snapshot.child("protein").getValue(Double::class.java)
                                    val fat = snapshot.child("fat").getValue(Double::class.java)
                                    val kcal = snapshot.child("kcal").getValue(Double::class.java)

                                    val totalProtein = dinnerAdapter.getDailyProteinTotal()
                                    val totalFat = dinnerAdapter.getDailyFatTotal()
                                    val totalCarbs = dinnerAdapter.getDailyCarbsTotal()
                                    val totalKcal = dinnerAdapter.getDailyKcalTotal()

                                    val roundedProtein = String.format("%.2f", totalProtein).toDouble()
                                    val roundedFat = String.format("%.2f", totalFat).toDouble()
                                    val roundedCarbs = String.format("%.2f", totalCarbs).toDouble()
                                    val roundedKcal = String.format("%.2f", totalKcal).toDouble()
                                    totalProteinTextView.text = "$roundedProtein/$protein g"
                                    totalFatTextView.text = "$roundedFat/$fat g"
                                    totalCarbsTextView.text = "$roundedCarbs/$carbs g"
                                    totalKcalTextView.text = "$roundedKcal/$kcal"
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Log.e("Firebase", "Error fetching user", error.toException())
                            }
                        })
                }
            }
        })
        return view
    }


    private fun handleSelectedDate(selectedDate: Date?): String {
        selectedDate?.let {
            updateProductListsForDate(selectedDate)
            breakfastAdapter.notifyDataSetChanged()
            lunchAdapter.notifyDataSetChanged()
            dinnerAdapter.notifyDataSetChanged()

            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate)

            return "$formattedDate"
        }
        return "Not selected data"
    }

    private fun updateProductListsForDate(selectedDate: Date) {
        val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedDate)

        updateBreakfastListForDate(formattedDate)

        updateLunchListForDate(formattedDate)
        breakfastAdapter.notifyDataSetChanged()
        lunchAdapter.notifyDataSetChanged()
        dinnerAdapter.notifyDataSetChanged()
    }

    private fun updateBreakfastListForDate(date: String) {
        auth = Firebase.auth
        val user = auth.currentUser
        val uid = user?.uid.toString()
        val breakfastRef = FirebaseDatabase.getInstance().getReference("Users")
            .child(uid)
            .child("Breakfast")
            .child(date)

        breakfastRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val productList = mutableListOf<Product>()
                for (productSnapshot in dataSnapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    product?.let {
                        productList.add(it)
                    }
                }
                updateProductList(productList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun updateLunchListForDate(date: String) {
        auth = Firebase.auth
        val user = auth.currentUser
        val uid = user?.uid.toString()
        val lunchRef = FirebaseDatabase.getInstance().getReference("Users")
            .child(uid)
            .child("Lunch")
            .child(date)

        lunchRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val productList = mutableListOf<Product>()

                for (productSnapshot in dataSnapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    product?.let {
                        productList.add(it)
                    }
                }
                updateProductListLunch(productList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun updateDinnerListForDate(date: String) {
        auth = Firebase.auth
        val user = auth.currentUser
        val uid = user?.uid.toString()

        val dinnerRef = FirebaseDatabase.getInstance().getReference("Users")
            .child(uid)
            .child("Dinner")
            .child(date)

        dinnerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val productList = mutableListOf<Product>()

                for (productSnapshot in dataSnapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    product?.let {
                        productList.add(it)
                    }
                }
                updateProductListDinner(productList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun isDateToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val selected = Calendar.getInstance()
        selected.time = date
        return today.get(Calendar.YEAR) == selected.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == selected.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) == selected.get(Calendar.DAY_OF_MONTH)
    }

    private fun updateProductList(productList: List<Product>) {
        breakfastAdapter.updateList(productList)
    }

    private fun updateProductListLunch(productList: List<Product>) {
        lunchAdapter.updateList(productList)
    }
    private fun updateProductListDinner(productList: List<Product>) {
        dinnerAdapter.updateList(productList)
    }
}

data class User(
    val carbohydrates: Double = 0.0,
    val fat: Double = 0.0,
    val protein: Double = 0.0
)

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

data class Breakfast(
    val key: String = "",
    val productName: String = "",
    val kcal: Double = 0.0,
    val fat: Double = 0.0,
    val protein: Double = 0.0,
    val carbohydrates: Double = 0.0
)

interface OnDeleteClickListener {
    fun onDeleteClick(productKey: String)
}

class ProductAdapter(
    private var productList: List<Product>,
    private val onDeleteClickListener: OnDeleteClickListener

) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    val currentDate = SimpleDateFormat(
        "dd-MM-yyyy",
        Locale.getDefault()
    ).format(Date())

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val uidTextView: TextView = itemView.findViewById(R.id.uidTextView)
        val keyTextView: TextView = itemView.findViewById(R.id.keyTextView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val caloriesTextView: TextView = itemView.findViewById(R.id.caloriesTextView)
        val fatTextView: TextView = itemView.findViewById(R.id.fatTextView)
        val proteinTextView: TextView = itemView.findViewById(R.id.proteinTextView)
        val carbohydratesTextView: TextView = itemView.findViewById(R.id.carbohydratesTextView)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }
    inner class BreakfastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val caloriesTextView: TextView = itemView.findViewById(R.id.caloriesTextView)
        val fatTextView: TextView = itemView.findViewById(R.id.fatTextView)
        val proteinTextView: TextView = itemView.findViewById(R.id.proteinTextView)
        val carbohydratesTextView: TextView = itemView.findViewById(R.id.carbohydratesTextView)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }

    fun updateList(newList: List<Product>) {
        productList = newList.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.uidTextView.text = product.uid
        holder.keyTextView.text = product.key
        holder.nameTextView.text = product.productName
        holder.caloriesTextView.text = "${product.kcal}"
        holder.fatTextView.text = "${product.fat}"
        holder.proteinTextView.text = "${product.protein}"
        holder.carbohydratesTextView.text = "${product.carbohydrates}"
        holder.deleteButton.setOnClickListener {
            val productName = product.key
            val uid = product.uid
            deleteData(uid,productName)
        }
    }

    fun getDailyProteinTotal(): Double {
        var totalProtein = 0.0
        for (product in productList)
        {
            totalProtein += product.protein
        }
        return totalProtein
    }

    fun getDailyFatTotal(): Double {
        var totalFat = 0.0
        for (product in productList)
        {
            totalFat += product.fat
        }
        return totalFat
    }

    fun getDailyCarbsTotal(): Double {
        var totalCarbs = 0.0
        for (product in productList)
        {
            totalCarbs += product.carbohydrates
        }
        return totalCarbs
    }

    fun getDailyKcalTotal(): Double {
        var totalKcal = 0.0
        for (product in productList)
        {
            totalKcal += product.kcal
        }
        return totalKcal
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    private fun deleteData(uid: String, productName: String) {
        val database =
            FirebaseDatabase.getInstance("https://fitapp-loginbase-default-rtdb.europe-west1.firebasedatabase.app")
        val productsRef = database.getReference("Users").child(uid).child("Breakfast").child(currentDate).child(productName)
        productsRef.removeValue().addOnSuccessListener {
        }
    }
}

class LunchAdapter(
    private var lunchItemList: List<Product>,
    private val onDeleteClickListener: OnDeleteClickListener
) : RecyclerView.Adapter<LunchAdapter.LunchViewHolder>() {

    val currentDate = SimpleDateFormat(
        "dd-MM-yyyy",
        Locale.getDefault()
    ).format(Date())

    inner class LunchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val uidTextView: TextView = itemView.findViewById(R.id.uidTextView)
        val keyTextView: TextView = itemView.findViewById(R.id.keyTextView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val caloriesTextView: TextView = itemView.findViewById(R.id.caloriesTextView)
        val fatTextView: TextView = itemView.findViewById(R.id.fatTextView)
        val proteinTextView: TextView = itemView.findViewById(R.id.proteinTextView)
        val carbohydratesTextView: TextView = itemView.findViewById(R.id.carbohydratesTextView)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }
    fun updateList(newList: List<Product>) {
        lunchItemList = newList.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LunchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lunch, parent, false)
        return LunchViewHolder(view)
    }

    override fun onBindViewHolder(holder: LunchViewHolder, position: Int) {
        val product = lunchItemList[position]
        holder.uidTextView.text = product.uid
        holder.keyTextView.text = product.key
        holder.nameTextView.text = product.productName
        holder.caloriesTextView.text = "${product.kcal}"
        holder.fatTextView.text = "${product.fat}"
        holder.proteinTextView.text = "${product.protein}"
        holder.carbohydratesTextView.text = "${product.carbohydrates}"
        holder.deleteButton.setOnClickListener {
            val productName = product.key
            val uid = product.uid
            deleteData(uid,productName)
        }
    }

    fun getDailyProteinTotal(): Double {
        var totalProtein = 0.0
        for (product in lunchItemList)
        {
            totalProtein += product.protein
        }
        return totalProtein
    }

    fun getDailyFatTotal(): Double {
        var totalFat = 0.0
        for (product in lunchItemList)
        {
            totalFat += product.fat
        }
        return totalFat
    }

    fun getDailyCarbsTotal(): Double {
        var totalCarbs = 0.0
        for (product in lunchItemList)
        {
            totalCarbs += product.carbohydrates
        }
        return totalCarbs
    }

    fun getDailyKcalTotal(): Double {
        var totalKcal = 0.0
        for (product in lunchItemList)
        {
            totalKcal += product.kcal
        }
        return totalKcal
    }

    override fun getItemCount(): Int {
        return lunchItemList.size
    }

    private fun deleteData(uid: String, productName: String) {
        val database =
            FirebaseDatabase.getInstance("https://fitapp-loginbase-default-rtdb.europe-west1.firebasedatabase.app")
        val productsRef = database.getReference("Users").child(uid).child("Lunch").child(currentDate).child(productName)
        productsRef.removeValue().addOnSuccessListener {
        }
    }
}

class DinnerAdapter(
    private var dinnerItemList: List<Product>,
    private val onDeleteClickListener: OnDeleteClickListener
) : RecyclerView.Adapter<DinnerAdapter.DinnerViewHolder>() {

    val currentDate = SimpleDateFormat(
        "dd-MM-yyyy",
        Locale.getDefault()
    ).format(Date())

    inner class DinnerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val uidTextView: TextView = itemView.findViewById(R.id.uidTextView)
        val keyTextView: TextView = itemView.findViewById(R.id.keyTextView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val caloriesTextView: TextView = itemView.findViewById(R.id.caloriesTextView)
        val fatTextView: TextView = itemView.findViewById(R.id.fatTextView)
        val proteinTextView: TextView = itemView.findViewById(R.id.proteinTextView)
        val carbohydratesTextView: TextView = itemView.findViewById(R.id.carbohydratesTextView)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }
    fun updateList(newList: List<Product>) {
        dinnerItemList = newList.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DinnerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dinner, parent, false)
        return DinnerViewHolder(view)
    }

    override fun onBindViewHolder(holder: DinnerViewHolder, position: Int) {
        val product = dinnerItemList[position]
        holder.uidTextView.text = product.uid
        holder.keyTextView.text = product.key
        holder.nameTextView.text = product.productName
        holder.caloriesTextView.text = "${product.kcal}"
        holder.fatTextView.text = "${product.fat}"
        holder.proteinTextView.text = "${product.protein}"
        holder.carbohydratesTextView.text = "${product.carbohydrates}"
        holder.deleteButton.setOnClickListener {
            val productName = product.key
            val uid = product.uid
            deleteData(uid,productName)
        }
    }

    fun getDailyProteinTotal(): Double {
        var totalProtein = 0.0
        for (product in dinnerItemList)
        {
            totalProtein += product.protein
        }
        return totalProtein
    }

    fun getDailyFatTotal(): Double {
        var totalFat = 0.0
        for (product in dinnerItemList)
        {
            totalFat += product.fat
        }
        return totalFat
    }

    fun getDailyCarbsTotal(): Double {
        var totalCarbs = 0.0
        for (product in dinnerItemList)
        {
            totalCarbs += product.carbohydrates
        }
        return totalCarbs
    }

    fun getDailyKcalTotal(): Double {
        var totalKcal = 0.0
        for (product in dinnerItemList)
        {
            totalKcal += product.kcal
        }
        return totalKcal
    }

    override fun getItemCount(): Int {
        return dinnerItemList.size
    }

    private fun deleteData(uid: String, productName: String) {
        val database =
            FirebaseDatabase.getInstance("https://fitapp-loginbase-default-rtdb.europe-west1.firebasedatabase.app")
        val productsRef = database.getReference("Users").child(uid).child("Dinner").child(currentDate).child(productName)
        productsRef.removeValue().addOnSuccessListener {
        }
    }
}