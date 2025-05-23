package com.danielescrich.myapplication.mainmodule

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
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

    private lateinit var mBinding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.tvLogin2.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        mBinding.btnRegister.setOnClickListener {
            val nombreUsuario = mBinding.etUsername.text.toString().trim()
            val correo = mBinding.etEmail.text.toString().trim()
            val password = mBinding.etPassword.text.toString().trim()

            if (nombreUsuario.isBlank()) {
                mBinding.etUsername.error = "El nombre de usuario es obligatorio"
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                mBinding.etEmail.error = "Correo electrónico inválido"
                return@setOnClickListener
            }

            if (password.length < 6) {
                mBinding.etPassword.error = "La contraseña debe tener al menos 6 caracteres"
                return@setOnClickListener
            }

            if (!mBinding.cbTerms.isChecked) {
                Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = RegisterRequest(nombreUsuario, correo, password)

            lifecycleScope.launch {
                try {
                    val user: UserEntity = withContext(Dispatchers.IO) {
                        RetrofitInstance.userService.register(request)
                    }

                    // GUARDAR EN ROOM
                    val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "clases_db")
                        .fallbackToDestructiveMigration().build()

                    withContext(Dispatchers.IO) {
                        val usuarioDao = db.usuarioDao()
                        usuarioDao.insertUsuario(
                            UsuarioRoomEntity(
                                id = user.id,
                                nombre = user.nombreUsuario
                            )
                        )
                    }

                    Toast.makeText(
                        this@RegisterActivity,
                        "¡Usuario ${user.nombreUsuario} registrado con ID ${user.id}!",
                        Toast.LENGTH_LONG
                    ).show()

                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()

                } catch (e: Exception) {
                    val errorMessage = if (e.localizedMessage?.contains("400") == true) {
                        "El nombre de usuario o correo ya están en uso"
                    } else {
                        "Error al registrar"
                    }

                    Log.e("API_ERROR", "Error al registrar: ${e.localizedMessage}")
                    Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
