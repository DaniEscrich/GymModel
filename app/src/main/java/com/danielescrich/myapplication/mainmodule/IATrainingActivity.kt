package com.danielescrich.myapplication.mainmodule

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.danielescrich.myapplication.databinding.ActivityIaTrainingBinding
import com.danielescrich.myapplication.retrofit.RetrofitInstance
import com.danielescrich.myapplication.retrofit.data.IAFavoriteRequest
import com.danielescrich.myapplication.retrofit.data.IARequest
import com.danielescrich.myapplication.retrofit.data.IAResponse
import com.danielescrich.myapplication.room.database.AppDatabase
import com.danielescrich.myapplication.room.entity.PlanTrainingEntity
import com.danielescrich.myapplication.room.entity.PlanDayItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class IATrainingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIaTrainingBinding
    private var userId: Int = -1
    private var textoFormateado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIaTrainingBinding.inflate(layoutInflater)
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

        val request = IARequest(
            sexo,
            edad,
            altura,
            peso.toFloat(),
            objetivo,
            nivel,
            dias,
            tipo = "entrenamiento"
        )

        lifecycleScope.launch {
            var intentos = 0
            var exito = false
            val db = AppDatabase.getDatabase(this@IATrainingActivity)

            while (intentos < 3 && !exito) {
                try {
                    val response: IAResponse = withContext(Dispatchers.IO) {
                        RetrofitInstance.userService.generarPlanIA(request)
                    }

                    textoFormateado = response.respuesta.trim()
                    binding.tvContenidoPlan.text = formatearTextoConEstilo(textoFormateado)

                    val planId = db.planEntrenamientoDao().insertarPlan(
                        PlanTrainingEntity(
                            userId = userId,
                            fechaCreacion = LocalDate.now().toString()
                        )
                    )

                    val items = textoFormateado.lines()
                    var diaActual = "Día X"
                    val itemsConDias = mutableListOf<PlanDayItemEntity>()

                    for (linea in items) {
                        val texto = linea.trim()
                        if (texto.uppercase().startsWith("DÍA") || texto.uppercase()
                                .startsWith("DIA")
                        ) {
                            diaActual = texto.replace(":", "")
                        } else if (texto.startsWith("-")) {
                            itemsConDias.add(
                                PlanDayItemEntity(
                                    planId = planId.toInt(),
                                    dia = diaActual,
                                    ejercicio = texto.removePrefix("-").trim()
                                )
                            )
                        }
                    }
                    db.planEntrenamientoDao().insertarItems(itemsConDias)
                    exito = true

                } catch (e: Exception) {
                    intentos++
                    Log.e("IA_ERROR", "Intento $intentos fallido: ${e.message}")
                    if (intentos == 3) {
                        Toast.makeText(
                            this@IATrainingActivity,
                            "No se pudo generar el plan tras varios intentos, se le mostrará uno generico",
                            Toast.LENGTH_LONG
                        ).show()

                        textoFormateado = """
                            Día 1:
                            - Sentadillas 3x12
                            - Flexiones 3x10
                            
                            Día 2:
                            - Peso muerto 3x10
                            - Plancha 3x30s
                            """.trimIndent()

                        binding.tvContenidoPlan.text = formatearTextoConEstilo(textoFormateado)

// NO GUARDAMOS EN ROOM si es el plan por defecto
                        return@launch

                    }
                }
            }
        }


        binding.btnRegenerar.setOnClickListener {
            startActivity(Intent(this, IAHomeActivity::class.java))
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
                        this@IATrainingActivity,
                        "Guardado en favoritos",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@IATrainingActivity,
                        "Error al guardar favorito",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun formatearTextoConEstilo(texto: String): CharSequence {
        val builder = SpannableStringBuilder()
        val lineas = texto.lines()

        for (linea in lineas) {
            val textoLimpio = linea.trim()
            if (textoLimpio.uppercase().startsWith("DÍA") || textoLimpio.uppercase()
                    .startsWith("DIA")
            ) {
                val normalizado = textoLimpio.replace(":", "") + ":"
                val inicio = builder.length
                builder.append(normalizado).append("\n")

                // Aplicar negrita y color rojo
                builder.setSpan(
                    StyleSpan(android.graphics.Typeface.BOLD),
                    inicio,
                    builder.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                builder.setSpan(
                    android.text.style.ForegroundColorSpan(android.graphics.Color.RED),
                    inicio,
                    builder.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                builder.append(textoLimpio).append("\n")
            }
        }
        return builder
    }

}
