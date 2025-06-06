package com.danielescrich.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.danielescrich.myapplication.R
import com.danielescrich.myapplication.model.MessageChat

class ChatAdapter(private val mensajes: List<MessageChat>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TIPO_USUARIO = 0
        private const val TIPO_IA = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (mensajes[position].esUsuario) TIPO_USUARIO else TIPO_IA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == TIPO_USUARIO) {
            val view = layoutInflater.inflate(R.layout.item_message_user, parent, false)
            UsuarioViewHolder(view)
        } else {
            val view = layoutInflater.inflate(R.layout.item_message_ia, parent, false)
            IAViewHolder(view)
        }
    }

    override fun getItemCount(): Int = mensajes.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mensaje = mensajes[position]
        if (holder is UsuarioViewHolder) {
            holder.texto.text = mensaje.texto
        } else if (holder is IAViewHolder) {
            holder.texto.text = mensaje.texto
        }
    }

    class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val texto: TextView = view.findViewById(R.id.tvMensajeUsuario)
    }

    class IAViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val texto: TextView = view.findViewById(R.id.tvMensaje)
    }
}
