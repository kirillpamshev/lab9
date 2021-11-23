package ru.mgkit.lab9

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.util.ArrayList

object DATA_KEYS {
    const val SB_LIST = "SB_LIST"
    const val MODEL_DB = "MODEL_DB"
    const val ENTER_BROADCAST = "enterBroadcast"
    const val ENTER_SERVICE_PLAN = "ServicePlan"
    const val ENTER_NAME = "enterName"
    const val ENTER_FIELD = "enterField"
}

object REQUEST_CODES {
    const val PRINT_CODE = 10008
    const val EDIT_CODE = 10006
    const val EDIT_SEARCH_CODE = 10005
    const val FIND_CODE = 10004
    const val FIND_FIELD_CODE = 10003
    const val ADD_CODE = 10001
    const val SORT_CODE = 10002
    const val DELETE_CODE = 10007
}

object USING_CONST_COLLECTIONS {
    val sortMap: Map<Field, (ServicePlan, ServicePlan) -> Int> =
        mapOf(Field.NAME to { a, b -> a.servicePlanName.compareTo(b.servicePlanName) },
            Field.BROADCAST to { a, b -> a.typeOfBroadcast.compareTo(b.typeOfBroadcast) },
            Field.PA to { a, b -> a.publicAvailability.compareTo(b.publicAvailability) })
    val StrToBroadcast: Map<String, AllTypesOfBroadcast> =
        mapOf("REGULAR" to AllTypesOfBroadcast.REGULAR, "HD" to AllTypesOfBroadcast.HD)
    val BroadcastToStr: Map<AllTypesOfBroadcast, String> =
        mapOf(AllTypesOfBroadcast.REGULAR to "REGULAR", AllTypesOfBroadcast.HD to "HD")
    val IntToField: Map<Int, Field> = mapOf(0 to Field.NAME, 1 to Field.BROADCAST, 2 to Field.PA)
}


enum class Commands { // команды меню
    UNKNOWN_COMMAND, ADD, SORT, FIND, SHOW, EDIT, DELETE
}

enum class Field {
    NAME, BROADCAST, PA
}

enum class AllTypesOfBroadcast { // тип тарифоного плана
    REGULAR, HD
}

