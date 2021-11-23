package ru.mgkit.lab9

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi

class EnterActivity : AppCompatActivity() {

    private lateinit var nameText: EditText
    private lateinit var tbSpin:Spinner
    private lateinit var availBox: CheckBox
    private lateinit var tbList: Array<String>
    private lateinit var printer: ToPrint
    private lateinit var enterBut: Button

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter)
        printer = ToPrint()
        printer.setContext(this)
        nameText = findViewById(R.id.nameText)
        tbSpin = findViewById(R.id.tbSpin)
        tbList = USING_CONST_COLLECTIONS.BroadcastToStr.entries.sortedBy { it.key}.map { it.value }.toTypedArray()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tbList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tbSpin.adapter = adapter
        availBox = findViewById(R.id.availBox)
        enterBut = findViewById(R.id.enterplanBut)

        //
        // Забрать из bundle содержимое текст поля и положение спинера и установку чек бокса
        //
        enterBut.setOnClickListener(View.OnClickListener {
            val plan = setResult()
            if (plan != null) {
                val data = Intent().apply {
                    putExtra(DATA_KEYS.ENTER_SERVICE_PLAN,plan)
                }
                setResult(RESULT_OK, data)
                finish()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun setResult():ServicePlan? {
        var plan:ServicePlan? = null
        if (Checker().check(Reader().read(nameText))) {
            val stringToBroadcast = EnterBroadcast()
            if (stringToBroadcast.precheck(tbSpin)) plan = ServicePlan(Reader().read(nameText),
                stringToBroadcast.convert(tbSpin),
                availBox.isChecked)
            else
                printer.print("Не выбран тип тарифного плана!")
        } else {
            printer.print("Недопустимое имя плана при вводе!")
        }
        return plan
    }
}