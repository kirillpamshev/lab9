package ru.mgkit.lab9

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class EnterPlanName : AppCompatActivity() {
    private val ENTER_NAME = "enterName"
    lateinit var button: Button
    lateinit var text: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_plan_name)
        text = findViewById(R.id.EditPlanName)
        button = findViewById(R.id.EnterPlanName)
        button.setOnClickListener {
            if (Checker().check(text.text.toString())){
                val data = Intent().apply {
                    putExtra(ENTER_NAME,text.text.toString())
                }
                setResult(RESULT_OK, data)
                parentActivityIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(parentActivityIntent)
            }
            else
                ToPrint().setContext(this).print("Неверное имя плана!")
        }

    }
}