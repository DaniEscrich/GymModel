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

        // Botón volver atrás
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Información de contacto
        binding.tvContactContent.text = """
            ¿Tienes dudas o sugerencias?

            ✉️ Email: contacto@gymmodelapp.com
            📞 Teléfono: +34 675 04 02 83
            🕐 Horario: Lunes a viernes de 9:00 a 21:00

            ¡Estaremos encantados de ayudarte!
        """.trimIndent()
    }
}
