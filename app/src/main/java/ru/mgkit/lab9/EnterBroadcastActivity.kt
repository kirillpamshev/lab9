package ru.mgkit.lab9

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AbsSpinner
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner

class EnterBroadcastActivity : AppCompatActivity() {

    private val ENTER_BROADCAST = "enterBroadcast"
    lateinit var button: Button
    lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_broadcast)
        spinner = findViewById(R.id.broadcastSpinner)
        val tbList = ToPrint().broadcastToString.entries.sortedBy { it.key}.map { it.value }.toTypedArray()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tbList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        button = findViewById(R.id.enterBroadcast)
        button.setOnClickListener {
            val stringToBroadcast = EnterBroadcast()
            if (stringToBroadcast.precheck(spinner)){
                val data = Intent().apply {
                    putExtra(ENTER_BROADCAST, spinner.selectedItem as String)
                }
                setResult(RESULT_OK, data)
                parentActivityIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(parentActivityIntent)

            }
            else
                ToPrint().setContext(this).print("Не выбран тип вещания!")
        }




    }
}