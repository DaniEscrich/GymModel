package com.danielescrich.myapplication.mainmodule

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.danielescrich.myapplication.databinding.ActivityPlanNutritionInCourseBinding
import com.danielescrich.myapplication.mainmodule.adapter.IAPlanAdapter
import com.danielescrich.myapplication.model.IAPlanItem
import com.danielescrich.myapplication.room.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlanNutritionInCourseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlanNutritionInCourseBinding
    private lateinit var adapter: IAPlanAdapter
    private val listaItems = mutableListOf<IAPlanItem>()
    private var userId: Int = -1
    private var planId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlanNutritionInCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        val prefs = getSharedPreferences("gymmodel_prefs", MODE_PRIVATE)
        userId = prefs.getInt("usuarioIdActivo", -1)
        if (userId == -1) {
            finish()
            return
        }

        cargarPlanDesdeRoom()

        binding.btnFinalizar.setOnClickListener { finalizarPlan() }

        binding.checkSeleccionarTodo.setOnCheckedChangeListener { _, isChecked ->
            marcarTodos(isChecked)
        }
    }

    private fun setupToolbar() {
        binding.toolbarTitle.text = "Comida en curso"
        binding.ivBack.setOnClickListener { finish() }
    }

    private fun cargarPlanDesdeRoom() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@PlanNutritionInCourseActivity)
            val plan = withContext(Dispatchers.IO) {
                db.planComidaDao().obtenerPlanEnCurso(userId)
            }

            if (plan == null) {
                Toast.makeText(
                    this@PlanNutritionInCourseActivity,
                    "No tienes un plan activo",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@launch
            }

            planId = plan.id
            val items = withContext(Dispatchers.IO) {
                db.planComidaDao().obtenerItemsDePlan(plan.id)
            }

            listaItems.clear()
            var diaActual = ""
            for (item in items) {
                if (item.dia != diaActual) {
                    diaActual = item.dia
                    listaItems.add(IAPlanItem(texto = diaActual, esTitulo = true))
                }
                listaItems.add(IAPlanItem(texto = item.comida, completado = item.completado))
            }

            adapter = IAPlanAdapter(listaItems, mostrarCheckbox = true) { pos, isChecked ->
                val item = listaItems[pos]
                if (!item.esTitulo) {
                    lifecycleScope.launch {
                        val dia = obtenerDiaDeItem(pos)
                        withContext(Dispatchers.IO) {
                            db.planComidaDao().actualizarItemManual(
                                planId = planId,
                                dia = dia,
                                comida = item.texto,
                                completado = isChecked
                            )
                        }
                        binding.btnFinalizar.visibility =
                            if (todosCompletados()) View.VISIBLE else View.GONE
                    }
                }
            }

            binding.rvComida.layoutManager = LinearLayoutManager(this@PlanNutritionInCourseActivity)
            binding.rvComida.adapter = adapter

            binding.btnFinalizar.visibility = if (todosCompletados()) View.VISIBLE else View.GONE
        }
    }

    private fun marcarTodos(seleccionar: Boolean) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@PlanNutritionInCourseActivity)
            withContext(Dispatchers.IO) {
                var diaActual = ""
                listaItems.forEach { item ->
                    if (item.esTitulo) {
                        diaActual = item.texto
                    } else {
                        item.completado = seleccionar
                        db.planComidaDao().actualizarItemManual(
                            planId = planId,
                            dia = diaActual,
                            comida = item.texto,
                            completado = seleccionar
                        )
                    }
                }
            }

            adapter.notifyDataSetChanged()
            binding.btnFinalizar.visibility = if (todosCompletados()) View.VISIBLE else View.GONE
        }
    }

    private fun obtenerDiaDeItem(pos: Int): String {
        for (i in pos downTo 0) {
            val item = listaItems[i]
            if (item.esTitulo) return item.texto
        }
        return "Día desconocido"
    }

    private fun todosCompletados(): Boolean {
        return listaItems.all { it.esTitulo || it.completado }
    }

    private fun finalizarPlan() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@PlanNutritionInCourseActivity)
            withContext(Dispatchers.IO) {
                db.planComidaDao().marcarPlanComoFinalizado(planId)
                db.planComidaDao().eliminarItemsDePlan(planId)
            }

            Toast.makeText(
                this@PlanNutritionInCourseActivity,
                "¡Plan finalizado!",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}
