package com.danielescrich.myapplication.mainmodule.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.danielescrich.myapplication.databinding.ItemIaFavoriteBinding
import com.danielescrich.myapplication.retrofit.data.IAFavorite

class IAFavoriteAdapter(
    private val lista: MutableList<IAFavorite>,
    private val listener: OnIaFavoriteClickListener
) : RecyclerView.Adapter<IAFavoriteAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemIaFavoriteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(favorito: IAFavorite) {
            val tipoTexto = if (favorito.contenido.contains("Desayuno", ignoreCase = true) ||
                favorito.contenido.contains("Comida", ignoreCase = true)
            ) "🍽️ Comida" else "🏋️ Entrenamiento"
            binding.tvTipo.text = tipoTexto

            binding.tvFecha.text = "📅 ${favorito.fechaGuardado}"

            binding.tvContenido.text = formatearTexto(favorito.contenido)

            binding.root.setOnLongClickListener {
                listener.onLongClick(favorito)
                true
            }

            binding.root.setOnClickListener {
                listener.onMarcarComoPlanEnCurso(favorito)
            }
        }

        private fun formatearTexto(texto: String): CharSequence {
            val builder = SpannableStringBuilder()
            val lineas = texto.lines()

            for (linea in lineas) {
                val textoLimpio = linea.trim()
                if (textoLimpio.uppercase().startsWith("DÍA")) {
                    val inicio = builder.length
                    builder.append(textoLimpio).append("\n\n")
                    builder.setSpan(StyleSpan(Typeface.BOLD), inicio, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    builder.setSpan(ForegroundColorSpan(Color.RED), inicio, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else if (textoLimpio.startsWith("-")) {
                    builder.append(textoLimpio).append("\n")
                } else {
                    builder.append(textoLimpio).append("\n")
                }
            }

            return builder
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIaFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    fun eliminarFavorito(favorito: IAFavorite) {
        val index = lista.indexOf(favorito)
        if (index != -1) {
            lista.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
