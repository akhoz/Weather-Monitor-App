package com.weathermonitor

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*

import android.graphics.Color
import android.text.TextWatcher
import android.widget.EditText
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.transition.MaterialSharedAxis.Axis


class MainActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("SuspiciousIndentation", "MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Firebase section----------------------------------------------------------
        FirebaseApp.initializeApp(this)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        databaseReference = FirebaseDatabase.getInstance().reference

        //val textView = findViewById<TextView>(R.id.textView)
        //val textView2 = findViewById<TextView>(R.id.textView2)

        val temperatureReport = mutableListOf<Any>()
        val celciusReport = mutableMapOf<String, Map<String, Any>>()
        val farenheitReport = mutableMapOf<String, Map<String, Any>>()


        val celciusMap = mutableMapOf<String, Any>()
        val farenheitMap = mutableMapOf<String, Any>()
        val temperatureKeys = mutableListOf<String>()
        val historicalValue = findViewById<TextView>(R.id.historicalValue)
        val weatherMonitorReference = databaseReference.child("Temperature")

        weatherMonitorReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dateReference in snapshot.children) {
                    val value = dateReference.getValue()
                    val key = dateReference.key.toString()
                    key.let { temperatureKeys.add(it) }
                    value?.let { temperatureReport.add(it) }

                    val celciusMap = mutableMapOf<String, Any>() // Nuevo mapa para cada clave
                    val farenheitMap = mutableMapOf<String, Any>() // Nuevo mapa para cada clave

                    for (hourReference in dateReference.children) {
                        val hourKey = hourReference.key.toString()
                        val hourCelcius = hourReference.child("celcius").getValue()
                        val hourFarenheit = hourReference.child("farenheit").getValue()
                        celciusMap[hourReference.key.toString()] = hourCelcius!!
                        farenheitMap[hourReference.key.toString()] = hourFarenheit!!
                    }

                    celciusReport[key] = celciusMap
                    farenheitReport[key] = farenheitMap
                }

                var maxCelcius: Int? = null
                var minCelcius: Int? = null
                var maxFarenheit: Int? = null
                var minFarenheit: Int? = null

                for ((date, hours) in celciusReport) {
                    for ((hour, temp) in hours) {
                        if (maxCelcius == null || temp.toString().toInt() > maxCelcius!!) {
                            maxCelcius = temp.toString().toInt()
                        }
                        if (minCelcius == null || temp.toString().toInt() < minCelcius!!) {
                            minCelcius = temp.toString().toInt()
                        }
                    }
                }

                for ((date, hours) in farenheitReport) {
                    for ((hour, temp) in hours) {
                        if (maxFarenheit == null || temp.toString().toInt() > maxFarenheit!!) {
                            maxFarenheit = temp.toString().toInt()
                        }
                        if (minFarenheit == null || temp.toString().toInt() < minFarenheit!!) {
                            minFarenheit = temp.toString().toInt()
                        }
                    }
                }

                historicalValue.text = "Max: $maxCelcius C, $maxFarenheit F - Min: $minCelcius C, $minFarenheit F"
            }
            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value.")
            }
        })

        //UI section----------------------------------------------------------

        val dayButton = findViewById<Button>(R.id.dayButton)
        val inRangeButton = findViewById<Button>(R.id.inRangeButton)

        val spinner = findViewById<Spinner>(R.id.spinner)
        val initialDate = findViewById<EditText>(R.id.initialDate)
        val finalDate = findViewById<EditText>(R.id.finalDate)

        val celciusEntries = ArrayList<Entry>()
        val farenheitEntries = ArrayList<Entry>()
        val lineChart = findViewById<LineChart>(R.id.lineChart)
        val historicalMsg = findViewById<TextView>(R.id.historicalMsg)
        val queryMsg = findViewById<TextView>(R.id.queryMsg)
        val queryValue = findViewById<TextView>(R.id.queryValue)
        val xAxis = lineChart.xAxis

        dayButton.setOnClickListener {

            //Spinner construction
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, temperatureKeys)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.visibility = View.VISIBLE

            initialDate.visibility = View.INVISIBLE
            finalDate.visibility = View.INVISIBLE
            historicalMsg.visibility = View.VISIBLE
            historicalValue.visibility = View.VISIBLE
            queryMsg.visibility = View.INVISIBLE
            queryValue.visibility = View.INVISIBLE
            lineChart.visibility = View.INVISIBLE

            xAxis.isEnabled = false

            //Spinner selection
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    println("Nothing selected")
                    //textView.text = "Nothing selected"
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedOption = temperatureKeys[position].toString()
                    //textView2.text = selectedOption


                    celciusEntries.clear()
                    farenheitEntries.clear()

                    val celciusData = celciusReport[selectedOption]
                    //textView.text = celciusReport[selectedOption].toString()
                    val farenheitData = farenheitReport[selectedOption]

                    for (hour in celciusData!!.keys) {
                        celciusEntries.add(Entry(hour.toFloat(), celciusData[hour].toString().toFloat()))
                    }

                    for (hour in farenheitData!!.keys) {
                        farenheitEntries.add(Entry(hour.toFloat(), farenheitData[hour].toString().toFloat()))
                    }

                    val celciusDataSet = LineDataSet(celciusEntries, "Celcius")
                    val farenheitDataSet = LineDataSet(farenheitEntries, "Farenheit")

                    celciusDataSet.color = Color.RED
                    farenheitDataSet.color = Color.BLUE
                    lineChart.visibility = View.VISIBLE
                    val lineData = LineData(celciusDataSet, farenheitDataSet)
                    lineChart.data = lineData
                    //lineChart.visibility = View.VISIBLE
                    lineChart.data.notifyDataChanged()
                    lineChart.notifyDataSetChanged()
                    lineChart.invalidate()

                    var maxCelcius: Int? = null
                    var minCelcius: Int? = null
                    var maxFarenheit: Int? = null
                    var minFarenheit: Int? = null

                    for ((hour,temp) in celciusData) {
                        if (maxCelcius == null || temp.toString().toInt() > maxCelcius!!) {
                            maxCelcius = temp.toString().toInt()
                        }
                        if (minCelcius == null || temp.toString().toInt() < minCelcius!!) {
                            minCelcius = temp.toString().toInt()
                        }
                    }

                    for ((hour,temp) in farenheitData) {
                        if (maxFarenheit == null || temp.toString().toInt() > maxFarenheit!!) {
                            maxFarenheit = temp.toString().toInt()
                        }
                        if (minFarenheit == null || temp.toString().toInt() < minFarenheit!!) {
                            minFarenheit = temp.toString().toInt()
                        }
                    }

                    queryMsg.visibility = View.VISIBLE
                    queryValue.visibility = View.VISIBLE
                    queryValue.text = "Max: $maxCelcius C, $maxFarenheit F - Min: $minCelcius C, $minFarenheit F"

                }
            }


        }


        inRangeButton.setOnClickListener {
            spinner.visibility = View.INVISIBLE

            lineChart.visibility = View.INVISIBLE
            historicalMsg.visibility = View.VISIBLE
            historicalValue.visibility = View.VISIBLE
            initialDate.visibility = View.VISIBLE
            finalDate.visibility = View.VISIBLE

            queryMsg.visibility = View.VISIBLE
            queryValue.visibility = View.VISIBLE


            initialDate.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    //This should be empty
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    //This should be empty
                }

                override fun afterTextChanged(s: android.text.Editable) {
                    val initialDateValue = initialDate.text.toString()
                    val finalDateValue = finalDate.text.toString()

                    if (initialDateValue in temperatureKeys && finalDateValue in temperatureKeys) {
                        val initialDateIndex = temperatureKeys.indexOf(initialDateValue)
                        val finalDateIndex = temperatureKeys.indexOf(finalDateValue)

                        val celciusEntries = ArrayList<Entry>()
                        val farenheitEntries = ArrayList<Entry>()

                        val celciusDateLabels = mutableListOf<String>()

                        for (i in initialDateIndex..finalDateIndex) {
                            val date = temperatureKeys[i]
                            val celciusData = celciusReport[date]
                            val farenheitData = farenheitReport[date]

                            // Calcular el promedio de las temperaturas para el día
                            val celciusAverage = celciusData?.values?.map { it.toString().toFloat() }?.average()!!.toFloat()
                            val farenheitAverage = farenheitData?.values?.map { it.toString().toFloat() }?.average()!!.toFloat()

                            celciusEntries.add(Entry(i.toFloat(), celciusAverage))
                            farenheitEntries.add(Entry(i.toFloat(), farenheitAverage))
                            celciusDateLabels.add(date)
                        }

                        xAxis.isEnabled = true

                        xAxis.valueFormatter = IndexAxisValueFormatter(celciusDateLabels)
                        xAxis.labelRotationAngle = 45f

                        val celciusDataSet = LineDataSet(celciusEntries, "Celsius")
                        val farenheitDataSet = LineDataSet(farenheitEntries, "Fahrenheit")

                        celciusDataSet.color = Color.RED
                        farenheitDataSet.color = Color.BLUE

                        val lineData = LineData(celciusDataSet, farenheitDataSet)
                        lineChart.data = lineData
                        lineChart.visibility = View.VISIBLE
                        lineChart.invalidate()
                    }

                    else {
                        lineChart.visibility = View.INVISIBLE
                    }
                }

            })

            finalDate.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    //This should be empty
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    //This should be empty
                }

                override fun afterTextChanged(s: android.text.Editable) {
                    val initialDateValue = initialDate.text.toString()
                    val finalDateValue = finalDate.text.toString()

                    if (initialDateValue in temperatureKeys && finalDateValue in temperatureKeys) {
                        val initialDateIndex = temperatureKeys.indexOf(initialDateValue)
                        val finalDateIndex = temperatureKeys.indexOf(finalDateValue)

                        val celciusEntries = ArrayList<Entry>()
                        val farenheitEntries = ArrayList<Entry>()

                        for (i in initialDateIndex..finalDateIndex) {
                            val date = temperatureKeys[i]
                            val celciusData = celciusReport[date]
                            val farenheitData = farenheitReport[date]

                            // Calcular el promedio de las temperaturas para el día
                            val celciusAverage = celciusData?.values?.map { it.toString().toFloat() }?.average()!!.toFloat()
                            val farenheitAverage = farenheitData?.values?.map { it.toString().toFloat() }?.average()!!.toFloat()

                            celciusEntries.add(Entry(i.toFloat(), celciusAverage))
                            farenheitEntries.add(Entry(i.toFloat(), farenheitAverage))
                        }

                        val celciusDateLabels = temperatureKeys.subList(initialDateIndex, finalDateIndex + 1)

                        val xAxis = lineChart.xAxis
                        xAxis.valueFormatter = IndexAxisValueFormatter(celciusDateLabels)
                        xAxis.labelRotationAngle = 45f

                        val celciusDataSet = LineDataSet(celciusEntries, "Celsius")
                        val farenheitDataSet = LineDataSet(farenheitEntries, "Fahrenheit")

                        celciusDataSet.color = Color.RED
                        farenheitDataSet.color = Color.BLUE

                        val lineData = LineData(celciusDataSet, farenheitDataSet)
                        lineChart.data = lineData
                        lineChart.visibility = View.VISIBLE
                        lineChart.invalidate()

                        var maxCelcius: Int? = null
                        var minCelcius: Int? = null
                        var maxFarenheit: Int? = null
                        var minFarenheit: Int? = null

                        for(i in initialDateIndex..finalDateIndex) {
                            val date = temperatureKeys[i]
                            val celciusData = celciusReport[date]
                            val farenheitData = farenheitReport[date]

                            for ((hour,temp) in celciusData!!) {
                                if (maxCelcius == null || temp.toString().toInt() > maxCelcius!!) {
                                    maxCelcius = temp.toString().toInt()
                                }
                                if (minCelcius == null || temp.toString().toInt() < minCelcius!!) {
                                    minCelcius = temp.toString().toInt()
                                }
                            }

                            for ((hour,temp) in farenheitData!!) {
                                if (maxFarenheit == null || temp.toString().toInt() > maxFarenheit!!) {
                                    maxFarenheit = temp.toString().toInt()
                                }
                                if (minFarenheit == null || temp.toString().toInt() < minFarenheit!!) {
                                    minFarenheit = temp.toString().toInt()
                                }
                            }
                        }

                        queryValue.text = "Max: $maxCelcius C, $maxFarenheit F - Min: $minCelcius C, $minFarenheit F"

                    }

                    else {
                        lineChart.visibility = View.INVISIBLE
                    }
                }

            })


        }
    }
}

