package com.weathermonitor

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("SuspiciousIndentation", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Firebase section----------------------------------------------------------
        FirebaseApp.initializeApp(this)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        databaseReference = FirebaseDatabase.getInstance().reference

        val temperatureReport = mutableListOf<Any>()
        val temperatureKeys = mutableListOf<String>()
        val weatherMonitorReference = databaseReference.child("Temperature")

        weatherMonitorReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dateReference in snapshot.children) {
                    val value = dateReference.getValue()
                    val key = dateReference.key.toString()
                    key.let { temperatureKeys.add(it) }
                    value?.let { temperatureReport.add(it) }
                }
                //textView.text = temperatureReport.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value.")
            }
        })

        //UI section----------------------------------------------------------
        val dayButton = findViewById<Button>(R.id.dayButton)
        val inRangeButton = findViewById<Button>(R.id.inRangeButton)

        val spinner = findViewById<Spinner>(R.id.spinner)
        dayButton.setOnClickListener {

            //Spinner construction
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, temperatureKeys)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.visibility = View.VISIBLE

            //Spinner selection
            spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    println("Nothing selected")
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedOption = temperatureKeys[position]

                    when (selectedOption)  {
                        "2023-09-27" -> {

                        }
                    }
                }
            })


        }
    }
}
