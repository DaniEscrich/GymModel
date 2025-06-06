package com.danielescrich.myapplication.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.danielescrich.myapplication.room.dao.*
import com.danielescrich.myapplication.room.entity.*

@Database(
    entities = [ClaseEntity::class, ClaseUserEntity::class, UsuarioRoomEntity::class, PlanTrainingEntity::class, PlanDayItemEntity::class, PlanNutritionEntity::class, ItemNutritionEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun claseDao(): ClaseDao
    abstract fun claseUsuarioDao(): ClaseUserDao
    abstract fun usuarioDao(): UserDao
    abstract fun planEntrenamientoDao(): PlanEntrenamientoDao
    abstract fun planComidaDao(): PlanNutritionDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gymmodel_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
