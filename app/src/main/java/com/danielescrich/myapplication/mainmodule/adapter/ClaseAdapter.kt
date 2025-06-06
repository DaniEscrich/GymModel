package com.danielescrich.myapplication.mainmodule.adapter

import android.graphics.BitmapFactory
import android.util.Base64
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
import com.danielescrich.myapplication.retrofit.RetrofitInstance
import com.danielescrich.myapplication.room.dao.ClaseUserDao
import com.danielescrich.myapplication.room.dao.UserDao
import com.danielescrich.myapplication.room.entity.ClaseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ClaseAdapter(
    private val listener: OnClickListener,
    private val semanaActual: Int,
    private val claseUserDao: ClaseUserDao,
    private val userDao: UserDao
) : ListAdapter<ClaseEntity, ClaseAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemClaseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(clase: ClaseEntity, position: Int) {
            binding.tvClaseTitulo.text =
                "${clase.titulo.uppercase()} | ${clase.hora} a ${sumarUnaHora(clase.hora)}"
            binding.tvClaseApuntados.text = "${clase.apuntados}/${clase.maxUsuarios}"

            binding.layoutUsuarios.removeAllViews()

            CoroutineScope(Dispatchers.Main).launch {
                val usuarios = withContext(Dispatchers.IO) {
                    val ids = claseUserDao.getUsuariosPorClaseYSemana(clase.id, semanaActual)
                    ids.mapNotNull { id ->
                        val nombre = userDao.getNombreUsuarioPorId(id)
                        nombre?.let { Pair(id, it) }
                    }
                }

                usuarios.forEach { (userId, nombre) ->
                    val layout = LinearLayout(binding.root.context).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(12, 0, 12, 0) }
                    }

                    val image = ImageView(binding.root.context).apply {
                        layoutParams = LinearLayout.LayoutParams(100, 100)
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        setImageResource(R.drawable.ic_user)
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response =
                                RetrofitInstance.userService.obtenerImagenPerfil(userId.toInt())
                            if (response.isSuccessful) {
                                response.body()?.imagenBase64?.let { base64 ->
                                    val decoded = Base64.decode(base64, Base64.DEFAULT)
                                    val bitmap =
                                        BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                                    withContext(Dispatchers.Main) {
                                        image.setImageBitmap(bitmap)
                                    }
                                }
                            }
                        } catch (_: Exception) {
                        }
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

            val isLleno = clase.apuntados >= clase.maxUsuarios
            val esSemanaActual = clase.semana == semanaActual
            val esDiaPasado = if (esSemanaActual) {
                val hoy = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                val map = mapOf(
                    "Lun" to Calendar.MONDAY,
                    "Mar" to Calendar.TUESDAY,
                    "Mie" to Calendar.WEDNESDAY,
                    "Jue" to Calendar.THURSDAY,
                    "Vie" to Calendar.FRIDAY,
                    "Sab" to Calendar.SATURDAY,
                    "Dom" to Calendar.SUNDAY
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
