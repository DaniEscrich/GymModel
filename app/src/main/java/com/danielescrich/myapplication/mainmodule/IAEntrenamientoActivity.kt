package com.danielescrich.myapplication.mainmodule

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.danielescrich.myapplication.databinding.ActivityIaEntrenamientoBinding
import com.danielescrich.myapplication.retrofit.RetrofitInstance
import com.danielescrich.myapplication.retrofit.data.IAFavoriteRequest
import com.danielescrich.myapplication.retrofit.data.IARequest
import com.danielescrich.myapplication.retrofit.data.IAResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class IAEntrenamientoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIaEntrenamientoBinding
    private var userId: Int = -1
    private var textoFormateado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIaEntrenamientoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
        userId = prefs.getInt("usuarioIdActivo", -1)

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
                textoFormateado = partes.getOrNull(0)?.trim()
                    ?.replace("-", "•")
                    ?.replace("\n", "\n\n") ?: "No se pudo generar el plan de entrenamiento"

                binding.tvEntrenamiento.text = textoFormateado

            } catch (e: Exception) {
                Log.e("IA_ERROR", "Error: ${e.message}")
                Toast.makeText(this@IAEntrenamientoActivity, "Error al generar el plan. Se mostrará uno predeterminado.", Toast.LENGTH_SHORT).show()

                textoFormateado = """
        • Día 1:
        
        - Calentamiento: 10 minutos de caminata rápida.
        - Sentadillas 3x12
        - Flexiones 3x10
        - Plancha abdominal 3x30 segundos
        
        • Día 2:
        
        - Calentamiento: 10 minutos de bicicleta estática.
        - Peso muerto con mancuernas 3x10
        - Remo con banda elástica 3x12
        - Abdominales bicicleta 3x20
        
        • Día 3:
        
        - Calentamiento: Jumping Jacks 3x30 segundos.
        - Zancadas 3x12 por pierna
        - Fondos de tríceps en banco 3x10
        - Superman 3x15
    """.trimIndent().replace("-", "•").replace("\n", "\n\n")

                binding.tvEntrenamiento.text = textoFormateado
            }

        }

        binding.btnRegenerar.setOnClickListener {
            val intent = Intent(this, IAHomeActivity::class.java)
            startActivity(intent)
            finish()
        }


        binding.ivBack.setOnClickListener{
            finish()
        }

        binding.checkboxFavorito.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                guardarComoFavorito()
            }
        }
    }

    private fun guardarComoFavorito() {
        if (userId == -1 || textoFormateado.isBlank()) {
            Log.e("IA_DEBUG", "userId = $userId")
            Log.e("IA_DEBUG", "textoFormateado = '$textoFormateado'")
            Toast.makeText(this, "No se puede guardar el favorito", Toast.LENGTH_SHORT).show()
            return
        }


        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.userService.guardarFavorito(
                        IAFavoriteRequest(userId, textoFormateado)
                    )

                }
                if (response.success) {
                    Toast.makeText(this@IAEntrenamientoActivity, "Guardado en favoritos", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@IAEntrenamientoActivity, "Error al guardar favorito", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("IA_FAVORITO", "Error al guardar favorito", e)
                Toast.makeText(this@IAEntrenamientoActivity, "Error de red al guardar favorito", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
