package com.danielescrich.myapplication.mainmodule

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.danielescrich.myapplication.databinding.ActivityLoadingBinding

class LoadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtenemos el nombre de la clase a la que debemos redirigir
        val nextActivityName = intent.getStringExtra("nextActivity")

        try {
            val nextActivityClass = Class.forName(nextActivityName ?: "") as Class<out AppCompatActivity>
            val intentDestino = Intent(this, nextActivityClass)

            // Copiamos los extras originales (sexo, edad, etc.)
            intent.extras?.let { extras ->
                intentDestino.putExtras(extras)
            }

            // Iniciar la actividad objetivo tras una breve animación o espera
            binding.root.postDelayed({
                startActivity(intentDestino)
                finish()
            }, 1500) // 1.5 segundos opcional de espera

        } catch (e: Exception) {
            e.printStackTrace()
            finish() // Por si falla algo
        }
    }
}
