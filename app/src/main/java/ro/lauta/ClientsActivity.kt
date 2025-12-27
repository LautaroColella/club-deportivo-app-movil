package ro.lauta

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ClientsActivity : AppCompatActivity() {
    data class ClientItem(
        val nroSocio: Int,
        var name: String,
        var lastname: String,
        var age: Int,
        var dni: Int,
        var cuotaPayDate: String,
        var joinDate: String,
        var isSocio: Boolean,
        val activities: MutableList<String> = mutableListOf()
    )

    private val clients = mutableListOf<ClientItem>()
    private var nextId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_clients)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvEmpty = findViewById<TextView>(R.id.tvEmptyClients)

        val clientsContainer = findViewById<LinearLayout>(R.id.clientsContainer)
        val btnVenceHoy = findViewById<Button>(R.id.btnVenceHoy)

        val btnCreate = findViewById<Button>(R.id.btnCreate)
        btnCreate.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.activity_create_client, null)

            val etName = view.findViewById<EditText>(R.id.etName)
            val etLastname = view.findViewById<EditText>(R.id.etLastName)
            val etAge = view.findViewById<EditText>(R.id.etAge)
            val etDni = view.findViewById<EditText>(R.id.etDni)
            val cbSocio = view.findViewById<CheckBox>(R.id.cbSocio)

            val btnClose = view.findViewById<ImageButton>(R.id.btnCloseDialog)
            val btnCancel = view.findViewById<Button>(R.id.btnCancelDialog)
            val btnSave = view.findViewById<Button>(R.id.btnSaveDialog)

            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(view)
                .create()

            btnClose.setOnClickListener { dialog.dismiss() }
            btnCancel.setOnClickListener { dialog.dismiss() }

            btnSave.setOnClickListener {
                if (!validateClientFields(
                        etName, etLastname, etAge, etDni
                    )) return@setOnClickListener

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val today = Calendar.getInstance().time
                val todayStr = dateFormat.format(today)

                val newClient = ClientItem(
                    nroSocio = nextId++,
                    name = etName.text.toString().trim(),
                    lastname = etLastname.text.toString().trim(),
                    age = etAge.text.toString().trim().toInt(),
                    dni = etDni.text.toString().trim().toInt(),
                    cuotaPayDate = todayStr,
                    joinDate = todayStr,
                    isSocio = cbSocio.isChecked,
                    activities = mutableListOf()
                )

                clients.add(newClient)
                tvEmpty.visibility = View.GONE

                val row = layoutInflater.inflate(R.layout.activity_item_client, null)

                val clientId = View.generateViewId()
                row.id = clientId

                val tvName = row.findViewById<TextView>(R.id.tvName)
                val btnEdit = row.findViewById<Button>(R.id.btnEdit)
                val btnDelete = row.findViewById<Button>(R.id.btnDelete)

                tvName.text = "${newClient.name} ${newClient.lastname}"

                tvName.setOnClickListener {
                    showClientInfoModal(newClient)
                }

                btnEdit.setOnClickListener {
                    showEditClientModal(newClient)
                }

                btnDelete.setOnClickListener {
                    clients.remove(newClient)
                    clientsContainer.removeView(row)
                    if (clients.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                    }
                    Toast.makeText(this, "Cliente ${newClient.name} ${newClient.lastname} eliminado", Toast.LENGTH_SHORT).show()
                }

                clientsContainer.addView(row)

                dialog.dismiss()
            }

            dialog.show()
        }

        btnVenceHoy.setOnClickListener {
            clientsContainer.removeAllViews()

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val today = Calendar.getInstance()

            val dueClients = clients.filter { client ->
                client.isSocio && run {
                    val lastPayment = dateFormat.parse(client.cuotaPayDate) ?: return@run false
                    val limit = Calendar.getInstance().apply { time = lastPayment; add(Calendar.MONTH, 1) }
                    limit.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                            limit.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                            limit.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
                }
            }

            if (dueClients.isEmpty()) {
                val tvEmpty = findViewById<TextView>(R.id.tvEmptyClients)
                tvEmpty.visibility = View.VISIBLE
            } else {
                val tvEmpty = findViewById<TextView>(R.id.tvEmptyClients)
                tvEmpty.visibility = View.GONE

                dueClients.forEach { client ->
                    val row = layoutInflater.inflate(R.layout.activity_item_client, null)
                    val tvName = row.findViewById<TextView>(R.id.tvName)
                    val btnEdit = row.findViewById<Button>(R.id.btnEdit)
                    val btnDelete = row.findViewById<Button>(R.id.btnDelete)

                    tvName.text = "${client.name} ${client.lastname}"
                    tvName.setOnClickListener { showClientInfoModal(client) }

                    btnEdit.setOnClickListener { showEditClientModal(client) }

                    btnDelete.setOnClickListener {
                        clients.remove(client)
                        clientsContainer.removeView(row)
                        if (clients.isEmpty()) findViewById<TextView>(R.id.tvEmptyClients).visibility = View.VISIBLE
                    }

                    clientsContainer.addView(row)
                }
            }
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.item_client

        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.item_client -> {
                    true
                }
                R.id.item_activities -> {
                    val intent = Intent(this, ManageActivitiesActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.item_calendar -> {
                    val intent = Intent(this, CalendarActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.item_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
    fun highlight(view: EditText, valid: Boolean) {
        if (valid) {
            view.backgroundTintList = getColorStateList(android.R.color.darker_gray)
        } else {
            view.backgroundTintList = getColorStateList(android.R.color.holo_red_light)
        }
    }

    fun showClientInfoModal(client: ClientItem) {
        val view = layoutInflater.inflate(R.layout.activity_info_client, null)

        val tvNroSocio = view.findViewById<TextView>(R.id.tvNroSocio)
        val tvName = view.findViewById<TextView>(R.id.tvViewName)
        val tvAge = view.findViewById<TextView>(R.id.tvAge)
        val tvDni = view.findViewById<TextView>(R.id.tvDni)
        val tvCuotaPayDate = view.findViewById<TextView>(R.id.tvCuotaPayDate)
        val tvJoinDate = view.findViewById<TextView>(R.id.tvJoinDate)
        val tvActivitiesTitle = view.findViewById<TextView>(R.id.tvActivitiesTitle)
        val activitiesContainer = view.findViewById<LinearLayout>(R.id.activitiesContainer)
        val payCuotaContainer = view.findViewById<LinearLayout>(R.id.cuotaPayContainer)
        val carnetContainer = view.findViewById<LinearLayout>(R.id.carnetContainer)

        tvNroSocio.text = "Nro de socio: ${client.nroSocio}"
        tvName.text = "${client.name} ${client.lastname}"
        tvAge.text = "Edad: ${client.age}"
        tvDni.text = "DNI: ${client.dni}"
        tvJoinDate.text = "Fecha de alta: ${client.joinDate} - ${if (client.isSocio) "Socio" else "No Socio"}"

        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val lastPaymentDate: Date? = formatter.parse(client.cuotaPayDate)
        val now = Calendar.getInstance()
        val limitDate = Calendar.getInstance().apply {
            time = lastPaymentDate!!
            if (client.isSocio) {
                add(Calendar.MONTH, 1)
            } else {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("Cerrar", null)
            .create()

        val payCuotaBtn = MaterialButton(this).apply {
            text = "Pagar cuota"
            setOnClickListener {
                dialog.dismiss()
                showPayCuotaModal(client)
            }
        }
        val vencerCuotaBtn = MaterialButton(this).apply {
            text = "DEBUG: Vencer cuota"
            setOnClickListener {
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.time = formatter.parse(client.cuotaPayDate)!!

                if (client.isSocio) {
                    calendar.add(Calendar.MONTH, -1)
                } else {
                    calendar.add(Calendar.DAY_OF_MONTH, -7)
                }

                client.cuotaPayDate = formatter.format(calendar.time)
                Toast.makeText(context, "Cuota vencida para ${client.name}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        if (now.before(limitDate)) {
            tvCuotaPayDate.text = "Último pago: ${client.cuotaPayDate}"
            payCuotaContainer.addView(vencerCuotaBtn)
        } else {
            tvCuotaPayDate.text = "Último pago: ${client.cuotaPayDate} - Vencido"
            payCuotaContainer.addView(payCuotaBtn)
        }

        tvActivitiesTitle.text = "Actividades (${client.activities.size})"

        val verListadoBtn = MaterialButton(this).apply {
            text = "Ver listado"
            setOnClickListener {
                dialog.dismiss()
                showListadoActividadesModal(client)
            }
        }

        val imprimirCarnetBtn = MaterialButton(this).apply {
            text = "Imprimir carnet"
            setOnClickListener {
                Toast.makeText(this@ClientsActivity, "Carnet impreso", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        activitiesContainer.addView(verListadoBtn)
        if (client.isSocio) carnetContainer.addView(imprimirCarnetBtn)

        dialog.show()
    }

    fun showListadoActividadesModal(client: ClientItem) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dpToPx(), 24.dpToPx(), 24.dpToPx(), 24.dpToPx())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val title = TextView(this).apply {
            text = "Listado de actividades"
            textSize = 20f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val search = EditText(this).apply {
            hint = "Buscar actividad"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 12.dpToPx() }
        }

        val addBtn = MaterialButton(this).apply {
            text = "Agregar nueva"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 12.dpToPx() }
        }

        val listScroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                300.dpToPx()
            ).apply { topMargin = 12.dpToPx() }
        }
        val listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        listScroll.addView(listContainer)

        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 16.dpToPx() }
        }
        val backBtn = MaterialButton(this).apply { text = "Volver" }
        val saveBtn = MaterialButton(this).apply { text = "Guardar" }
        bottomRow.addView(backBtn)
        bottomRow.addView(saveBtn)

        root.addView(title)
        root.addView(addBtn)
        root.addView(search)
        root.addView(listScroll)
        root.addView(bottomRow)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(root)
            .create()

        val activities = client.activities

        fun refreshDisplayedList(filter: String? = null) {
            val filtered = activities
                .sortedWith(String.CASE_INSENSITIVE_ORDER)
                .filter { if (filter.isNullOrEmpty()) true else it.contains(filter, ignoreCase = true) }

            listContainer.removeAllViews()
            filtered.forEach { pname ->
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setPadding(0, 10.dpToPx(), 0, 10.dpToPx())
                }

                val tv = TextView(this).apply {
                    text = pname
                    textSize = 16f
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val removeBtn = MaterialButton(this).apply {
                    text = "Eliminar"
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    setOnClickListener {
                        activities.remove(pname)
                        refreshDisplayedList(search.text.toString())
                    }
                }

                row.addView(tv)
                row.addView(removeBtn)
                listContainer.addView(row)
            }
        }

        refreshDisplayedList()

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                refreshDisplayedList(s?.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        addBtn.setOnClickListener {
            val input = EditText(this).apply { hint = "Nombre de la actividad" }
            AlertDialog.Builder(this)
                .setTitle("Agregar actividad")
                .setView(input)
                .setPositiveButton("Agregar") { _, _ ->
                    val newName = input.text.toString().trim()
                    if (newName.isNotEmpty()) {
                        activities.add(newName)
                        activities.sortWith(String.CASE_INSENSITIVE_ORDER)
                        refreshDisplayedList(search.text.toString())
                    } else {
                        Toast.makeText(this, "Nombre vacío", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        backBtn.setOnClickListener {
            dialog.dismiss()
            showClientInfoModal(client)
        }

        saveBtn.setOnClickListener {
            Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showEditClientModal(client: ClientItem) {
        val view = layoutInflater.inflate(R.layout.activity_create_client, null)

        val etName = view.findViewById<EditText>(R.id.etName)
        val etLastName = view.findViewById<EditText>(R.id.etLastName)
        val etAge = view.findViewById<EditText>(R.id.etAge)
        val etDni = view.findViewById<EditText>(R.id.etDni)
        val cbSocio = view.findViewById<CheckBox>(R.id.cbSocio)

        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseDialog)
        val btnCancel = view.findViewById<Button>(R.id.btnCancelDialog)
        val btnSave = view.findViewById<Button>(R.id.btnSaveDialog)

        etName.setText(client.name)
        etLastName.setText(client.lastname)
        etAge.setText(client.age.toString())
        etDni.setText(client.dni.toString())
        cbSocio.isChecked = client.isSocio

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(view)
            .create()

        btnClose.setOnClickListener { dialog.dismiss() }
        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            if (!validateClientFields(
                    etName, etLastName, etAge, etDni
                )) return@setOnClickListener

            client.name = etName.text.toString().trim()
            client.lastname = etLastName.text.toString().trim()
            client.age = etAge.text.toString().trim().toInt()
            client.dni = etDni.text.toString().trim().toInt()
            client.isSocio = cbSocio.isChecked

            refreshClientsList()
            dialog.dismiss()
        }

        dialog.show()
    }

    fun refreshClientsList() {
        val container = findViewById<LinearLayout>(R.id.clientsContainer)
        container.removeAllViews()

        clients.forEach { client ->
            val row = layoutInflater.inflate(R.layout.activity_item_client, null)

            val tvName = row.findViewById<TextView>(R.id.tvName)
            val btnEdit = row.findViewById<Button>(R.id.btnEdit)
            val btnDelete = row.findViewById<Button>(R.id.btnDelete)

            tvName.text = "${client.name} ${client.lastname}"

            tvName.setOnClickListener { showClientInfoModal(client) }
            btnEdit.setOnClickListener { showEditClientModal(client) }
            btnDelete.setOnClickListener {
                clients.remove(client)
                refreshClientsList()
            }

            container.addView(row)
        }
    }

    fun showPayCuotaModal(client: ClientItem) {
        val view = layoutInflater.inflate(R.layout.activity_pay_cuota, null)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvInfo = view.findViewById<TextView>(R.id.tvInfo)
        val btnPay = view.findViewById<MaterialButton>(R.id.btnPay)
        val btnClose = view.findViewById<MaterialButton>(R.id.btnClose)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val todayStr = formatter.format(Date())

        if (client.isSocio) {
            tvTitle.text = "Pagar cuota mensual"

            tvInfo.text = "Fecha de pago: $todayStr\nCuota mensual a pagar: ${(1200..1500).random()}"

            btnPay.setOnClickListener {
                client.cuotaPayDate = todayStr
                Toast.makeText(this, "Cuota mensual pagada", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        } else {
            tvTitle.text = "Pagar cuota diaria"

            val fakeAmount = (200..500).random()

            tvInfo.text = "Fecha: $todayStr\nMonto a pagar: $$fakeAmount"

            btnPay.setOnClickListener {
                client.cuotaPayDate = todayStr
                Toast.makeText(this, "Cuota diaria pagada ($$fakeAmount)", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }


    private fun validateClientFields(
        etName: EditText,
        etLastName: EditText,
        etAge: EditText,
        etDni: EditText,
    ): Boolean {
        val name = etName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val age = etAge.text.toString().trim()
        val dni = etDni.text.toString().trim()

        if (name.isEmpty()) {
            highlight(etName, false)
            toast("El nombre es obligatorio")
            return false
        }
        if (name.length > 20) {
            highlight(etName, false)
            toast("El nombre no puede superar 20 caracteres")
            return false
        } else highlight(etName, true)

        if (lastName.isEmpty()) {
            highlight(etLastName, false)
            toast("El apellido es obligatorio")
            return false
        }
        if (lastName.length > 20) {
            highlight(etLastName, false)
            toast("El apellido no puede superar 20 caracteres")
            return false
        } else highlight(etLastName, true)

        val ageInt = age.toIntOrNull()
        if (age.isEmpty()) {
            highlight(etAge, false)
            toast("La edad es obligatoria")
            return false
        }
        if (ageInt == null || ageInt < 1 || ageInt > 120) {
            highlight(etAge, false)
            toast("La edad debe estar entre 1 y 120 años")
            return false
        } else highlight(etAge, true)

        val dniInt = dni.toIntOrNull()
        if (dni.isEmpty()) {
            highlight(etDni, false)
            toast("El DNI es obligatorio")
            return false
        }
        if (dniInt == null || dniInt < 1 || dniInt > 99999999) {
            highlight(etDni, false)
            toast("El DNI debe ser un número válido (1 a 99.999.999)")
            return false
        } else highlight(etDni, true)

        return true
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}