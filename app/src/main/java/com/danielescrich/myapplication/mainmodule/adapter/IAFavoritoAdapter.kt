package com.danielescrich.myapplication.mainmodule.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.danielescrich.myapplication.R
import com.danielescrich.myapplication.retrofit.data.IAFavorite

class IAFavoritoAdapter(
    private val favoritos: MutableList<IAFavorite>,
    private val listener: OnIaFavoriteClickListener
) : RecyclerView.Adapter<IAFavoritoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvContenido: TextView = view.findViewById(R.id.tvContenido)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)

        init {
            view.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onLongClick(favoritos[position])
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ia_favorito, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val favorito = favoritos[position]
        holder.tvContenido.text = "🤖 ${favorito.contenido}"
        holder.tvFecha.text = "📅 ${favorito.fechaGuardado}"
    }

    override fun getItemCount(): Int = favoritos.size

    fun eliminarFavorito(favorito: IAFavorite) {
        val index = favoritos.indexOfFirst { it.id == favorito.id }
        if (index != -1) {
            favoritos.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
