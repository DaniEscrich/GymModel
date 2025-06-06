package com.danielescrich.myapplication.mainmodule.adapter

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.danielescrich.myapplication.R
import com.danielescrich.myapplication.databinding.ItemDataProgressBinding

class ProgressCardAdapter(
    private val datos: List<Pair<String, String>>,
    private val onEditarPesoClick: () -> Unit,
    private val onEditarPesoObjetivoClick: () -> Unit
) : RecyclerView.Adapter<ProgressCardAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemDataProgressBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemDataProgressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = datos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (titulo, descripcion) = datos[position]
        holder.binding.tvTitulo.text = titulo
        holder.binding.tvDescripcion.text = descripcion

        val context = holder.itemView.context
        val (iconRes, colorRes) = when (titulo) {
            "Peso objetivo" -> R.drawable.ic_check to R.color.color_error
            "Peso actual" -> R.drawable.ic_peso to R.color.color_primary
            "Peso anterior" -> R.drawable.ic_peso to R.color.color_accent
            "Diferencia" -> R.drawable.ic_flecha_arriba to R.color.color_success
            "IMC" -> R.drawable.ic_info to R.color.color_first
            "Clases asistidas" -> R.drawable.ic_calendario to R.color.color_third
            "Día de inicio" -> R.drawable.ic_reloj to R.color.progreso_clases_icono
            "Última actualización" -> R.drawable.ic_refresh to R.color.color_on_background
            else -> R.drawable.ic_info to R.color.color_on_background
        }

        holder.binding.ivIcono.setImageResource(iconRes)
        holder.binding.ivIcono.setColorFilter(
            ContextCompat.getColor(context, colorRes),
            PorterDuff.Mode.SRC_IN
        )

        when (titulo) {
            "Peso actual" -> {
                holder.binding.ivEditarPeso.visibility = View.VISIBLE
                holder.binding.ivEditarPeso.setOnClickListener {
                    onEditarPesoClick()
                }
            }

            "Peso objetivo", "Establecer peso objetivo ✏️" -> {
                holder.binding.ivEditarPeso.visibility = View.VISIBLE
                holder.binding.ivEditarPeso.setOnClickListener {
                    onEditarPesoObjetivoClick()
                }
            }

            else -> {
                holder.binding.ivEditarPeso.visibility = View.GONE
            }
        }
    }
}
