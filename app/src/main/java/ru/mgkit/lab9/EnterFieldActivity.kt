package ru.mgkit.lab9

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup

class EnterFieldActivity : AppCompatActivity() {
    private val ENTER_FIELD = "enterField"
    val fieldsList = arrayListOf<String>("Название плана", "Тип вещания", "Общедоступность")
    lateinit var button: Button
    lateinit var radioGroup: RadioGroup
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_field)
        radioGroup = findViewById(R.id.radioFieldGroup)
        fieldsList.forEach {
            val rb = RadioButton(this)
            rb.text = it
            radioGroup.addView(rb)
        }

        button = findViewById(R.id.enterFieldType)
        button.setOnClickListener {
            val count = radioGroup.childCount
            for (i in 0 until count) {
                val el = radioGroup.getChildAt(i)
                if (el is RadioButton) {
                    if (el.isChecked) {
                        val data = Intent().apply {
                            putExtra(ENTER_FIELD,i)
                        }
                        setResult(RESULT_OK, data)
                        finish()
                    }
                }
            }
        }
    }

}