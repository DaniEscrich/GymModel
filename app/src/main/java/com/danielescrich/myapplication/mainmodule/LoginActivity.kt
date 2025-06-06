package com.danielescrich.myapplication.mainmodule

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.danielescrich.myapplication.databinding.ActivityLoginBinding
import com.danielescrich.myapplication.retrofit.RetrofitInstance
import com.danielescrich.myapplication.retrofit.data.LoginRequest
import com.danielescrich.myapplication.retrofit.data.UserEntity
import com.danielescrich.myapplication.room.database.AppDatabase
import com.danielescrich.myapplication.room.entity.UsuarioRoomEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
        val savedNombreUsuario = prefs.getString("nombreUsuario", "")
        if (!savedNombreUsuario.isNullOrEmpty()) {
            mBinding.etUsernameLogin.setText(savedNombreUsuario)
            mBinding.cbRememberMe.isChecked = true
        }

        mBinding.tvRegisterHere.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        mBinding.btnLogin.setOnClickListener {
            val nombreUsuario = mBinding.etUsernameLogin.text.toString().trim()
            val password = mBinding.etPasswordLogin.text.toString().trim()

            if (nombreUsuario.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(nombreUsuario, password)

            lifecycleScope.launch {
                try {
                    val user: UserEntity = withContext(Dispatchers.IO) {
                        RetrofitInstance.userService.login(request)
                    }

                    if (mBinding.cbRememberMe.isChecked) {
                        prefs.edit().putString("nombreUsuario", nombreUsuario).apply()
                    } else {
                        prefs.edit().remove("nombreUsuario").apply()
                    }

                    prefs.edit()
                        .putInt("usuarioIdActivo", user.id)
                        .putString("createdAt", user.createdAt.substring(0, 19))
                        .putString("nombreUsuarioActivo", user.nombreUsuario)
                        .apply()

                    val db = Room.databaseBuilder(
                        applicationContext,
                        AppDatabase::class.java,
                        "clases_db"
                    )
                        .fallbackToDestructiveMigration().build()

                    withContext(Dispatchers.IO) {
                        val usuarioDao = db.usuarioDao()
                        val yaExiste = usuarioDao.getNombreUsuarioPorId(user.id)
                        if (yaExiste == null) {
                            usuarioDao.insertUsuario(
                                UsuarioRoomEntity(
                                    id = user.id,
                                    nombre = user.nombreUsuario
                                )
                            )
                        }
                    }

                    Toast.makeText(
                        this@LoginActivity,
                        "¡Bienvenido ${user.nombreUsuario}!",
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finish()

                } catch (e: Exception) {
                    val errorMessage = if (e.localizedMessage?.contains("401") == true) {
                        "Credenciales inválidas"
                    } else {
                        "Error al iniciar sesión"
                    }

                    Log.e("API_ERROR", "Error al iniciar sesión: ${e.localizedMessage}")
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
