package com.danielescrich.myapplication.mainmodule

import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.danielescrich.myapplication.R
import com.danielescrich.myapplication.databinding.ActivityIaFavoritesBinding
import com.danielescrich.myapplication.mainmodule.adapter.IAFavoriteAdapter
import com.danielescrich.myapplication.mainmodule.adapter.OnIaFavoriteClickListener
import com.danielescrich.myapplication.retrofit.RetrofitInstance
import com.danielescrich.myapplication.retrofit.data.IAFavorite
import com.danielescrich.myapplication.room.database.AppDatabase
import com.danielescrich.myapplication.room.entity.ItemNutritionEntity
import com.danielescrich.myapplication.room.entity.PlanNutritionEntity
import com.danielescrich.myapplication.room.entity.PlanDayItemEntity
import com.danielescrich.myapplication.room.entity.PlanTrainingEntity
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IAFavoritesActivity : AppCompatActivity(), OnIaFavoriteClickListener {

    private lateinit var binding: ActivityIaFavoritesBinding
    private lateinit var adapter: IAFavoriteAdapter
    private val favoritos = mutableListOf<IAFavorite>()
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIaFavoritesBinding.inflate(layoutInflater)
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
        adapter = IAFavoriteAdapter(favoritos, this)
        binding.rvFavoritos.layoutManager = LinearLayoutManager(this)
        binding.rvFavoritos.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val favorito = favoritos[position]

                when (direction) {
                    ItemTouchHelper.LEFT -> eliminarFavorito(favorito)
                    ItemTouchHelper.RIGHT -> {
                        if (favorito.contenido.contains("comida", ignoreCase = true) ||
                            favorito.contenido.contains("nutrición", ignoreCase = true)
                        ) {
                            guardarPlanEnCursoComida(favorito)
                        } else {
                            guardarPlanEnCurso(favorito)
                        }
                        adapter.notifyItemChanged(position)
                    }
                }
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(Color.RED)
                    .addSwipeLeftActionIcon(R.drawable.ic_delete)
                    .addSwipeLeftLabel("Eliminar")
                    .setSwipeLeftLabelColor(Color.WHITE)
                    .addSwipeRightBackgroundColor(Color.parseColor("#4CAF50"))
                    .addSwipeRightActionIcon(R.drawable.ic_check)
                    .addSwipeRightLabel("En curso")
                    .setSwipeRightLabelColor(Color.WHITE)
                    .create()
                    .decorate()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.rvFavoritos)
    }

    private fun eliminarFavorito(favorito: IAFavorite) {
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
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("IA_FAVORITOS", "Error al eliminar favorito", e)
                Toast.makeText(this@IAFavoritesActivity, "Error al eliminar favorito", Toast.LENGTH_SHORT).show()
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onLongClick(favorito: IAFavorite) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Eliminar favorito")
        builder.setMessage("¿Quieres eliminar este plan de favoritos?")
        builder.setPositiveButton("Sí") { _, _ -> eliminarFavorito(favorito) }
        builder.setNegativeButton("Cancelar") { _, _ -> adapter.notifyDataSetChanged() }
        builder.show()
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

    override fun onMarcarComoPlanEnCurso(favorito: IAFavorite) {
        guardarPlanEnCurso(favorito)
    }

    private fun guardarPlanEnCurso(favorito: IAFavorite) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@IAFavoritesActivity)

            withContext(Dispatchers.IO) {
                db.planEntrenamientoDao().finalizarPlanesDelUsuario(userId)
            }

            val planId = withContext(Dispatchers.IO) {
                db.planEntrenamientoDao().insertarPlan(
                    PlanTrainingEntity(
                        userId = userId,
                        fechaCreacion = System.currentTimeMillis().toString(),
                        enCurso = true
                    )
                )
            }

            val items = mutableListOf<PlanDayItemEntity>()
            val lineas = favorito.contenido.lines()
            var diaActual = ""

            for (linea in lineas) {
                if (linea.trim().startsWith("Día", ignoreCase = true)) {
                    diaActual = linea.trim()
                } else if (linea.isNotBlank()) {
                    items.add(
                        PlanDayItemEntity(
                            planId = planId.toInt(),
                            dia = diaActual,
                            ejercicio = linea.trim(),
                            completado = false
                        )
                    )
                }
            }

            withContext(Dispatchers.IO) {
                db.planEntrenamientoDao().insertarItems(items)
            }

            Toast.makeText(this@IAFavoritesActivity, "Plan guardado correctamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarPlanEnCursoComida(favorito: IAFavorite) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@IAFavoritesActivity)

            withContext(Dispatchers.IO) {
                db.planComidaDao().finalizarPlanesDelUsuario(userId)
            }

            val planId = withContext(Dispatchers.IO) {
                db.planComidaDao().insertarPlan(
                    PlanNutritionEntity(
                        userId = userId,
                        fechaCreacion = System.currentTimeMillis().toString(),
                        finalizado = false
                    )
                )
            }

            val items = mutableListOf<ItemNutritionEntity>()
            val lineas = favorito.contenido.lines()
            var diaActual = ""

            for (linea in lineas) {
                if (linea.trim().startsWith("Día", ignoreCase = true)) {
                    diaActual = linea.trim()
                } else if (linea.isNotBlank()) {
                    items.add(
                        ItemNutritionEntity(
                            planId = planId.toInt(),
                            dia = diaActual,
                            comida = linea.trim(),
                            completado = false
                        )
                    )
                }
            }

            withContext(Dispatchers.IO) {
                db.planComidaDao().insertarItems(items)
            }

            Toast.makeText(this@IAFavoritesActivity, "Plan de comida guardado correctamente", Toast.LENGTH_SHORT).show()
        }
    }
}
