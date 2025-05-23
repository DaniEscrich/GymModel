package com.danielescrich.myapplication.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.danielescrich.myapplication.room.dao.ClaseDao
import com.danielescrich.myapplication.room.dao.ClaseUsuarioDao
import com.danielescrich.myapplication.room.dao.UsuarioDao
import com.danielescrich.myapplication.room.entity.ClaseEntity
import com.danielescrich.myapplication.room.entity.ClaseUsuarioEntity
import com.danielescrich.myapplication.room.entity.UsuarioRoomEntity

@Database(
    entities = [ClaseEntity::class, ClaseUsuarioEntity::class, UsuarioRoomEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun claseDao(): ClaseDao
    abstract fun claseUsuarioDao(): ClaseUsuarioDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gymmodel_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
