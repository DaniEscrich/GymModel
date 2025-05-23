package com.danielescrich.myapplication.mainmodule

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.danielescrich.myapplication.databinding.ActivityFormIaBinding

class FormIAActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormIaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFormIaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón de volver
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Spinners
        binding.spSexo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Masculino", "Femenino"))
        binding.spObjetivo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Adelgazar", "Ganar músculo", "Mantener"))
        binding.spNivel.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Principiante", "Intermedio", "Avanzado"))
        binding.spEdad.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, (13..80).toList())
        binding.spAltura.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, (120..220 step 5).toList())
        binding.spPeso.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, (40..140 step 5).toList())
        binding.spDias.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, (1..7).toList())

        // Acción para generar
        val launchWithMode: (Class<*>) -> Unit = { destination ->
            val intent = Intent(this, LoadingActivity::class.java).apply {
                putExtra("sexo", binding.spSexo.selectedItem.toString())
                putExtra("edad", binding.spEdad.selectedItem.toString().toInt())
                putExtra("altura", binding.spAltura.selectedItem.toString().toInt())
                putExtra("peso", binding.spPeso.selectedItem.toString())
                putExtra("objetivo", binding.spObjetivo.selectedItem.toString())
                putExtra("nivel", binding.spNivel.selectedItem.toString())
                putExtra("dias", binding.spDias.selectedItem.toString().toInt())
                putExtra("nextActivity", destination.name)
            }
            startActivity(intent)
        }

        binding.btnGenerarEntrenamiento.setOnClickListener {
            launchWithMode(IAEntrenamientoActivity::class.java)
        }

        binding.btnGenerarComidas.setOnClickListener {
            launchWithMode(IAComidasActivity::class.java)
        }
    }
}