data class ServicePlan(
    var servicePlanName: String = "unknown service plan name", // датакласс тарифного плана
    var typeOfBroadcast: AllTypesOfBroadcast = AllTypesOfBroadcast.REGULAR,
    var publicAvailability: Boolean = false,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        USING_CONST_COLLECTIONS.StrToBroadcast[parcel.readString()]!!,
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(servicePlanName)
        USING_CONST_COLLECTIONS.BroadcastToStr[typeOfBroadcast]?.let { parcel.writeString(it) }
        parcel.writeByte(if (publicAvailability) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ServicePlan> {
        override fun createFromParcel(parcel: Parcel): ServicePlan {
            return ServicePlan(parcel)
        }

        override fun newArray(size: Int): Array<ServicePlan?> {
            return arrayOfNulls(size)
        }
    }
}


class ModelOfDB {
    private var objects : MutableList<ServicePlan> = mutableListOf()

    fun add(Name: String, tb: AllTypesOfBroadcast, publicAvailability: Boolean): Boolean {
        if (findIndexByName(Name) != -1) {
            return false
        }
        val el = ServicePlan(Name, tb, publicAvailability)
        objects.add(el)
        return true
    }


    fun get(n: Int): ServicePlan {
        return objects[n]
    }

    fun delete(Name: String): Boolean {
        if (objects.isNotEmpty()) {
            val index = findIndexByName(Name)
            if (index != -1) {
                objects.removeAt(index)
                return true
            }
        }
        return false
    }

    fun edit(oldName: String, newName: String, tb: AllTypesOfBroadcast, publicAvailability: Boolean): Boolean {
        val index = findIndexByName(oldName)
        return if (index != -1) {
            if (oldName == newName || oldName != newName && findIndexByName(newName) ==-1)
                false
            else{
                objects[index].servicePlanName = newName
                objects[index].typeOfBroadcast = tb
                objects[index].publicAvailability = publicAvailability
                true
            }
        } else {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun sort(ts: Field) { // сделать map от ts
        if (objects.isNotEmpty()) {
            objects.sortWith(USING_CONST_COLLECTIONS.sortMap.getOrDefault(ts) { _, _ -> 0 })
        }
    }

    private fun findIndexByName(qw: String): Int { // поиск индекса элемента по полю field, значению - qm; -1 если не найдено
        val el = objects.find { it.servicePlanName == qw } // ищем элемент
        return if (el != null){
            objects.indexOf(el) // определяем индекс
        } else -1
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun find(field: Field, qw: Any): Array<ServicePlan> { // разные поиски // тоже самое как в сортировке
        val seachPredicate: Map <Field, (ServicePlan) -> Boolean> = mapOf(Field.NAME to { it.servicePlanName == qw as String},
            Field.BROADCAST to { it.typeOfBroadcast ==  qw as AllTypesOfBroadcast},
            Field.PA to { it.publicAvailability == qw as Boolean} )
        return objects.filter(seachPredicate.getOrDefault(field) { (true) }).toTypedArray()
    }

    fun giveAll(): Array<ServicePlan> {
        return objects.toTypedArray() // преобразование в массив
    }

    fun giveHashedNames(): Array<Int> {
        return objects.map { it.servicePlanName.hashCode() }.toTypedArray()
    }

    fun setAll(arr: Array<ServicePlan>){
        objects = arr.toMutableList()
    }

}

class Reader {
    fun read(editText: EditText): String{
        return editText.text.toString()
    }
}

class Checker(checkRegString: String = "[A-Za-z0-9_]+") { // в конструктор передаётся не проверяемая строка а строка для регекспа!
    private var checkRegex: Regex = checkRegString.toRegex()
    fun check(s: String): Boolean {
        return s.matches(checkRegex)
    }
}

class EnterBroadcast {

    fun precheck(s: Spinner):Boolean {
        return s.selectedItem != null
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun convert(s: Spinner):AllTypesOfBroadcast {
        return USING_CONST_COLLECTIONS.StrToBroadcast.getOrDefault((s.selectedItem as String), AllTypesOfBroadcast.REGULAR)
    }

}

class ToPrint {
    private lateinit var cnt: AppCompatActivity
    fun setContext(cnt: AppCompatActivity) : ToPrint{
        this.cnt = cnt
        return this
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun print(s: String) = Toast.makeText(cnt, s, Toast.LENGTH_SHORT).show()


    @RequiresApi(Build.VERSION_CODES.N)
    fun printList(arr: Array<ServicePlan>) {
        if (arr.isNotEmpty()) {
            val intent = Intent(cnt, ListActivity::class.java)
            intent.putExtra(DATA_KEYS.SB_LIST, arr)
            cnt.startActivityForResult(intent, REQUEST_CODES.PRINT_CODE)
        }
        else {
            print("Нет отображаемых элементов!")
        }
    }
}

data class PunktOfMenu(var Number: Int,
                       var Name: String,
                       var Command: Commands)

class Menu {
    private var punkts: MutableList<PunktOfMenu> = mutableListOf()
    private lateinit var menuGroup: RadioGroup
    private lateinit var cnt: Context
    fun add(punktOfMenu: PunktOfMenu) {
        punkts.add(punktOfMenu)
    }

    fun setGroup(group: RadioGroup) {
        menuGroup = group
    }

    fun setContext(cnt: Context) {
        this.cnt = cnt
    }

    fun printMenu() {
        punkts.forEach {
            val rb = RadioButton(cnt)
            rb.text = it.Name
            menuGroup.addView(rb)
        }
    }

    fun selectMenu(): Commands {
        var curCommand = Commands.UNKNOWN_COMMAND
        val count = menuGroup.childCount
        for (i in 0 until count) {
            val el = menuGroup.getChildAt(i)
            if (el is RadioButton) {
                if (el.isChecked) {
                    curCommand = punkts.find { it.Name == el.text }?.Command!!
                }
            }
        }
        return curCommand
    }
}


class EnterUI (private var cnt: MainActivity) {

    fun enter(code: Int, sb: ServicePlan? = null) {
        val intent = Intent(cnt, EnterActivity::class.java)
        if (sb != null) {
            intent.putExtra(DATA_KEYS.ENTER_SERVICE_PLAN, sb)
        }
        cnt.startActivityForResult(intent, code)
    }
}

class ViewOfDB(private var db: ModelOfDB, private var printer: ToPrint = ToPrint()) {
    private val commandMap: Map<Commands,() -> Unit > = mapOf(Commands.ADD to  this::add,
        Commands.SORT to this::sort,
        Commands.FIND to this::search,
        Commands.SHOW to this::printAll,
        Commands.EDIT to this::edit,
        Commands.DELETE to  this::delete,
    )

    private val requestCodeMap: Map<Int,(Intent)->Unit> = mapOf()

    private lateinit var cnt: MainActivity

    fun setContext(cnt: MainActivity) {
        this.cnt = cnt
        printer.setContext(cnt)
    }

    private fun add() {
        EnterUI(cnt).enter(REQUEST_CODES.ADD_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun add(data: Intent) {
        val servicePlan = data.getParcelableExtra<ServicePlan>(DATA_KEYS.ENTER_SERVICE_PLAN)
        if (servicePlan != null) {
            if(db.add(servicePlan.servicePlanName, servicePlan.typeOfBroadcast, servicePlan.publicAvailability))
                printer.print("План добавлен успешно!")
            else
                printer.print("Ошибка добаления!")
        }
    }

    private fun delete() {
        val intent = Intent(cnt, EnterPlanName::class.java)
        cnt.startActivityForResult(intent, REQUEST_CODES.DELETE_CODE)
    }
    @RequiresApi(Build.VERSION_CODES.N)
    private fun delete(data: Intent) {
        val curVal = data.getStringExtra(DATA_KEYS.ENTER_NAME)
        if (curVal != null) {
            if (db.delete(curVal))
                printer.print("Элемент удалён!")
            else
                printer.print("Ошибка удаления!")
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun edit() {
        val intent = Intent(cnt, EnterPlanName::class.java)
        cnt.startActivityForResult(intent, REQUEST_CODES.EDIT_SEARCH_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun edit_search(data: Intent) {
        val curVal = data.getStringExtra(DATA_KEYS.ENTER_NAME)
        if (curVal != null) {
            val tempArray: Array<ServicePlan> = db.find(Field.NAME, curVal)
            if (tempArray.size == 1) {
                val tempsp = tempArray[0]
                EnterUI(cnt).enter(REQUEST_CODES.EDIT_CODE, tempsp)
            } else
                printer.print("Элемент не найден или ошибка БД")
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun edit(data: Intent) {
        val servicePlan = data.getParcelableExtra<ServicePlan>(DATA_KEYS.ENTER_SERVICE_PLAN)
        val curVal = data.getStringExtra(DATA_KEYS.ENTER_NAME)
        if (servicePlan != null && curVal != null) {
            if (db.edit(curVal, servicePlan.servicePlanName, servicePlan.typeOfBroadcast, servicePlan.publicAvailability))
                printer.print("Элемент изменён!")
            else
                printer.print("Ошибка редактирования!")
        }
    }

    private fun sort() {
        val intent = Intent(cnt, EnterFieldActivity::class.java)
        cnt.startActivityForResult(intent, REQUEST_CODES.SORT_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun sort(data: Intent) {
        val field = USING_CONST_COLLECTIONS.IntToField[data.getIntExtra(DATA_KEYS.ENTER_FIELD,0)]
        if (field != null)
            db.sort(field)
    }

    private fun search() {
        val intent = Intent(cnt, EnterFieldActivity::class.java)
        cnt.startActivityForResult(intent, REQUEST_CODES.FIND_FIELD_CODE)
    }

    private fun search_field(data: Intent) {
        val field = USING_CONST_COLLECTIONS.IntToField[data.getIntExtra(DATA_KEYS.ENTER_FIELD,0)]
        if (field != null){
             when(field) {
                Field.NAME -> {
                    Intent(cnt, EnterPlanName::class.java)
                }
                Field.BROADCAST -> {



                }
                Field.PA -> {

                }
            }

        }
    }

    private fun search(data: Intent) {

    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun printAll() {
        val arr = db.giveAll()
        printer.printList(arr)
    }

    fun runCommand (theCommand: Commands) = commandMap[theCommand]?.let { it() }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getAnsver(requestCode: Int, data: Intent) {
        when (requestCode) {
        REQUEST_CODES.ADD_CODE  -> add(data)
        REQUEST_CODES.SORT_CODE -> sort(data)
        REQUEST_CODES.FIND_FIELD_CODE -> search_field(data)
        REQUEST_CODES.FIND_CODE -> search(data)
        REQUEST_CODES.EDIT_SEARCH_CODE -> edit_search(data)
        REQUEST_CODES.EDIT_CODE -> edit(data)
        REQUEST_CODES.DELETE_CODE -> delete(data)
        }
    }
}

open class App {
    protected val dataBase:ModelOfDB = ModelOfDB()
    val view = ViewOfDB(dataBase)
    protected val menu = Menu()
    init {
        menu.add(PunktOfMenu(1, "Добавить", Commands.ADD))
        menu.add(PunktOfMenu(2, "Удалить", Commands.DELETE))
        menu.add(PunktOfMenu(3, "Редактировать", Commands.EDIT))
        menu.add(PunktOfMenu(4, "Поиск", Commands.FIND))
        menu.add(PunktOfMenu(5, "Сортировать", Commands.SORT))
        menu.add(PunktOfMenu(6, "Показать всё", Commands.SHOW))
    }
}

class AndroidApp(button: Button, radioGroup: RadioGroup, private var cnt: MainActivity) :App(), View.OnClickListener{
    init {
        menu.setGroup(radioGroup)
        menu.setContext(cnt)
        button.setOnClickListener(this)
        view.setContext(cnt)
        menu.printMenu()
    }

    fun setDB(bundle: Bundle) {
        val array:ArrayList<ServicePlan> = bundle.getParcelableArrayList(DATA_KEYS.MODEL_DB)!!
        dataBase.setAll(array.toTypedArray())
    }

    fun getArrList():ArrayList<ServicePlan> = dataBase.giveAll().toCollection(ArrayList<ServicePlan>())


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(p0: View?) {
        val entr = menu.selectMenu()
        if (entr != Commands.UNKNOWN_COMMAND)
            view.runCommand(entr)
        else
            ToPrint().setContext(cnt).print("Не выбран пункт меню!")
    }
}


class MainActivity : AppCompatActivity() {

    private lateinit var enterBut:Button
    private lateinit var menuGroup:RadioGroup
    private lateinit var app: AndroidApp
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enterBut = findViewById(R.id.enterBut)
        menuGroup = findViewById(R.id.menuGroup)
        app = AndroidApp(enterBut, menuGroup, this)
        if (savedInstanceState != null){
            app.setDB(savedInstanceState)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (data != null) {
                app.view.getAnsver(requestCode, data)
            }

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val arrayList = app.getArrList()
        if (arrayList.isNotEmpty()) {
            outState.putParcelableArrayList(DATA_KEYS.MODEL_DB, arrayList)
        }
    }

}