package com.danielescrich.myapplication.mainmodule

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.danielescrich.myapplication.databinding.ActivityIaNutritionBinding
import com.danielescrich.myapplication.retrofit.RetrofitInstance
import com.danielescrich.myapplication.retrofit.data.IAFavoriteRequest
import com.danielescrich.myapplication.retrofit.data.IARequest
import com.danielescrich.myapplication.retrofit.data.IAResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IANutritionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIaNutritionBinding
    private var userId: Int = -1
    private var textoFormateado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIaNutritionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
        userId = prefs.getInt("usuarioIdActivo", -1)

        val sexo = intent.getStringExtra("sexo") ?: return
        val edad = intent.getIntExtra("edad", -1)
        val altura = intent.getIntExtra("altura", -1)
        val peso = intent.getFloatExtra("peso", -1f)
        val objetivo = intent.getStringExtra("objetivo") ?: return
        val nivel = intent.getStringExtra("nivel") ?: return
        val dias = intent.getIntExtra("dias", -1)

        if (edad == -1 || altura == -1 || peso <= 0.0 || dias == -1) {
            Toast.makeText(this, "Error en los datos recibidos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val request =
            IARequest(sexo, edad, altura, peso.toFloat(), objetivo, nivel, dias, tipo = "comida")

        lifecycleScope.launch {
            var intentos = 0
            var exito = false

            val db = RetrofitInstance.userService

            while (intentos < 3 && !exito) {
                try {
                    val response: IAResponse = withContext(Dispatchers.IO) {
                        db.generarPlanIA(request)
                    }

                    textoFormateado = response.respuesta.trim()
                    binding.tvComidas.text = formatearTextoComidas(textoFormateado)
                    exito = true

                } catch (e: Exception) {
                    Log.e("IA_ERROR", "Error: ${e.message}")
                    Toast.makeText(
                        this@IANutritionActivity,
                        "Error al generar el plan. Se mostrará uno predeterminado.",
                        Toast.LENGTH_SHORT
                    ).show()

                    textoFormateado = """
                DÍA 1:
                - Desayuno | Avena con plátano
                - Comida | Arroz con pollo
                - Merienda | Yogur natural con nueces
                - Cena | Ensalada de atún
            """.trimIndent()

                    binding.tvComidas.text = formatearTextoComidas(textoFormateado)
                }

            }
        }


        binding.btnRegenerar.setOnClickListener {
            val intent = Intent(this, IAHomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.ivBack.setOnClickListener {
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
                    Toast.makeText(
                        this@IANutritionActivity,
                        "Guardado en favoritos",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun formatearTextoComidas(texto: String): CharSequence {
        val builder = SpannableStringBuilder()
        val lineas = texto.lines()

        for (linea in lineas) {
            val textoLimpio = linea.trim()
            if (textoLimpio.lowercase()
                    .matches(Regex("^(día \\d+|lunes|martes|miércoles|miercoles|jueves|viernes|sábado|sabado|domingo):?$"))
            ) {
                val textoNormalizado = textoLimpio.replace(":", "") + ":"
                val inicio = builder.length
                builder.append(textoNormalizado).append("\n\n")
                builder.setSpan(
                    StyleSpan(android.graphics.Typeface.BOLD),
                    inicio,
                    builder.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                builder.setSpan(
                    ForegroundColorSpan(Color.RED),
                    inicio,
                    builder.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else if (textoLimpio.startsWith("-")) {
                builder.append("• ").append(textoLimpio.removePrefix("-").trim()).append("\n\n")
            } else {
                builder.append(textoLimpio).append("\n\n")
            }
        }

        return builder
    }
}
