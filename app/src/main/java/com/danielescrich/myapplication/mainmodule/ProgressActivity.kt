package com.danielescrich.myapplication.mainmodule

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.danielescrich.myapplication.R
import com.danielescrich.myapplication.databinding.ActivityProgressBinding
import com.danielescrich.myapplication.mainmodule.adapter.ProgressCardAdapter
import com.danielescrich.myapplication.mainmodule.ui.dialog.PesoDialogFragment
import com.danielescrich.myapplication.retrofit.RetrofitInstance
import com.danielescrich.myapplication.retrofit.data.Progress
import com.danielescrich.myapplication.room.database.AppDatabase
import com.github.mikephil.charting.data.LineData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProgressBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private var pesoActualGuardado: Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbarAndDrawer()
        setupProfileMenu()
        setupBottomNavigation()
        cargarDatosProgreso()

        binding.fabEditarPeso.setOnClickListener {
            mostrarDialogoComparativoPeso()
        }
    }

    private fun mostrarGrafico(progresoList: List<Progress>) {
        val entries = progresoList.mapIndexed { index, progreso ->
            com.github.mikephil.charting.data.Entry(index.toFloat(), progreso.weight)
        }

        if (entries.isEmpty()) {
            binding.lineChart.clear()
            binding.lineChart.visibility = View.GONE
            binding.tvSinDatos.visibility = View.VISIBLE
            return
        }

        val dataSet = com.github.mikephil.charting.data.LineDataSet(entries, "Peso")
        dataSet.color = getColor(R.color.color_primary)
        dataSet.valueTextColor = getColor(android.R.color.black)
        dataSet.setCircleColor(getColor(R.color.color_primary))
        dataSet.circleRadius = 4f
        dataSet.lineWidth = 2f
        dataSet.valueTextSize = 10f

        val lineData = LineData(dataSet)
        binding.lineChart.data = lineData
        binding.lineChart.description.isEnabled = false

        binding.tvSinDatos.visibility = View.GONE
        binding.lineChart.visibility = View.VISIBLE
        binding.lineChart.invalidate()
    }



    private fun obtenerUsuarioIdActivo(): Int {
        val prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
        return prefs.getInt("usuarioIdActivo", -1)
    }

    private fun cargarDatosProgreso() {
        lifecycleScope.launch {
            val userId = obtenerUsuarioIdActivo()
            try {
                val progresoList = withContext(Dispatchers.IO) {
                    RetrofitInstance.progressService.getProgressByUser(userId)
                }

                val clasesAsistidas = withContext(Dispatchers.IO) {
                    val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "clases_db")
                        .fallbackToDestructiveMigration()
                        .build()
                    db.claseUsuarioDao().getTotalClasesAsistidas(userId)
                }

                val fechaRegistro = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
                    .getString("createdAt", null)

                val textoFecha = fechaRegistro?.let {
                    val formatos = listOf(
                        "yyyy-MM-dd'T'HH:mm:ss",
                        "yyyy-MM-dd'T'HH:mm",
                        "yyyy-MM-dd"
                    )
                    val fecha = formatos.firstNotNullOfOrNull { formato ->
                        try {
                            SimpleDateFormat(formato, Locale.getDefault()).parse(it)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    fecha?.let {
                        val formateador = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
                        formateador.format(it)
                    } ?: "Fecha no disponible"
                } ?: "Fecha no disponible"

                if (progresoList.isNotEmpty()) {
                    val actual = progresoList.last().weight
                    pesoActualGuardado = actual
                    val anterior = if (progresoList.size > 1) progresoList[progresoList.size - 2].weight else actual
                    val diferencia = actual - anterior
                    val altura = 1.75f
                    val imc = actual / (altura * altura)
                    val prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
                    val pesoObjetivo = prefs.getFloat("pesoObjetivo", -1f)

                    val datos = mutableListOf(
                        "Peso objetivo" to if (pesoObjetivo > 0) "$pesoObjetivo kg" else "No disponible",
                        "Peso actual" to "${String.format("%.1f", actual)} kg",
                        "Peso anterior" to "${String.format("%.1f", anterior)} kg",
                        "Diferencia" to "${if (diferencia >= 0) "+" else ""}${String.format("%.1f", diferencia)} kg",
                        "IMC" to String.format("%.1f", imc),
                        "Clases asistidas" to clasesAsistidas.toString(),
                        "Día de inicio" to textoFecha,
                        "Última actualización" to (progresoList.lastOrNull()?.date ?: "N/A")
                    )

                    val adapter = ProgressCardAdapter(
                        datos,
                        onEditarPesoClick = { mostrarDialogoCambiarPeso() },
                        onEditarPesoObjetivoClick = { mostrarDialogoCambiarPesoObjetivo() }
                    )


                    binding.recyclerDatosProgreso.layoutManager = LinearLayoutManager(this@ProgressActivity)
                    binding.recyclerDatosProgreso.adapter = adapter
                    adapter.notifyDataSetChanged()

                    mostrarGrafico(progresoList)

                } else {
                    // No hay datos aún
                    val datosIniciales = listOf(
                        "Peso actual" to "No disponible",
                        "Peso anterior" to "No disponible",
                        "Diferencia" to "N/A",
                        "IMC" to "N/A",
                        "Peso objetivo" to "No disponible",
                        "Clases asistidas" to clasesAsistidas.toString(),
                        "Día de inicio" to textoFecha,
                        "Última actualización" to "N/A"
                    )

                    val adapter = ProgressCardAdapter(
                        datosIniciales,
                        onEditarPesoClick = { mostrarDialogoCambiarPeso() },
                        onEditarPesoObjetivoClick = { mostrarDialogoCambiarPesoObjetivo() }
                    )


                    binding.recyclerDatosProgreso.layoutManager = LinearLayoutManager(this@ProgressActivity)
                    binding.recyclerDatosProgreso.adapter = adapter
                    adapter.notifyDataSetChanged()
                }


            } catch (e: Exception) {
                Toast.makeText(this@ProgressActivity, "Error al cargar progreso", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun mostrarDialogoCambiarPeso() {
        val editText = EditText(this)
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        editText.hint = "Introduce nuevo peso (kg)"

        AlertDialog.Builder(this)
            .setTitle("Cambiar peso actual")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoPeso = editText.text.toString().toFloatOrNull()
                if (nuevoPeso != null) {
                    lifecycleScope.launch {
                        try {
                            val userId = obtenerUsuarioIdActivo()
                            val nuevoProgreso = Progress(
                                userId = userId,
                                weight = nuevoPeso,
                                date = getFechaActual()
                            )

                            withContext(Dispatchers.IO) {
                                RetrofitInstance.progressService.addProgress(nuevoProgreso)
                            }
                            cargarDatosProgreso()
                            Toast.makeText(this@ProgressActivity, "Peso actualizado", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@ProgressActivity, "Error al guardar el peso", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoCambiarPesoObjetivo() {
        val editText = EditText(this)
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        editText.hint = "Introduce tu peso objetivo (kg)"

        AlertDialog.Builder(this)
            .setTitle("Establecer peso objetivo")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoPeso = editText.text.toString().toFloatOrNull()
                if (nuevoPeso != null) {
                    val prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
                    prefs.edit().putFloat("pesoObjetivo", nuevoPeso).apply()
                    cargarDatosProgreso()
                    Toast.makeText(this, "Peso objetivo actualizado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun getFechaActual(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }


    private fun mostrarDialogoComparativoPeso() {
        val prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
        val pesoObjetivo = prefs.getFloat("pesoObjetivo", -1f)

        val pesoActual = pesoActualGuardado

        if (pesoActual != null && pesoObjetivo > 0) {
            val dialog = PesoDialogFragment(pesoActual = pesoActual, pesoObjetivo = pesoObjetivo)
            dialog.show(supportFragmentManager, "PesoDialog")
        } else {
            Toast.makeText(this, "No hay datos de peso suficientes", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupToolbarAndDrawer() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val headerView = binding.navigationView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)
        val ivUserProfile = headerView.findViewById<ImageView>(R.id.ivProfileImage)
        val prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
        tvUserName.text = prefs.getString("usuario_nombre", "Usuario")
        ivUserProfile.setImageResource(R.drawable.ic_user)

        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_preferencias -> startActivity(Intent(this, PreferencesActivity::class.java))
                R.id.nav_terminos -> startActivity(Intent(this, TermsActivity::class.java))
                R.id.nav_sobre -> startActivity(Intent(this, AboutActivity::class.java))
                R.id.nav_contacto -> startActivity(Intent(this, ContactActivity::class.java))
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupProfileMenu() {
        binding.ivProfile.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menuInflater.inflate(R.menu.menu_profile, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_profile -> {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        true
                    }
                    R.id.menu_logout -> {
                        getSharedPreferences("gymmodel_prefs", MODE_PRIVATE).edit().clear().apply()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_classes -> {
                    startActivity(Intent(this, ClassesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_progress -> true
                R.id.nav_ranking -> {
                    startActivity(Intent(this, RankingActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_ia -> {
                    startActivity(Intent(this, IAHomeActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
        binding.bottomNavigation.menu.findItem(R.id.nav_progress).isChecked = true
    }
}
