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


/*
---Задание 1.
База данных тарифных планов оператора. Поля: название, тип вещания (обычный/HD), флаг общедоступности.*/

enum class Commands { // команды меню
    UNKNOWN_COMMAND, ADD, SORT, FIND, SHOW, EDIT, DELETE, QUIT
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
    public var BroadcastToInt:Map<AllTypesOfBroadcast,Int> = mapOf(AllTypesOfBroadcast.REGULAR to 0, AllTypesOfBroadcast.HD to 1)
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        mapOf(0 to AllTypesOfBroadcast.REGULAR, 1 to AllTypesOfBroadcast.HD)[parcel.readInt()]!!,
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(servicePlanName)
        BroadcastToInt[typeOfBroadcast]?.let { parcel.writeInt(it) }
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
    private val sortMap: Map<Field, (ServicePlan, ServicePlan)-> Int> = mapOf(Field.NAME to { a, b -> a.servicePlanName.compareTo(b.servicePlanName) },
        Field.BROADCAST to {a, b -> a.typeOfBroadcast.compareTo(b.typeOfBroadcast) },
        Field.PA to { a, b -> a.publicAvailability.compareTo(b.publicAvailability) })

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
            objects[index].servicePlanName = newName
            objects[index].typeOfBroadcast = tb
            objects[index].publicAvailability = publicAvailability
            true

        } else {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun sort(ts: Field) { // сделать map от ts
        if (objects.isNotEmpty()) {
            objects.sortWith(sortMap.getOrDefault(ts) { _, _ -> 0 })
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

    fun setAll(arr: Array<ServicePlan>){
        objects = arr.toMutableList()
    }

}

class Checker(checkRegString: String = "[A-Za-z0-9_]+") { // в конструктор передаётся не проверяемая строка а строка для регекспа!
    private var checkRegex: Regex
    init {
        checkRegex = checkRegString.toRegex()
    }
    fun check(s: String): Boolean {
        return s.matches(checkRegex)
    }
}


class EnterBroadcast () {
    private val StrToBroadcast: Map<String, AllTypesOfBroadcast> = mapOf("REGULAR" to AllTypesOfBroadcast.REGULAR, "HD" to AllTypesOfBroadcast.HD)

    fun precheck(s: Spinner):Boolean {
        return s.selectedItem != null
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun convert(s: Spinner):AllTypesOfBroadcast {
        return StrToBroadcast.getOrDefault((s.selectedItem as String), AllTypesOfBroadcast.REGULAR)
    }

}

class ToPrint {
    private lateinit var cnt: Context
    fun setContext(cnt: Context) : ToPrint{
        this.cnt = cnt
        return this
    }

    val broadcastToString:Map<AllTypesOfBroadcast, String> = mapOf(AllTypesOfBroadcast.HD to "HD",
        AllTypesOfBroadcast.REGULAR to "Обычный")

    @RequiresApi(Build.VERSION_CODES.N)
    fun enumPrint (nameBroadcast: AllTypesOfBroadcast) : String = broadcastToString.getOrDefault(nameBroadcast, "Неизвестный")

    fun print(s: String) = Toast.makeText(cnt, s, Toast.LENGTH_SHORT).show()

    @RequiresApi(Build.VERSION_CODES.N)
    private fun printElement (element: ServicePlan) { //вывод элемента базы
        print("Название тарифного плана: ${element.servicePlanName}")
        print("Тип вещания: ${enumPrint(element.typeOfBroadcast)}")
        print("Общедоступность: ${ if (element.publicAvailability)"YES" else "NO"}")
        print("--------------------------------------------------------------------")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun printList(arr: Array<ServicePlan>) {
        if (arr.isNotEmpty()) {
            ToPrint().print("--------------------------------------------------------------------")
            arr.forEach { printElement(it) }
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
            rb.setText(it.Name)
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


class EnterUI() {
    private lateinit var cnt: MainActivity

    fun enter() {
        val intent = Intent(cnt, EnterActivity::class.java)
        cnt.startActivityForResult(intent, cnt.ENTER_ACTIVITY_CODE)
    }
}

class FindUI(private var field: Field? = null, private val printer: ToPrint = ToPrint()) {

    data class FuncsAndHelps(
        val key: String, val name: String,
        val help: String, val func: (Reader) -> Any?)

    private val enterFuncHelp: Map <Field, FuncsAndHelps> = mapOf(
        Field.NAME to FuncsAndHelps("1", "Название тарифного плана"
            ,"Введите название тарифного плана: ", this::enterName),
        Field.BROADCAST to FuncsAndHelps("2", "Тип вещания",
            "Укажите тип тарифного плана цифрой (1 - REGULAR / 2 - HD): ", this::enterTypeOfBroadcast),
        Field.PA to FuncsAndHelps("3", "Общедоступность",
            "Общедопступный режим вещания (YES / NO): ", this::enterPublicAvailability)
    )

    private val stringToField: Map<String, Field> = mapOf("1" to Field.NAME, "2" to Field.BROADCAST, "3" to Field.PA)

    fun enterField() : Field? {
        printer.print("Выберете поле")
        enterFuncHelp.values.sortedBy { it.key }.forEach{printer.print(it.key + " - " + it.name)}
        printer.print(":")
        val r = Reader()
        if (r.read()){
            field = stringToField[r.get()]

        } else {
            printer.print("Ошибка ввода!")
        }
        if (field == null) printer.print("Неверно выбрано поле!")
        return field
    }

    private fun enterName(r: Reader):String? {
        return if(Checker().check(r.get())) {
            r.get()
        }
        else
        {
            printer.print("Неверное имя тарифного плана!")
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun enterTypeOfBroadcast(r: Reader):AllTypesOfBroadcast?
    {
        return if(ConvertStringToBroadcast().precheck(r.get())){
            ConvertStringToBroadcast().convert(r.get())
        } else
        {
            printer.print("Неверный тип тарифного плана!")
            null
        }
    }

    private fun enterPublicAvailability(r: Reader):Boolean? {
        return if(YesNoToBoolean().precheck(r.get())) {
            YesNoToBoolean().convert(r.get())
        }
        else
        {
            printer.print("Неверный флаг общедоступности!")
            null
        }
    }

    fun enterValue():Any? {
        var value: Any? = null
        val r = Reader()
        enterFuncHelp[field]?.help?.let { printer.print(it) }
        if (!r.read()) {
            printer.print("Ошибка ввода!")
            return value
        }
        value = enterFuncHelp[field]?.func?.let { it(r) }
        return value
    }

    fun set(field: Field) {
        this.field = field
    }
}

class ViewOfDB(private var db: ModelOfDB, private var printer: ToPrint = ToPrint()) {
    private val IntToField: Map<Int, Field> = mapOf(0 to Field.NAME, 1 to Field.BROADCAST, 2 to Field.PA)

    private lateinit var cnt: MainActivity

    fun setContext(cnt: MainActivity) {
        this.cnt = cnt
        printer.setContext(cnt)
    }

    private val commandMap: Map<Commands,() -> Unit > = mapOf(Commands.ADD to  this::add,
        Commands.SORT to this::sort,
        Commands.FIND to this::search,
        Commands.SHOW to this::printAll,
        Commands.EDIT to this::edit,
        Commands.DELETE to  this::delete,
    )

    private fun add() {
        EnterUI().enter()
    }

    fun add(data: Intent) {
        val servicePlan = data.getParcelableExtra<ServicePlan>(cnt.ENTER_SERVICE_PLAN)
        db.add(servicePlan.servicePlanName, servicePlan.typeOfBroadcast, servicePlan.publicAvailability)
    }

    private fun delete() {
        val eVal = FindUI()
        eVal.set(Field.NAME)
        val curVal = eVal.enterValue() ?: return
        if (db.delete(curVal as String)) {
            printer.print("Элемент удалён!")
        } else {
            printer.print("Ошибка удаления!")
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun edit() {
        val eVal = FindUI()
        eVal.set(Field.NAME)
        val curVal = eVal.enterValue() ?: return
        val el = EnterUI().enter() ?: return
        if (db.edit(curVal as String, el.servicePlanName, el.typeOfBroadcast, el.publicAvailability)) {
            printer.print("Элемент изменён!")
        } else {
            printer.print("Ошибка редактирования!")
        }
    }

    private fun sort() {
        val intent = Intent(cnt, EnterFieldActivity::class.java)
        cnt.startActivityForResult(intent, cnt.ENTER_FIELD_SORT_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun sort(data: Intent) {
        val field = IntToField[data.getIntExtra(cnt.ENTER_FIELD,0)]
        if (field != null)
            db.sort(field)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun search() {
        val eVal = FindUI()
        val field = eVal.enterField() ?: return
        val curVal = eVal.enterValue() ?: return
        val arr = db.find(field, curVal)
        printer.printList(arr)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun printAll() {
        val arr = db.giveAll()
        printer.printList(arr)
    }

    fun runCommand (theCommand: Commands) = commandMap[theCommand]?.let { it() }
}

open class App {
    protected val dataBase:ModelOfDB = ModelOfDB()
    val view = ViewOfDB(dataBase)
    protected var theCommand = Commands.UNKNOWN_COMMAND
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
        val array:ArrayList<ServicePlan> = bundle.getParcelableArrayList(cnt.MODEL_DB)!!
        dataBase.setAll(array.toTypedArray())
    }

    fun getArrList():ArrayList<ServicePlan> = dataBase.giveAll().toCollection(ArrayList<ServicePlan>())


    override fun onClick(p0: View?) {
        val entr = menu.selectMenu()
        if (entr != Commands.UNKNOWN_COMMAND)
            view.runCommand(entr)
        else
            ToPrint().setContext(cnt).print("Не выбран пункт меню!")
    }
}


class MainActivity : AppCompatActivity() {
    val ENTER_ACTIVITY_CODE = 10001
    val ENTER_FIELD_SORT_CODE = 10002
    val MODEL_DB = "MODEL_DB"
    val ENTER_SERVICE_PLAN = "ServicePlan"
    val ENTER_FIELD = "enterField"
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
            if (requestCode == ENTER_ACTIVITY_CODE) {
                if (data != null)
                    app.view.add(data)
            }
            else if (requestCode == ENTER_FIELD_SORT_CODE) {
                if (data != null) {
                    app.view.sort(data)
                }
            }

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val arrayList = app.getArrList()
        if (arrayList.isNotEmpty()) {
            outState.putParcelableArrayList(MODEL_DB, arrayList)
        }
    }

}