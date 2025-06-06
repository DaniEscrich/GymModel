package com.danielescrich.myapplication.mainmodule

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import com.danielescrich.myapplication.R
import com.danielescrich.myapplication.databinding.ActivityPreferencesBinding

class PreferencesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreferencesBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.switchNotificaciones.isChecked = prefs.getBoolean("notificaciones_activadas", true)
        binding.switchTemaOscuro.isChecked = prefs.getBoolean("tema_oscuro", false)

        binding.switchNotificaciones.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            prefs.edit().putBoolean("notificaciones_activadas", isChecked).apply()
        }

        binding.switchTemaOscuro.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            prefs.edit().putBoolean("tema_oscuro", isChecked).apply()
        }


        binding.tvAppVersion.text = getString(R.string.text_appversion)
    }
}
