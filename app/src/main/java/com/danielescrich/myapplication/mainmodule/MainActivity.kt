package com.danielescrich.myapplication.mainmodule

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.danielescrich.myapplication.R
import com.danielescrich.myapplication.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

open class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    protected lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.navigationView.setNavigationItemSelectedListener(this)

        binding.ivProfile.setOnClickListener {
            val popup = android.widget.PopupMenu(this, it)
            popup.menuInflater.inflate(R.menu.menu_profile, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_profile -> {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        true
                    }

                    R.id.menu_logout -> {
                        prefs.edit().clear().apply()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                        true
                    }

                    else -> false
                }
            }
            popup.show()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_classes -> startActivity(Intent(this, ClassesActivity::class.java))
                R.id.nav_progress -> startActivity(Intent(this, ProgressActivity::class.java))
                R.id.nav_ranking -> startActivity(Intent(this, RankingActivity::class.java))
                R.id.nav_ia -> startActivity(Intent(this, IAHomeActivity::class.java))
            }
            true
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_preferencias -> startActivity(Intent(this, PreferencesActivity::class.java))
            R.id.nav_terminos -> startActivity(Intent(this, TermsActivity::class.java))
            R.id.nav_sobre -> startActivity(Intent(this, AboutActivity::class.java))
            R.id.nav_contacto -> startActivity(Intent(this, ContactActivity::class.java))
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
