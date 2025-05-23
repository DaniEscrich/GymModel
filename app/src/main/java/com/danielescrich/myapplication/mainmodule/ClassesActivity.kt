package com.danielescrich.myapplication.mainmodule

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.danielescrich.myapplication.R
import com.danielescrich.myapplication.databinding.ActivityClassesBinding
import com.danielescrich.myapplication.mainmodule.adapter.ClaseAdapter
import com.danielescrich.myapplication.mainmodule.adapter.OnClickListener
import com.danielescrich.myapplication.room.dao.ClaseUsuarioDao
import com.danielescrich.myapplication.room.dao.UsuarioDao
import com.danielescrich.myapplication.room.database.AppDatabase
import com.danielescrich.myapplication.room.entity.ClaseEntity
import com.danielescrich.myapplication.room.entity.ClaseUsuarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ClassesActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityClassesBinding
    private lateinit var claseAdapter: ClaseAdapter
    private lateinit var claseDao: com.danielescrich.myapplication.room.dao.ClaseDao
    private lateinit var claseUsuarioDao: ClaseUsuarioDao
    private lateinit var usuarioDao: UsuarioDao

    private var semanaActual: Int = 0
    private var diaSeleccionado: String = "Lun"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Calcular semana actual
        calcularSemanaActual()

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "clases_db"
        ).fallbackToDestructiveMigration().build()

        claseDao = db.claseDao()
        claseUsuarioDao = db.claseUsuarioDao()
        usuarioDao = db.usuarioDao()

        claseAdapter = ClaseAdapter(
            listener = this,
            semanaActual = semanaActual,
            claseUsuarioDao = claseUsuarioDao,
            usuarioDao = usuarioDao
        )

        binding.rvClases.layoutManager = LinearLayoutManager(this)
        binding.rvClases.adapter = claseAdapter

        lifecycleScope.launch {
            val currentList = withContext(Dispatchers.IO) { claseDao.getAllClasesDirect() }
            val yaHayParaEstaSemana = currentList.any { it.semana == semanaActual }

            if (!yaHayParaEstaSemana) {
                withContext(Dispatchers.IO) { insertarClasesParaTodaLaSemana() }
            }

            actualizarLista()
        }

        configurarBotonesDia()
        actualizarEstiloDias()


        binding.btnPrevious.setOnClickListener {
            Toast.makeText(this, "No se puede ver semanas pasadas", Toast.LENGTH_SHORT).show()
        }

        binding.btnNext.setOnClickListener {
            Toast.makeText(this, "Las siguientes semanas aún no están disponibles", Toast.LENGTH_SHORT).show()
        }

        actualizarTextoSemana()

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_classes -> true
                R.id.nav_progress -> {
                    startActivity(Intent(this, ProgressActivity::class.java))
                    finish()
                    true
                }
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

        binding.bottomNavigation.selectedItemId = R.id.nav_classes
    }

    private fun calcularSemanaActual() {
        val hoy = Calendar.getInstance(Locale("es", "ES")).apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val inicioDelAnio = Calendar.getInstance(Locale("es", "ES")).apply {
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffMillis = hoy.timeInMillis - inicioDelAnio.timeInMillis
        val millisEnUnaSemana = 7 * 24 * 60 * 60 * 1000L
        semanaActual = (diffMillis / millisEnUnaSemana).toInt()

        val diaHoy = hoy.get(Calendar.DAY_OF_WEEK)
        val mapaDia = mapOf(
            Calendar.MONDAY to "Lun", Calendar.TUESDAY to "Mar", Calendar.WEDNESDAY to "Mie",
            Calendar.THURSDAY to "Jue", Calendar.FRIDAY to "Vie",
            Calendar.SATURDAY to "Sab", Calendar.SUNDAY to "Dom"
        )
        diaSeleccionado = mapaDia[diaHoy] ?: "Lun"
    }

    private fun actualizarTextoSemana() {
        val hoy = Calendar.getInstance(Locale("es", "ES"))
        hoy.firstDayOfWeek = Calendar.MONDAY
        hoy.set(Calendar.HOUR_OF_DAY, 0)
        hoy.set(Calendar.MINUTE, 0)
        hoy.set(Calendar.SECOND, 0)
        hoy.set(Calendar.MILLISECOND, 0)
        hoy.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val inicioSemana = hoy.time
        hoy.add(Calendar.DAY_OF_WEEK, 6)
        val finSemana = hoy.time

        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
        binding.tvWeekTitle.text = "Semana del ${formatter.format(inicioSemana)} al ${formatter.format(finSemana)}"
    }

    private fun actualizarLista() {
        actualizarTextoSemana()
        claseDao.getClasesPorDiaYSemana(diaSeleccionado, semanaActual).observe(this) { clasesFiltradas ->
            claseAdapter.submitList(clasesFiltradas)
        }
    }

    private suspend fun insertarClasesParaTodaLaSemana() {
        val dias = listOf("Lun", "Mar", "Mie", "Jue", "Vie")
        val horas = listOf("09:00", "10:00", "11:00", "12:00", "13:00", "17:00", "18:00", "19:00", "20:00")
        val clases = mutableListOf<ClaseEntity>()

        for (dia in dias) {
            for (hora in horas) {
                val titulo = if (hora == "20:00") listOf("Yoga", "Pilates").random()
                else listOf("Fitboxing", "Spinning", "HIIT", "Ciclismo", "Funcional").random()
                val descripcion = "Clase de $titulo para mejorar tu estado físico"
                clases.add(
                    ClaseEntity(
                        titulo = titulo,
                        diaSemana = dia,
                        hora = hora,
                        descripcion = descripcion,
                        semana = semanaActual
                    )
                )
            }
        }
        claseDao.addClases(clases)
    }

    override fun onClick(clase: ClaseEntity, position: Int) {}

    override fun onApuntarse(clase: ClaseEntity, position: Int) {
        val prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
        val usuarioId = prefs.getInt("usuarioIdActivo", -1)
        if (usuarioId == -1) return

        lifecycleScope.launch(Dispatchers.IO) {
            val yaApuntado = claseUsuarioDao.getRelacion(clase.id, usuarioId.toString(), semanaActual)
            if (yaApuntado != null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ClassesActivity, "Ya estás apuntado a esta clase", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val clasesDelDia = claseDao.getClasesPorDiaYSemanaDirect(clase.diaSemana, semanaActual)
            val claseYaReservada = clasesDelDia.any {
                claseUsuarioDao.getRelacion(it.id, usuarioId.toString(), semanaActual) != null
            }

            if (claseYaReservada) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ClassesActivity, "Solo puedes apuntarte a una clase por día", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            if (clase.apuntados < clase.maxUsuarios) {
                val nuevaClase = clase.copy(apuntados = clase.apuntados + 1)
                claseUsuarioDao.apuntarUsuario(ClaseUsuarioEntity(clase.id, usuarioId, semanaActual))
                claseDao.updateClase(nuevaClase)
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ClassesActivity, "Clase completa", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onLongClick(clase: ClaseEntity, position: Int) {
        val prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
        val usuarioId = prefs.getInt("usuarioIdActivo", -1)
        if (usuarioId == -1) return

        lifecycleScope.launch(Dispatchers.IO) {
            val relacion = claseUsuarioDao.getRelacion(clase.id, usuarioId.toString(), semanaActual)
            if (relacion != null) {
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(this@ClassesActivity)
                        .setTitle("Salir de la clase")
                        .setMessage("¿Quieres dejar de estar apuntado?")
                        .setPositiveButton("Sí") { _, _ ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                claseDao.updateClase(clase.copy(apuntados = clase.apuntados - 1))
                                claseUsuarioDao.desapuntarUsuario(relacion)
                            }
                        }
                        .setNegativeButton("No", null)
                        .show()
                }
            }
        }
    }
    private fun configurarBotonesDia() {
        binding.llDays.children.forEach { view ->
            (view as? TextView)?.setOnClickListener {
                diaSeleccionado = view.text.toString()
                actualizarEstiloDias()
                actualizarLista()
            }
        }
    }

    private fun actualizarEstiloDias() {
        binding.llDays.children.forEach { view ->
            (view as? TextView)?.let { textView ->
                if (textView.text.toString() == diaSeleccionado) {
                    textView.setBackgroundColor(getColor(R.color.black))
                    textView.setTextColor(getColor(android.R.color.white))
                } else {
                    textView.setBackgroundColor(getColor(R.color.color_primary))
                    textView.setTextColor(getColor(android.R.color.white))
                }
            }
        }
    }

}
