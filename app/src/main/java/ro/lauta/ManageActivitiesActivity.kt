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
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialSplitButton

class ManageActivitiesActivity : AppCompatActivity() {
    data class ActivityItem(
        val id: Int,
        var name: String,
        var description: String,
        var teacher: String,
        var day: String,
        var time: String,
        var allowSocio: Boolean,
        var allowNoSocio: Boolean,
        var maxInscriptions: Int,
        val participants: MutableList<String> = mutableListOf()
    )

    private val activities = mutableListOf<ActivityItem>()
    private var nextId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_activities)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvEmpty = findViewById<TextView>(R.id.tvEmptyActivities)

        val splitButton = findViewById<MaterialSplitButton>(R.id.btnSort)
        val trailingButton = findViewById<Button>(R.id.expand_more_or_less_filled)
        val leadingButton = splitButton.getChildAt(0) as Button

        trailingButton.setOnClickListener {
            val popupMenu = PopupMenu(this, trailingButton)
            popupMenu.menuInflater.inflate(R.menu.sortby_dropdown_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                leadingButton.text = menuItem.title
                true
            }

            popupMenu.show()
        }

        val btnCreate = findViewById<Button>(R.id.btnCreate)
        btnCreate.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.activity_create_activity, null)

            val etName = view.findViewById<EditText>(R.id.etName)
            val etDesc = view.findViewById<EditText>(R.id.etDesc)
            val etTeacher = view.findViewById<EditText>(R.id.etTeacher)
            val etMax = view.findViewById<EditText>(R.id.etMax)
            val etTime = view.findViewById<EditText>(R.id.etTime)
            val etDay = view.findViewById<EditText>(R.id.etDay)
            val cbSocios = view.findViewById<CheckBox>(R.id.cbSocios)
            val cbNoSocios = view.findViewById<CheckBox>(R.id.cbNoSocios)

            val btnClose = view.findViewById<ImageButton>(R.id.btnCloseDialog)
            val btnCancel = view.findViewById<Button>(R.id.btnCancelDialog)
            val btnSave = view.findViewById<Button>(R.id.btnSaveDialog)

            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(view)
                .create()

            btnClose.setOnClickListener { dialog.dismiss() }
            btnCancel.setOnClickListener { dialog.dismiss() }

            val activitiesContainer = findViewById<LinearLayout>(R.id.activitiesContainer)

            btnSave.setOnClickListener {
                val allowSocios = cbSocios.isChecked
                val allowNoSocio = cbNoSocios.isChecked

                if (!validateActivityFields(
                        etName, etDesc, etTeacher, etMax, etTime, etDay,
                        allowSocios, allowNoSocio
                    )) return@setOnClickListener

                val newActivity = ActivityItem(
                    id = nextId++,
                    name = etName.text.toString().trim(),
                    description = etDesc.text.toString().trim(),
                    teacher = etTeacher.text.toString().trim(),
                    day = etDay.text.toString().trim(),
                    time = etTime.text.toString().trim(),
                    allowSocio = allowSocios,
                    allowNoSocio = allowNoSocio,
                    maxInscriptions = etMax.text.toString().trim().toInt(),
                    participants = mutableListOf()
                )

                activities.add(newActivity)
                tvEmpty.visibility = View.GONE

                val row = layoutInflater.inflate(R.layout.activity_item_activity, null)

                val activityId = View.generateViewId()
                row.id = activityId

                val tvName = row.findViewById<TextView>(R.id.tvName)
                val btnEdit = row.findViewById<Button>(R.id.btnEdit)
                val btnDelete = row.findViewById<Button>(R.id.btnDelete)

                tvName.text = newActivity.name

                tvName.setOnClickListener {
                    showActivityInfoModal(newActivity)
                }

                btnEdit.setOnClickListener {
                    showEditActivityModal(newActivity)
                }

                btnDelete.setOnClickListener {
                    activities.remove(newActivity)
                    activitiesContainer.removeView(row)
                    if (activities.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                    }
                    Toast.makeText(this, "Actividad $newActivity.name eliminada", Toast.LENGTH_SHORT).show()
                }

                activitiesContainer.addView(row)

                dialog.dismiss()
            }

            dialog.show()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.item_activities

        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.item_activities -> {
                    true
                }
                R.id.item_calendar -> {
                    val intent = Intent(this, CalendarActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.item_client -> {
                    val intent = Intent(this, ClientsActivity::class.java)
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

    fun showActivityInfoModal(activity: ActivityItem) {
        val view = layoutInflater.inflate(R.layout.activity_info_activity, null)

        val tvName = view.findViewById<TextView>(R.id.tvViewName)
        val tvDesc = view.findViewById<TextView>(R.id.tvViewDescription)
        val tvTeacher = view.findViewById<TextView>(R.id.tvViewTeacher)
        val tvDayTime = view.findViewById<TextView>(R.id.tvViewDayTime)
        val tvSocio = view.findViewById<TextView>(R.id.tvViewSocio)
        val tvParticipantsTitle = view.findViewById<TextView>(R.id.tvParticipantsTitle)
        val participantsContainer = view.findViewById<LinearLayout>(R.id.participantsContainer)

        tvName.text = activity.name
        tvDesc.text = activity.description
        tvTeacher.text = activity.teacher
        tvDayTime.text = "${activity.day} ${activity.time}"

        val socioText = when {
            activity.allowSocio && activity.allowNoSocio -> "Acepta: Socio, No Socio"
            activity.allowSocio -> "Acepta: Socio"
            activity.allowNoSocio -> "Acepta: No Socio"
            else -> "No acepta participantes"
        }
        tvSocio.text = socioText

        tvParticipantsTitle.text =
            "Participantes (${activity.participants.size}/${activity.maxInscriptions})"

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("Cerrar", null)
            .create()

        val verListadoBtn = MaterialButton(this).apply {
            text = "Ver listado"
            setOnClickListener {
                dialog.dismiss()
                showListadoParticipantesModal(activity)
            }
        }
        participantsContainer.addView(verListadoBtn)

        dialog.show()
    }

    fun showListadoParticipantesModal(activity: ActivityItem) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dpToPx(), 24.dpToPx(), 24.dpToPx(), 24.dpToPx())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val title = TextView(this).apply {
            text = "Listado de participantes"
            textSize = 20f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val search = EditText(this).apply {
            hint = "Buscar participante"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 12.dpToPx() }
        }

        val addBtn = MaterialButton(this).apply {
            text = "Agregar nuevo"
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

        val participants = activity.participants

        fun refreshDisplayedList(filter: String? = null) {
            val filtered = participants
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
                        participants.remove(pname)
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
            val input = EditText(this).apply { hint = "Nombre del participante" }
            AlertDialog.Builder(this)
                .setTitle("Agregar participante")
                .setView(input)
                .setPositiveButton("Agregar") { _, _ ->
                    val newName = input.text.toString().trim()
                    if (newName.isNotEmpty()) {
                        participants.add(newName)
                        participants.sortWith(String.CASE_INSENSITIVE_ORDER)
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
            showActivityInfoModal(activity)
        }

        saveBtn.setOnClickListener {
            Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showEditActivityModal(activity: ActivityItem) {
        val view = layoutInflater.inflate(R.layout.activity_create_activity, null)

        val etName = view.findViewById<EditText>(R.id.etName)
        val etDesc = view.findViewById<EditText>(R.id.etDesc)
        val etTeacher = view.findViewById<EditText>(R.id.etTeacher)
        val etMax = view.findViewById<EditText>(R.id.etMax)
        val etTime = view.findViewById<EditText>(R.id.etTime)
        val etDay = view.findViewById<EditText>(R.id.etDay)
        val cbSocios = view.findViewById<CheckBox>(R.id.cbSocios)
        val cbNoSocios = view.findViewById<CheckBox>(R.id.cbNoSocios)

        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseDialog)
        val btnCancel = view.findViewById<Button>(R.id.btnCancelDialog)
        val btnSave = view.findViewById<Button>(R.id.btnSaveDialog)

        etName.setText(activity.name)
        etDesc.setText(activity.description)
        etTeacher.setText(activity.teacher)
        etMax.setText(activity.maxInscriptions.toString())
        etTime.setText(activity.time)
        etDay.setText(activity.day)
        cbSocios.isChecked = activity.allowSocio
        cbNoSocios.isChecked = activity.allowNoSocio

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(view)
            .create()

        btnClose.setOnClickListener { dialog.dismiss() }
        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val allowSocios = cbSocios.isChecked
            val allowNoSocios = cbNoSocios.isChecked

            if (!validateActivityFields(
                    etName, etDesc, etTeacher, etMax, etTime, etDay,
                    allowSocios, allowNoSocios
                )) return@setOnClickListener

            activity.name = etName.text.toString().trim()
            activity.description = etDesc.text.toString().trim()
            activity.teacher = etTeacher.text.toString().trim()
            activity.maxInscriptions = etMax.text.toString().trim().toInt()
            activity.time = etTime.text.toString().trim()
            activity.day = etDay.text.toString().trim()
            activity.allowSocio = allowSocios
            activity.allowNoSocio = allowNoSocios

            refreshActivitiesList()
            dialog.dismiss()
        }

        dialog.show()
    }

    fun refreshActivitiesList() {
        val container = findViewById<LinearLayout>(R.id.activitiesContainer)
        container.removeAllViews()

        activities.forEach { activity ->
            val row = layoutInflater.inflate(R.layout.activity_item_activity, null)

            val tvName = row.findViewById<TextView>(R.id.tvName)
            val btnEdit = row.findViewById<Button>(R.id.btnEdit)
            val btnDelete = row.findViewById<Button>(R.id.btnDelete)

            tvName.text = activity.name

            tvName.setOnClickListener { showActivityInfoModal(activity) }
            btnEdit.setOnClickListener { showEditActivityModal(activity) }
            btnDelete.setOnClickListener {
                activities.remove(activity)
                refreshActivitiesList()
            }

            container.addView(row)
        }
    }

    private fun validateActivityFields(
        etName: EditText,
        etDesc: EditText,
        etTeacher: EditText,
        etMax: EditText,
        etTime: EditText,
        etDay: EditText,
        allowSocios: Boolean,
        allowNoSocios: Boolean
    ): Boolean {

        val name = etName.text.toString().trim()
        val desc = etDesc.text.toString().trim()
        val teacher = etTeacher.text.toString().trim()
        val max = etMax.text.toString().trim()
        val time = etTime.text.toString().trim()
        val day = etDay.text.toString().trim()

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

        if (desc.isEmpty()) {
            highlight(etDesc, false)
            toast("La descripción es obligatoria")
            return false
        }
        if (desc.length > 100) {
            highlight(etDesc, false)
            toast("La descripción no puede superar 100 caracteres")
            return false
        } else highlight(etDesc, true)

        if (teacher.isEmpty()) {
            highlight(etTeacher, false)
            toast("El profesor es obligatorio")
            return false
        }
        if (teacher.length > 20) {
            highlight(etTeacher, false)
            toast("El profesor no puede superar 20 caracteres")
            return false
        } else highlight(etTeacher, true)

        val maxInt = max.toIntOrNull()
        if (max.isEmpty()) {
            highlight(etMax, false)
            toast("El límite de inscripciones es obligatorio")
            return false
        }
        if (maxInt == null || maxInt < 1 || maxInt > 200) {
            highlight(etMax, false)
            toast("El límite de inscripciones debe ser mínimo 1 y máximo 200")
            return false
        } else highlight(etMax, true)

        val timeRegex = Regex("^([01]\\d|2[0-3]):([0-5]\\d)\$")
        if (time.isEmpty()) {
            highlight(etTime, false)
            toast("La hora es obligatoria")
            return false
        }
        if (!timeRegex.matches(time)) {
            highlight(etTime, false)
            toast("La hora debe tener formato HH:mm (ej: 07:30, 23:40)")
            return false
        } else highlight(etTime, true)

        val allowedDays = listOf(
            "Lunes", "Martes", "Miércoles", "Miercoles",
            "Jueves", "Viernes", "Sábado", "Sabado", "Domingo"
        )
        if (day.isEmpty()) {
            highlight(etDay, false)
            toast("El día es obligatorio")
            return false
        }
        if (!allowedDays.any { it.equals(day, ignoreCase = true) }) {
            highlight(etDay, false)
            toast("El día debe ser: Lunes, Martes, Miércoles, Jueves, Viernes, Sábado, Domingo")
            return false
        } else highlight(etDay, true)

        if (!allowSocios && !allowNoSocios) {
            toast("Debes permitir socios, no socios, o ambos")
            return false
        }

        return true
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}