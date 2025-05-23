package com.danielescrich.myapplication.mainmodule.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.danielescrich.myapplication.R
import com.danielescrich.myapplication.databinding.ItemClaseBinding
import com.danielescrich.myapplication.room.dao.ClaseUsuarioDao
import com.danielescrich.myapplication.room.dao.UsuarioDao
import com.danielescrich.myapplication.room.entity.ClaseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ClaseAdapter(
    private val listener: OnClickListener,
    private val semanaActual: Int,
    private val claseUsuarioDao: ClaseUsuarioDao,
    private val usuarioDao: UsuarioDao
) : ListAdapter<ClaseEntity, ClaseAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemClaseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(clase: ClaseEntity, position: Int) {
            binding.tvClaseTitulo.text = "${clase.titulo.uppercase()} | ${clase.hora} a ${sumarUnaHora(clase.hora)}"
            binding.tvClaseApuntados.text = "${clase.apuntados}/${clase.maxUsuarios}"

            binding.layoutUsuarios.removeAllViews()

            CoroutineScope(Dispatchers.Main).launch {
                val nombresUsuarios = withContext(Dispatchers.IO) {
                    val ids = claseUsuarioDao.getUsuariosPorClaseYSemana(clase.id, semanaActual)
                    ids.mapNotNull { id ->
                        usuarioDao.getNombreUsuarioPorId(id) ?: "Desconocido"
                    }
                }

                nombresUsuarios.forEach { nombre ->
                    val layout = LinearLayout(binding.root.context).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(12, 0, 12, 0) }
                    }

                    val image = ImageView(binding.root.context).apply {
                        setImageResource(R.drawable.ic_user)
                        layoutParams = LinearLayout.LayoutParams(100, 100)
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }

                    val name = TextView(binding.root.context).apply {
                        text = nombre
                        textSize = 12f
                        gravity = Gravity.CENTER
                    }

                    layout.addView(image)
                    layout.addView(name)
                    binding.layoutUsuarios.addView(layout)
                }
            }

            // Botón apuntarse
            val isLleno = clase.apuntados >= clase.maxUsuarios
            val esSemanaActual = clase.semana == semanaActual
            val esDiaPasado = if (esSemanaActual) {
                val hoy = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                val map = mapOf(
                    "Lun" to Calendar.MONDAY, "Mar" to Calendar.TUESDAY, "Mie" to Calendar.WEDNESDAY,
                    "Jue" to Calendar.THURSDAY, "Vie" to Calendar.FRIDAY,
                    "Sab" to Calendar.SATURDAY, "Dom" to Calendar.SUNDAY
                )
                map[clase.diaSemana]?.let { it < hoy } ?: false
            } else false

            val puedeApuntarse = !isLleno && esSemanaActual && !esDiaPasado
            binding.btnApuntarse.isEnabled = puedeApuntarse
            binding.btnApuntarse.alpha = if (puedeApuntarse) 1.0f else 0.5f

            binding.btnApuntarse.setOnClickListener {
                if (puedeApuntarse) {
                    listener.onApuntarse(clase, position)
                }
            }

            binding.root.setOnClickListener {
                listener.onClick(clase, position)
            }

            binding.root.setOnLongClickListener {
                if (puedeApuntarse) {
                    listener.onLongClick(clase, position)
                }
                true
            }
        }

        private fun sumarUnaHora(hora: String): String {
            return try {
                val (h, m) = hora.split(":").map { it.toInt() }
                val nuevaHora = (h + 1) % 24
                "%02d:%02d".format(nuevaHora, m)
            } catch (e: Exception) {
                hora
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ClaseEntity>() {
            override fun areItemsTheSame(oldItem: ClaseEntity, newItem: ClaseEntity): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ClaseEntity, newItem: ClaseEntity): Boolean =
                oldItem == newItem
        }
    }
}
