package com.danielescrich.myapplication.mainmodule

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.danielescrich.myapplication.databinding.ActivityIaFavoritosBinding
import com.danielescrich.myapplication.mainmodule.adapter.IAFavoritoAdapter
import com.danielescrich.myapplication.mainmodule.adapter.OnIaFavoriteClickListener
import com.danielescrich.myapplication.retrofit.RetrofitInstance
import com.danielescrich.myapplication.retrofit.data.IAFavorite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IAFavoritesActivity : AppCompatActivity(), OnIaFavoriteClickListener {

    private lateinit var binding: ActivityIaFavoritosBinding
    private lateinit var adapter: IAFavoritoAdapter
    private val favoritos = mutableListOf<IAFavorite>()
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIaFavoritosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
        userId = prefs.getInt("usuarioIdActivo", -1)

        if (userId == -1) {
            Toast.makeText(this, "Error al obtener el usuario", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        cargarFavoritos()
    }

    private fun setupToolbar() {
        binding.toolbarTitle.text = "Planes IA Favoritos"
        binding.ivBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = IAFavoritoAdapter(favoritos, this)
        binding.rvFavoritos.layoutManager = LinearLayoutManager(this)
        binding.rvFavoritos.adapter = adapter
    }

    private fun cargarFavoritos() {
        lifecycleScope.launch {
            try {
                val resultado = withContext(Dispatchers.IO) {
                    RetrofitInstance.userService.obtenerFavoritos(userId)
                }
                favoritos.clear()
                favoritos.addAll(resultado)
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("IA_FAVORITOS", "Error al cargar favoritos", e)
                Toast.makeText(this@IAFavoritesActivity, "Error al cargar favoritos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onLongClick(favorito: IAFavorite) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Eliminar favorito")
        builder.setMessage("¿Quieres eliminar este plan de favoritos?")
        builder.setPositiveButton("Sí") { _, _ ->
            lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitInstance.userService.eliminarFavorito(favorito.id)
                    }

                    if (response.isSuccessful) {
                        adapter.eliminarFavorito(favorito)
                        Toast.makeText(this@IAFavoritesActivity, "Favorito eliminado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@IAFavoritesActivity, "No se pudo eliminar el favorito", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Log.e("IA_FAVORITOS", "Error al eliminar favorito", e)
                    Toast.makeText(this@IAFavoritesActivity, "Error al eliminar favorito", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }


}
