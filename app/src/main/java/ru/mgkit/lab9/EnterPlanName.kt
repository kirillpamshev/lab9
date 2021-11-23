package ru.mgkit.lab9

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class EnterPlanName : AppCompatActivity() {
    lateinit var button: Button
    lateinit var text: EditText
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_plan_name)
        text = findViewById(R.id.EditPlanName)
        button = findViewById(R.id.EnterPlanName)
        button.setOnClickListener {
            if (Checker().check(text.text.toString())){
                val data = Intent().apply {
                    putExtra(DATA_KEYS.ENTER_NAME,text.text.toString())
                }
                setResult(RESULT_OK, data)
                finish()
            }
            else
                ToPrint().setContext(this).print("Неверное имя плана!")
        }
    }
}