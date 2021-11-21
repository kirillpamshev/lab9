package ru.mgkit.lab9

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox

class EnterPA_Activity : AppCompatActivity() {


    private val ENTER_AVAILABLE = "enterAvailable"
    lateinit var checkBox: CheckBox
    lateinit var button: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_pa)
        checkBox = findViewById(R.id.checkAvailable)
        button = findViewById(R.id.enterAvail)

        button.setOnClickListener {
            val data = Intent().apply {
                putExtra(ENTER_AVAILABLE, checkBox.isChecked)
            }
            setResult(RESULT_OK, data)
            parentActivityIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(parentActivityIntent)

        }

    }
}