package com.danielescrich.myapplication.mainmodule

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.danielescrich.myapplication.databinding.ActivityIaEntrenamientoBinding
import com.danielescrich.myapplication.retrofit.RetrofitInstance
import com.danielescrich.myapplication.retrofit.data.IARequest
import com.danielescrich.myapplication.retrofit.data.IAResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IAEntrenamientoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIaEntrenamientoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIaEntrenamientoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sexo = intent.getStringExtra("sexo") ?: return
        val edad = intent.getIntExtra("edad", -1)
        val altura = intent.getIntExtra("altura", -1)
        val peso = intent.getStringExtra("peso")?.toDoubleOrNull() ?: -1.0
        val objetivo = intent.getStringExtra("objetivo") ?: return
        val nivel = intent.getStringExtra("nivel") ?: return
        val dias = intent.getIntExtra("dias", -1)

        if (edad == -1 || altura == -1 || peso <= 0.0 || dias == -1) {
            Toast.makeText(this, "Error en los datos recibidos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val request = IARequest(sexo, edad, altura, peso.toFloat(), objetivo, nivel, dias)

        lifecycleScope.launch {
            try {
                val response: IAResponse = withContext(Dispatchers.IO) {
                    RetrofitInstance.userService.generarPlanIA(request)
                }

                val respuesta = response.respuesta
                val partes = respuesta.split("Plan de comidas", ignoreCase = true, limit = 2)
                binding.tvEntrenamiento.text = partes.getOrNull(0)?.trim()
                    ?: "No se pudo generar el plan de entrenamiento"

            } catch (e: Exception) {
                Log.e("IA_ERROR", "Error: ${e.message}")
                Toast.makeText(this@IAEntrenamientoActivity, "Error al generar el plan", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRegenerar.setOnClickListener {
            finish()
        }
    }
}
