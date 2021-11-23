package ru.mgkit.lab9

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.*


class ServicePlanAdapter(context: Context?, resource: Int, states: List<ServicePlan>) :
    ArrayAdapter<ServicePlan>(context!!, resource, states) {

    private val inflater: LayoutInflater
    private val layout: Int
    private val states: List<ServicePlan>

    init {
        this.states = states
        layout = resource
        inflater = LayoutInflater.from(context)
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = inflater.inflate(layout, parent, false)

        val itemName: TextView  = view.findViewById(R.id.itemName)
        val itemTB: TextView = view.findViewById(R.id.itemTB)
        val itemPA: TextView = view.findViewById(R.id.itemPA)
        val sp: ServicePlan = states[position]
        itemName.text = sp.servicePlanName
        itemTB.text = USING_CONST_COLLECTIONS.BroadcastToStr[sp.typeOfBroadcast]
        itemPA.text = if (sp.publicAvailability) "Да" else "Нет"
        return view
    }


}
class ListActivity : AppCompatActivity() {
    private lateinit var myListItems: ListView

    private lateinit var arrayItems: List<ServicePlan>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        myListItems = findViewById(R.id.itemsList)
        arrayItems = intent.getParcelableArrayExtra(DATA_KEYS.SB_LIST)?.asList() as List<ServicePlan>
        myListItems.adapter = ServicePlanAdapter(this, R.layout.list_item, arrayItems)
    }
}