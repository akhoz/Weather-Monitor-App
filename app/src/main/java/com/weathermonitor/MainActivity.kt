package com.weathermonitor

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Firebase initialization
        FirebaseApp.initializeApp(this)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        databaseReference = FirebaseDatabase.getInstance().reference

        val temperature = databaseReference.child("Temperature").child("2023-09-27").child("00").child("celcius")
        val textView = findViewById<TextView>(R.id.message)

        temperature.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Long::class.java)
                textView.text = value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value.")
            }
        })
        
    }

}