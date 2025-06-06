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
    }
}
