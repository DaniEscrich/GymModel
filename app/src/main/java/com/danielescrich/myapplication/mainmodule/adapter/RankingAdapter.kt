package com.danielescrich.myapplication.mainmodule.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.danielescrich.myapplication.databinding.ItemRankingBinding
import com.danielescrich.myapplication.retrofit.data.RankingUser

class RankingAdapter : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    private var rankingList: List<RankingUser> = listOf()
    private var tipoFiltro: Int = 0

    fun submitList(list: List<RankingUser>, filtro: Int) {
        rankingList = list
        tipoFiltro = filtro
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val binding = ItemRankingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RankingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        holder.bind(rankingList[position], tipoFiltro)
    }

    override fun getItemCount(): Int = rankingList.size

    class RankingViewHolder(private val binding: ItemRankingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: RankingUser, filtro: Int) {
            binding.tvNombreUsuario.text = user.nombreUsuario
            binding.tvValor.text = when (filtro) {
                0 -> "${user.pesoPerdido ?: 0.0} kg"
                1 -> "${user.diasDesdeRegistro} días"
                2 -> "${user.progresosRegistrados} progresos"
                else -> ""
            }
        }
    }
}
