package com.danielescrich.myapplication.mainmodule

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.danielescrich.myapplication.databinding.ActivityProfileBinding
import com.danielescrich.myapplication.retrofit.RetrofitInstance
import com.danielescrich.myapplication.retrofit.data.ProfileUpdaterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val PICK_IMAGE = 1
    private var imageUri: Uri? = null
    private lateinit var nombreUsuario: String
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
        nombreUsuario = prefs.getString("nombreUsuarioActivo", "") ?: ""
        userId = prefs.getInt("usuarioIdActivo", -1)

        binding.tvNombreUsuario.text = nombreUsuario
        binding.etCorreo.isEnabled = false

        // Obtener correo electrónico desde API
        lifecycleScope.launch {
            try {
                val correoResponse = withContext(Dispatchers.IO) {
                    RetrofitInstance.userService.obtenerDatosPerfil(userId)
                }
                val correo = correoResponse.body()?.correoElectronico
                binding.etCorreo.setText(correo ?: "")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Obtener imagen de perfil desde API
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.userService.obtenerImagenPerfil(userId)
                }
                val base64 = response.body()?.imagenBase64
                base64?.let {
                    val bytes = Base64.decode(it, Base64.DEFAULT)
                    Glide.with(this@ProfileActivity)
                        .asBitmap()
                        .load(bytes)
                        .into(binding.ivProfilePic)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Seleccionar nueva imagen
        binding.ivProfilePic.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE)
        }

        // Botón atrás
        binding.ivBack.setOnClickListener {
            finish()
        }

        // Botón actualizar
        binding.btnConfirmarCambio.setOnClickListener {
            val oldPass = binding.etPasswordActual.text.toString()
            val newPass = binding.etPasswordNueva.text.toString()

            if (newPass.isNotBlank() && oldPass.isBlank()) {
                Toast.makeText(this, "Introduce también la contraseña actual", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val imagenBase64 = convertirImagenABase64(imageUri)

            val request = ProfileUpdaterRequest(
                id = userId,
                correoElectronico = null,
                nuevaPassword = if (newPass.isNotBlank()) newPass else null,
                imagenBase64 = imagenBase64
            )

            lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitInstance.userService.actualizarPerfil(request)
                    }

                    if (response.isSuccessful) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ProfileActivity, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                            binding.etPasswordActual.text.clear()
                            binding.etPasswordNueva.text.clear()
                        }
                    } else {
                        showToast("Error al actualizar perfil")
                    }
                } catch (e: Exception) {
                    showToast("Error: ${e.message}")
                }
            }
        }
    }

    private fun convertirImagenABase64(uri: Uri?): String? {
        return try {
            uri?.let {
                val inputStream: InputStream? = contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            Glide.with(this).load(imageUri).into(binding.ivProfilePic)
        }
    }

    private fun showToast(msg: String) {
        runOnUiThread {
            Toast.makeText(this@ProfileActivity, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
