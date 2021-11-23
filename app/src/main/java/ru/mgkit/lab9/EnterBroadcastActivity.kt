package ru.mgkit.lab9

import android.R.*
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AbsSpinner
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.annotation.RequiresApi

class EnterBroadcastActivity : AppCompatActivity() {


    lateinit var button: Button
    lateinit var spinner: Spinner

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_broadcast)
        spinner = findViewById(R.id.broadcastSpinner)
        val tbList = USING_CONST_COLLECTIONS.BroadcastToStr.entries.sortedBy { it.key}.map { it.value }.toTypedArray()
        val adapter = ArrayAdapter(this, layout.simple_spinner_dropdown_item, tbList)
        adapter.setDropDownViewResource(layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        button = findViewById(R.id.enterBroadcast)
        button.setOnClickListener {
            val stringToBroadcast = EnterBroadcast()
            if (stringToBroadcast.precheck(spinner)){
                val data = Intent().apply {
                    putExtra(DATA_KEYS.ENTER_BROADCAST, spinner.selectedItem as String)
                }
                setResult(RESULT_OK, data)
                finish()

            }
            else
                ToPrint().setContext(this).print("Не выбран тип вещания!")
        }




    }
}