package com.danielescrich.myapplication.mainmodule

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.danielescrich.myapplication.R
import com.danielescrich.myapplication.databinding.ActivityRegisterBinding
import com.danielescrich.myapplication.retrofit.RetrofitInstance
import com.danielescrich.myapplication.retrofit.data.RegisterRequest
import com.danielescrich.myapplication.retrofit.data.UserEntity
import com.danielescrich.myapplication.room.database.AppDatabase
import com.danielescrich.myapplication.room.entity.UsuarioRoomEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.tvLogin2.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.tvViewTerms.setOnClickListener {
            mostrarDialogoTerminos()
        }

        binding.btnRegister.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun registrarUsuario() {
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        if (username.isBlank()) {
            binding.etUsername.error = getString(R.string.text_error_name)
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.text_email_incorrect)
            return
        }
        if (password.length < 6) {
            binding.etPassword.error = getString(R.string.text_password_6_characters)
            return
        }
        if (!binding.cbTerms.isChecked) {
            Toast.makeText(this, getString(R.string.text_accept_terms), Toast.LENGTH_SHORT).show()
            return
        }

        val request = RegisterRequest(username, email, password)

        lifecycleScope.launch {
            try {
                val user: UserEntity = withContext(Dispatchers.IO) {
                    RetrofitInstance.userService.register(request)
                }

                val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "clases_db")
                    .fallbackToDestructiveMigration()
                    .build()

                withContext(Dispatchers.IO) {
                    db.usuarioDao().insertUsuario(
                        UsuarioRoomEntity(
                            id = user.id,
                            nombre = user.nombreUsuario
                        )
                    )
                }
                Toast.makeText(
                    this@RegisterActivity,
                    "Registro Completado." + "\n" + "Inicia Sesión para continuar.",

                    Toast.LENGTH_LONG
                ).show()

                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()

            } catch (e: Exception) {
                val errorMessage = if (e.localizedMessage?.contains("400") == true) {
                    getString(R.string.text_username_or_email_repeat)
                } else {
                    getString(R.string.text_error_register)
                }

                Log.e("API_ERROR", "Error al registrar: ${e.localizedMessage}")
                Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoTerminos() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_terms, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val tvContenido = dialogView.findViewById<TextView>(R.id.tvTermsDialogContent)
        val btnAceptar = dialogView.findViewById<Button>(R.id.btnAceptarDialog)
        tvContenido.text = getString(R.string.text_terms_register).trimIndent()
        btnAceptar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
