package com.danielescrich.myapplication.mainmodule

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.danielescrich.myapplication.databinding.ActivityIaComidasBinding
import com.danielescrich.myapplication.retrofit.RetrofitInstance
import com.danielescrich.myapplication.retrofit.data.IAFavoriteRequest
import com.danielescrich.myapplication.retrofit.data.IARequest
import com.danielescrich.myapplication.retrofit.data.IAResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IAComidasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIaComidasBinding
    private var userId: Int = -1
    private var textoFormateado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIaComidasBinding.inflate(layoutInflater)
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
                textoFormateado = partes.getOrNull(1)?.trim()
                    ?.replace("-", "•")
                    ?.replace("\n", "\n\n")
                    ?: "No se pudo generar el plan de comidas"

                binding.tvComidas.text = textoFormateado

            } catch (e: Exception) {
                Log.e("IA_ERROR", "Error: ${e.message}")
                Toast.makeText(this@IAComidasActivity, "Error al generar el plan. Se mostrará uno predeterminado.", Toast.LENGTH_SHORT).show()

                textoFormateado = """
                • Lunes:
                
                Desayuno: Tostadas integrales con aguacate y huevo.
                
                Comida: Pollo a la plancha con arroz integral y verduras.
                
                Merienda: Yogur natural con nueces.
                
                Cena: Sopa de verduras y pescado blanco.
                
                • Martes:
                
                Desayuno: Avena con plátano y leche.
                
                Comida: Lentejas estofadas con arroz.
                
                Merienda: Batido de frutas y almendras.
                
                Cena: Ensalada de atún con huevo cocido y tomate.
    """.trimIndent().replace("\n", "\n\n")

                binding.tvComidas.text = textoFormateado
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
                    Toast.makeText(this@IAComidasActivity, "Guardado en favoritos", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@IAComidasActivity, "Error al guardar favorito", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("IA_FAVORITO", "Error al guardar favorito", e)
                Toast.makeText(this@IAComidasActivity, "Error de red al guardar favorito", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
