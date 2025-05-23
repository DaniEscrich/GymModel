package com.danielescrich.myapplication.mainmodule.adapter

import com.danielescrich.myapplication.room.entity.ClaseEntity

interface OnClickListener {
    fun onClick(clase: ClaseEntity, position: Int)
    fun onLongClick(clase: ClaseEntity, position: Int)
    fun onApuntarse(clase: ClaseEntity, position: Int)
}
