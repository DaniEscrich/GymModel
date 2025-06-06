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

        // Obtener tipo de plan del intent
        val tipoPlan = intent.getStringExtra("tipoPlan") ?: "entrenamiento"

        // Spinners
        binding.spSexo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Masculino", "Femenino"))
        binding.spObjetivo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Adelgazar", "Ganar músculo", "Mantener"))
        binding.spNivel.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Principiante", "Intermedio", "Avanzado"))
        binding.spAltura.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, (120..220 step 5).toList())
        binding.spDias.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, (1..7).toList())

        // Inicializar valores por defecto
        binding.etEdad.setText("25")
        binding.etPeso.setText("70")

        // Flechas edad
        binding.btnEdadUp.setOnClickListener {
            val edad = binding.etEdad.text.toString().toIntOrNull() ?: 25
            if (edad < 80) binding.etEdad.setText((edad + 1).toString())
        }
        binding.btnEdadDown.setOnClickListener {
            val edad = binding.etEdad.text.toString().toIntOrNull() ?: 25
            if (edad > 13) binding.etEdad.setText((edad - 1).toString())
        }

        // Flechas peso
        binding.btnPesoUp.setOnClickListener {
            val peso = binding.etPeso.text.toString().toFloatOrNull() ?: 70f
            if (peso < 140) binding.etPeso.setText((peso + 1).toInt().toString())
        }
        binding.btnPesoDown.setOnClickListener {
            val peso = binding.etPeso.text.toString().toFloatOrNull() ?: 70f
            if (peso > 40) binding.etPeso.setText((peso - 1).toInt().toString())
        }

        // Botón generar
        binding.btnGenerarPlan.setOnClickListener {
            val edad = binding.etEdad.text.toString().toIntOrNull()
            val peso = binding.etPeso.text.toString().toFloatOrNull()

            if (edad == null || peso == null) {
                Toast.makeText(this, "Por favor, rellena correctamente edad y peso", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nextActivity = when (tipoPlan.lowercase()) {
                "nutricion" -> IANutritionActivity::class.java
                else -> IATrainingActivity::class.java
            }

            val intent = Intent(this, LoadingActivity::class.java).apply {
                putExtra("sexo", binding.spSexo.selectedItem.toString())
                putExtra("edad", edad)
                putExtra("altura", binding.spAltura.selectedItem.toString().toInt())
                putExtra("peso", peso)
                putExtra("objetivo", binding.spObjetivo.selectedItem.toString())
                putExtra("nivel", binding.spNivel.selectedItem.toString())
                putExtra("dias", binding.spDias.selectedItem.toString().toInt())
                putExtra("nextActivity", nextActivity.name)
            }

            startActivity(intent)
        }
    }
}
