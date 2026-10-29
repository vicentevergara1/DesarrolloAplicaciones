package com.duoc.notix.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UsuarioEntity::class,
        TramiteEntity::class,
        DocumentoEntity::class,
        EventoHistorialEntity::class,
        AvisoEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(NotixTypeConverters::class)
abstract class NotixDatabase : RoomDatabase() {

    abstract fun tramiteDao(): TramiteDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun avisoDao(): AvisoDao

    companion object {
        @Volatile
        private var INSTANCE: NotixDatabase? = null

        fun getDatabase(context: Context): NotixDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotixDatabase::class.java,
                    "notix_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
