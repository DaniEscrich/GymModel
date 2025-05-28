package com.danielescrich.myapplication.mainmodule

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.danielescrich.myapplication.databinding.ActivityTermsBinding

class TermsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTermsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.tvTermsContent.text = """
            Bienvenido a GymModel, una aplicación de entrenamiento y seguimiento físico diseñada para mejorar tu experiencia fitness a través de herramientas tecnológicas avanzadas.

            Al acceder y utilizar esta aplicación, aceptas cumplir con los siguientes términos y condiciones. GymModel se reserva el derecho de modificar estos términos en cualquier momento.

            Funcionalidad de la Aplicación:
            - Registro, login, clases semanales, progreso, IA y ranking.

            Acceso y uso:
            El usuario debe proporcionar información veraz y está obligado a usar la app de forma responsable.

            Propiedad intelectual:
            Todo el contenido pertenece a GymModel. No se permite su reproducción.

            Política de privacidad:
            GymModel protege tus datos. No se comparten sin consentimiento.

            Limitación de responsabilidad:
            GymModel no se hace responsable de mal uso o resultados físicos derivados del uso de la app.

            Contacto:
            Puedes escribirnos desde la sección "Contacto" del menú.

            Gracias por usar GymModel.
        """.trimIndent()
    }
}
