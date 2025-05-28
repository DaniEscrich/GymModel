package com.danielescrich.myapplication.mainmodule

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.danielescrich.myapplication.R
import com.danielescrich.myapplication.databinding.ActivityProgresoBinding
import com.danielescrich.myapplication.retrofit.RetrofitInstance
import com.danielescrich.myapplication.retrofit.data.Progress
import com.danielescrich.myapplication.room.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProgresoBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgresoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnActualizarPeso.setOnClickListener {
            val pesoStr = binding.etPeso.text.toString()
            if (pesoStr.isBlank()) {
                Toast.makeText(this, "Introduce un peso válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val peso = pesoStr.toFloat()
            val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val userId = obtenerUsuarioIdActivo()

            val nuevoProgreso = Progress(
                id = null,
                userId = userId,
                weight = peso,
                date = fecha
            )

            lifecycleScope.launch {
                try {
                    RetrofitInstance.progressService.addProgress(nuevoProgreso)
                    binding.etPeso.text.clear()
                    cargarDatosProgreso()
                    Toast.makeText(this@ProgressActivity, "Progreso actualizado", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@ProgressActivity, "Error al guardar el progreso", Toast.LENGTH_SHORT).show()
                }
            }
        }


        setupToolbarAndDrawer()
        setupProfileMenu()
        setupBottomNavigation()
        cargarDatosProgreso()
    }

    private fun obtenerUsuarioIdActivo(): Int {
        val prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
        return prefs.getInt("usuarioIdActivo", -1)
    }

    private fun cargarDatosProgreso() {
        lifecycleScope.launch {
            val userId = obtenerUsuarioIdActivo()

            // Cargar progreso físico (peso)
            try {
                val progresoList = withContext(Dispatchers.IO) {
                    RetrofitInstance.progressService.getProgressByUser(userId)
                }

                if (progresoList.isNotEmpty()) {
                    val actual = progresoList.last().weight
                    val anterior = if (progresoList.size > 1) progresoList[progresoList.size - 2].weight else actual
                    val diferencia = actual - anterior

                    binding.tvPesoActual.text = "${String.format("%.1f", actual)} kg"
                    binding.tvDiferencia.text = "${if (diferencia >= 0) "+" else ""}${String.format("%.1f", diferencia)} kg"
                } else {
                    binding.tvPesoActual.text = "N/A"
                    binding.tvDiferencia.text = "0.0 kg"
                }

            } catch (e: Exception) {
                Toast.makeText(this@ProgressActivity, "Error al cargar progreso", Toast.LENGTH_SHORT).show()
            }

            // Cargar clases asistidas desde Room
            val clasesAsistidas = withContext(Dispatchers.IO) {
                val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "clases_db")
                    .fallbackToDestructiveMigration()
                    .build()
                db.claseUsuarioDao().getTotalClasesAsistidas(userId)
            }

            // Mostrar resultados
            binding.tvClases.text = clasesAsistidas.toString()

            // Mostrar fecha en que se registró el usuario
            val fechaRegistro = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
                .getString("createdAt", null)

            val textoFecha = fechaRegistro?.let {
                try {
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
                    if (fecha != null) {
                        val formateador = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
                        formateador.format(fecha)
                    } else {
                        "Fecha no disponible"
                    }
                } catch (e: Exception) {
                    "Fecha no disponible"
                }
            } ?: "Fecha no disponible"


            binding.tvDiasGimnasio.text = textoFecha
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
                R.id.nav_ranking ->{
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
