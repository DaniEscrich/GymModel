package com.danielescrich.myapplication.mainmodule

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.danielescrich.myapplication.databinding.ActivityPlanTrainingInCourseBinding
import com.danielescrich.myapplication.mainmodule.adapter.IAPlanAdapter
import com.danielescrich.myapplication.model.IAPlanItem
import com.danielescrich.myapplication.room.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlanTrainingInCourseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlanTrainingInCourseBinding
    private lateinit var adapter: IAPlanAdapter
    private val listaItems = mutableListOf<IAPlanItem>()
    private var userId: Int = -1
    private var planId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlanTrainingInCourseBinding.inflate(layoutInflater)
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
        binding.toolbarTitle.text = "Entrenamiento en curso"
        binding.ivBack.setOnClickListener { finish() }
    }

    private fun cargarPlanDesdeRoom() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@PlanTrainingInCourseActivity)
            val plan = withContext(Dispatchers.IO) {
                db.planEntrenamientoDao().obtenerPlanEnCurso(userId)
            }

            if (plan == null) {
                Toast.makeText(
                    this@PlanTrainingInCourseActivity,
                    "No tienes un plan activo",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@launch
            }

            planId = plan.id
            val items = withContext(Dispatchers.IO) {
                db.planEntrenamientoDao().obtenerItemsDePlan(plan.id)
            }

            listaItems.clear()
            var diaActual = ""
            for (item in items) {
                if (item.dia != diaActual) {
                    diaActual = item.dia
                    listaItems.add(IAPlanItem(texto = diaActual, esTitulo = true))
                }
                listaItems.add(IAPlanItem(texto = item.ejercicio, completado = item.completado))
            }

            adapter = IAPlanAdapter(listaItems, mostrarCheckbox = true) { pos, isChecked ->
                val item = listaItems[pos]
                if (!item.esTitulo) {
                    lifecycleScope.launch {
                        val dia = obtenerDiaDeItem(pos)
                        withContext(Dispatchers.IO) {
                            db.planEntrenamientoDao().actualizarItemManual(
                                planId = planId,
                                dia = dia,
                                ejercicio = item.texto,
                                completado = isChecked
                            )
                        }
                        binding.btnFinalizar.visibility =
                            if (todosCompletados()) View.VISIBLE else View.GONE
                    }
                }
            }

            binding.rvEntrenamiento.layoutManager =
                LinearLayoutManager(this@PlanTrainingInCourseActivity)
            binding.rvEntrenamiento.adapter = adapter

            binding.btnFinalizar.visibility = if (todosCompletados()) View.VISIBLE else View.GONE
        }
    }

    private fun marcarTodos(seleccionar: Boolean) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@PlanTrainingInCourseActivity)
            withContext(Dispatchers.IO) {
                var diaActual = ""
                listaItems.forEach { item ->
                    if (item.esTitulo) {
                        diaActual = item.texto
                    } else {
                        item.completado = seleccionar
                        db.planEntrenamientoDao().actualizarItemManual(
                            planId = planId,
                            dia = diaActual,
                            ejercicio = item.texto,
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
            val db = AppDatabase.getDatabase(this@PlanTrainingInCourseActivity)
            withContext(Dispatchers.IO) {
                db.planEntrenamientoDao().marcarPlanComoFinalizado(planId)
                db.planEntrenamientoDao().eliminarItemsDePlan(planId)
            }

            Toast.makeText(
                this@PlanTrainingInCourseActivity,
                "¡Plan finalizado!",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}
