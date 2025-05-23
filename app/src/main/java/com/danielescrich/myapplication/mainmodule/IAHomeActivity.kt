package com.danielescrich.myapplication.mainmodule

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.danielescrich.myapplication.R
import com.danielescrich.myapplication.databinding.ActivityIaHomeBinding

class IAHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIaHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIaHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón que lleva al formulario
        binding.btnGenerar.setOnClickListener {
            startActivity(Intent(this, FormIAActivity::class.java))
        }

        // Marcamos el botón de IA como activo
        binding.bottomNavigation.selectedItemId = R.id.nav_ia

        // Navegación
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_classes -> startActivity(Intent(this, ClassesActivity::class.java))
                R.id.nav_progress -> startActivity(Intent(this, ProgressActivity::class.java))
                R.id.nav_ranking -> startActivity(Intent(this, RankingActivity::class.java))
                R.id.nav_ia -> return@setOnItemSelectedListener true
            }
            true
        }
    }
}
