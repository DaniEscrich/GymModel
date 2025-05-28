package com.danielescrich.myapplication.mainmodule.adapter

import com.danielescrich.myapplication.retrofit.data.IAFavorite

interface OnIaFavoriteClickListener {
    fun onLongClick(favorito: IAFavorite)
}