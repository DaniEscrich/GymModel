package com.danielescrich.myapplication.mainmodule

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.danielescrich.myapplication.databinding.ActivityChatCoachBinding
import com.danielescrich.myapplication.retrofit.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatCoachActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatCoachBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatCoachBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Toast.makeText(this, "No hay entrenadores disponibles. Puedes hablar con respuestas automáticas.", Toast.LENGTH_LONG).show()

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnSend.setOnClickListener {
            val mensaje = binding.etMensaje.text.toString().trim()
            if (mensaje.isEmpty()) {
                Toast.makeText(this, "Escribe un mensaje primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.tvRespuesta.visibility = View.GONE

            lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitInstance.userService.enviarMensajeChat(mapOf("mensaje" to mensaje))
                    }

                    binding.tvRespuesta.text = response.respuesta
                    binding.tvRespuesta.visibility = View.VISIBLE
                    binding.etMensaje.setText("")

                } catch (e: Exception) {
                    binding.tvRespuesta.text = "Error al obtener respuesta. Intenta de nuevo."
                    binding.tvRespuesta.visibility = View.VISIBLE
                } finally {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }
}
