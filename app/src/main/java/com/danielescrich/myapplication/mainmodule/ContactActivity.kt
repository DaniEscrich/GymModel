package com.danielescrich.myapplication.mainmodule

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.danielescrich.myapplication.databinding.ActivityContactBinding

class ContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón de retroceso
        binding.ivBack.setOnClickListener {
            finish()
        }

        // Asignar textos con íconos bonitos y formato claro
        binding.tvTitle.text = "¿Tienes dudas o sugerencias?"
        binding.tvContactEmail.text = "✉️ contacto@gymmodelapp.com"
        binding.tvContactPhone.text = "📞 +34 675 04 02 83"
        binding.tvContactSchedule.text = "🕐 Lunes a viernes de 9:00 a 21:00"
        binding.tvThanks.text = "😊 ¡Estaremos encantados de ayudarte!"
    }
}
