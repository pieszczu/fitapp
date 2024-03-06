package com.example.myapplicationpls

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.harrywhewell.scrolldatepicker.DayScrollDatePicker
import org.joda.time.LocalDate
import java.text.SimpleDateFormat
import java.util.*

class action_history_trainings : Fragment() {
    private lateinit var mPicker: DayScrollDatePicker
    private lateinit var trainingAdapter: TrainingAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_action_history_trainings, container, false)
        mPicker = view.findViewById(R.id.dayPicker)

        val today = LocalDate()
        auth = Firebase.auth // current user in this fragment
        val currentDate = Date()

        handleSelectedDate(currentDate) // set view on current day
        val startDate = today.minusDays(7)
        mPicker.setStartDate(startDate.dayOfMonth, startDate.monthOfYear, startDate.year)
        mPicker.setEndDate(today.dayOfMonth, today.monthOfYear, today.year)

        trainingAdapter = TrainingAdapter(emptyList()) // progress list

        mPicker.getSelectedDate { date ->
            handleSelectedDate(date)
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.trainingList) // ecyclerView to progress list
        recyclerView.adapter = trainingAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        return view
    }

    private fun updateTrainingList(trainingList: List<Training>) { // update data
        trainingAdapter.updateList(trainingList)
    }

    private fun updateTrainingListForDate(date: String) { // update list with exercises in this day (data from Firebase)
        val user = auth.currentUser
        val uid = user?.uid.toString()

        val trainingRef = FirebaseDatabase.getInstance().getReference("Users")
            .child(uid)
            .child("Training")
            .child(date)

        trainingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val trainingList = mutableListOf<Training>()

                for (trainingSnapshot in dataSnapshot.children) {
                    val training = trainingSnapshot.getValue(Training::class.java)
                    training?.let {
                        trainingList.add(it)
                    }
                }

                updateTrainingList(trainingList) // update list in adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }


    private fun handleSelectedDate(selectedDate: Date?) { // manipulate date
        selectedDate?.let {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate)
            updateTrainingListForDate(formattedDate)
        }
    }
}

data class Training( // class Training constructor for Firebase data
    val key: String = "",
    val typeOfExercise: String = "",
    val time: String = "",
    val series: Int = 0,
    val reps: Int = 0,
    val kcal: Int = 0
)

class TrainingAdapter( // Firebase ---> Adapter
    private var trainingItemList: List<Training>,
) : RecyclerView.Adapter<TrainingAdapter.TrainingViewHolder>() {

    inner class TrainingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val keyTextView: TextView = itemView.findViewById(R.id.keyTextView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val seriesTextView: TextView = itemView.findViewById(R.id.seriesTextView)
        val repsTextView: TextView = itemView.findViewById(R.id.repsTextView)
        val caloriesTextView: TextView = itemView.findViewById(R.id.caloriesTextView)
    }

    fun updateList(newList: List<Training>) {
        trainingItemList = newList.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_training, parent, false)
        return TrainingViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrainingViewHolder, position: Int) {
        val training = trainingItemList[position]
        holder.keyTextView.text = training.key
        holder.nameTextView.text = training.typeOfExercise
        holder.timeTextView.text = "     TIME: ${training.time}"
        holder.seriesTextView.text = "   SERIES: ${training.series}"
        holder.repsTextView.text = "   REPS: ${training.reps}"
        holder.caloriesTextView.text = "   Kcal: ${training.kcal}"
    }

    override fun getItemCount(): Int {
        return trainingItemList.size
    }
}
