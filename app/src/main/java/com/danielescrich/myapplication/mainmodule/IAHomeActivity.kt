package com.danielescrich.myapplication.mainmodule

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.danielescrich.myapplication.R
import com.danielescrich.myapplication.databinding.ActivityIaHomeBinding

class IAHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIaHomeBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIaHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbarAndDrawer()
        setupProfileMenu()
        setupBottomNavigation()
        setupCardListeners()
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
                R.id.nav_ia -> true
                else -> false
            }
        }
        binding.bottomNavigation.menu.findItem(R.id.nav_ia).isChecked = true
    }

    private fun setupCardListeners() {
        // Entrenamiento → FormIAActivity con tipo entrenamiento
        binding.cardEntrenamiento.setOnClickListener {
            val intent = Intent(this, FormIAActivity::class.java)
            intent.putExtra("tipoPlan", "entrenamiento")
            startActivity(intent)
        }

        // Nutrición → FormIAActivity con tipo nutricion
        binding.cardNutricion.setOnClickListener {
            val intent = Intent(this, FormIAActivity::class.java)
            intent.putExtra("tipoPlan", "nutricion")
            startActivity(intent)
        }

        // Favoritos → IAFavoritosActivity directamente
        binding.cardFavoritos.setOnClickListener {
            startActivity(Intent(this, IAFavoritesActivity::class.java))
        }
        // Favoritos → IAFavoritosActivity directamente
        binding.cardEntrenamientoEnCurso.setOnClickListener {
            startActivity(Intent(this, PlanTrainingInCourseActivity::class.java))
        }
        // Favoritos → IAFavoritosActivity directamente
        binding.cardNutricionEnCurso.setOnClickListener {
            startActivity(Intent(this, PlanNutritionInCourseActivity::class.java))
        }
    }

}
