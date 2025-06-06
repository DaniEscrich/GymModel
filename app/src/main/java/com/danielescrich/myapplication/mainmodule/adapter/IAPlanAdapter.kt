package com.danielescrich.myapplication.mainmodule.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.danielescrich.myapplication.R
import com.danielescrich.myapplication.model.IAPlanItem

class IAPlanAdapter(
    private val items: List<IAPlanItem>,
    private val mostrarCheckbox: Boolean,
    private val onCheckedChange: ((Int, Boolean) -> Unit)? = null
) : RecyclerView.Adapter<IAPlanAdapter.PlanViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ia_plan, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, position)
    }

    override fun getItemCount(): Int = items.size

    inner class PlanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTexto: TextView = view.findViewById(R.id.tvTexto)
        private val checkbox: CheckBox = view.findViewById(R.id.checkbox)
        private val layoutRoot: ConstraintLayout = view as ConstraintLayout

        fun bind(item: IAPlanItem, position: Int) {
            tvTexto.text = item.texto

            if (item.esTitulo) {
                tvTexto.textSize = 17f
                tvTexto.setTextColor(layoutRoot.context.getColor(android.R.color.holo_red_dark))
                tvTexto.setPadding(4, 8, 4, 8)
                tvTexto.typeface =
                    ResourcesCompat.getFont(layoutRoot.context, R.font.montserrat_bold)
                checkbox.visibility = View.GONE
            } else {
                tvTexto.textSize = 15f
                tvTexto.setTextColor(layoutRoot.context.getColor(R.color.black))
                checkbox.visibility = if (mostrarCheckbox) View.VISIBLE else View.GONE
                checkbox.setOnCheckedChangeListener(null)
                checkbox.isChecked = item.completado

                tvTexto.paintFlags = if (item.completado) {
                    tvTexto.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    tvTexto.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }

                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    item.completado = isChecked
                    onCheckedChange?.invoke(position, isChecked)

                    tvTexto.paintFlags = if (isChecked) {
                        tvTexto.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    } else {
                        tvTexto.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    }
                }
            }
        }
    }
}
