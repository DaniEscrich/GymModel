package com.danielescrich.myapplication.mainmodule

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.danielescrich.myapplication.adapter.ChatAdapter
import com.danielescrich.myapplication.databinding.ActivityChatCoachBinding
import com.danielescrich.myapplication.model.MessageChat
import com.danielescrich.myapplication.retrofit.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatCoachActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatCoachBinding
    private val listaMensajes = mutableListOf<MessageChat>()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatCoachBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            finish()
        }
        Toast.makeText(
            this,
            "No hay entrenadores disponibles. Puedes hablar con respuestas automáticas.",
            Toast.LENGTH_LONG
        ).show()

        // Configurar RecyclerView
        chatAdapter = ChatAdapter(listaMensajes)
        binding.recyclerMensajes.layoutManager = LinearLayoutManager(this)
        binding.recyclerMensajes.adapter = chatAdapter

        binding.btnSend.setOnClickListener {
            val mensajeUsuario = binding.etMensaje.text.toString().trim()
            if (mensajeUsuario.isEmpty()) {
                Toast.makeText(this, "Escribe un mensaje primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Añadir mensaje del usuario
            listaMensajes.add(MessageChat(mensajeUsuario, esUsuario = true))
            chatAdapter.notifyItemInserted(listaMensajes.size - 1)
            binding.recyclerMensajes.scrollToPosition(listaMensajes.size - 1)
            binding.etMensaje.setText("")

            // Intentar respuesta hasta 3 veces
            lifecycleScope.launch {
                var success = false
                var intentos = 0
                while (intentos < 3 && !success) {
                    try {
                        val response = withContext(Dispatchers.IO) {
                            RetrofitInstance.userService.enviarMensajeChat(mapOf("mensaje" to mensajeUsuario))
                        }
                        val respuestaIA = response.respuesta.trim()
                        listaMensajes.add(MessageChat(respuestaIA, esUsuario = false))
                        chatAdapter.notifyItemInserted(listaMensajes.size - 1)
                        binding.recyclerMensajes.scrollToPosition(listaMensajes.size - 1)
                        success = true
                    } catch (e: Exception) {
                        intentos++
                        if (intentos >= 3) {
                            val errorMensaje = "Error al obtener respuesta tras varios intentos."
                            listaMensajes.add(MessageChat(errorMensaje, esUsuario = false))
                            chatAdapter.notifyItemInserted(listaMensajes.size - 1)
                            binding.recyclerMensajes.scrollToPosition(listaMensajes.size - 1)
                        }
                    }
                }
            }
        }
    }
}

