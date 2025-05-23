package com.danielescrich.myapplication.mainmodule

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.danielescrich.myapplication.R
import com.danielescrich.myapplication.databinding.ActivityHomeBinding
import com.danielescrich.myapplication.retrofit.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbarAndDrawer()
        setupProfileMenu()
        setupBottomNavigation()
        setupUserInfo()

        obtenerConsejoIA()
        mostrarDiasEnGimnasio()
        mostrarPlanesGenerados()
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
                R.id.nav_home -> true
                R.id.nav_classes -> {
                    startActivity(Intent(this, ClassesActivity::class.java))
                    true
                }
                R.id.nav_progress -> {
                    startActivity(Intent(this, ProgressActivity::class.java))
                    true
                }
                R.id.nav_ranking -> {
                    startActivity(Intent(this, RankingActivity::class.java))
                    true
                }
                R.id.nav_ia -> {
                    startActivity(Intent(this, IAHomeActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }


    private fun setupUserInfo() {
        val prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
        val nombreUsuario = prefs.getString("nombreUsuarioActivo", "Usuario") ?: "Usuario"
        binding.tvWelcomeUser.text = "¡Hola, $nombreUsuario!"
    }


    private fun obtenerConsejoIA() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.userService.generarConsejoDelDia()
                }
                binding.tvAIAdvice.text = "🤖 Consejo del día: ${response.respuesta}"

                val prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
                val planes = prefs.getInt("planesGenerados", 0)
                prefs.edit().putInt("planesGenerados", planes + 1).apply()
            } catch (e: Exception) {
                binding.tvAIAdvice.text = "🤖 Consejo del día: Mantén una actitud positiva y entrena con constancia."
            }
        }
    }


    private fun mostrarDiasEnGimnasio() {
        val prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
        val fechaStr = prefs.getString("createdAt", null)
        fechaStr?.let {
            val formatos = listOf("yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd")
            val fechaRegistro = formatos.firstNotNullOfOrNull { formato ->
                try {
                    SimpleDateFormat(formato, Locale.getDefault()).parse(fechaStr)
                } catch (e: Exception) {
                    null
                }
            }
            val texto = if (fechaRegistro != null) {
                val dias = ((Date().time - fechaRegistro.time) / (1000 * 60 * 60 * 24)).toInt()
                "📆 Llevas $dias días en el gimnasio"
            } else {
                "📆 Fecha de registro no disponible"
            }
            binding.tvNextClass.text = texto
        }
    }


    private fun mostrarPlanesGenerados() {
        val prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
        val total = prefs.getInt("planesGenerados", 0)
        binding.tvWeeklyProgress.text = "📄 Planes personalizados generados: $total"
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
