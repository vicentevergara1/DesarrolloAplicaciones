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
    version = 3,
    exportSchema = true
)
@TypeConverters(NotixTypeConverters::class)
abstract class NotixDatabase : RoomDatabase() {

    abstract fun tramiteDao(): TramiteDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun avisoDao(): AvisoDao

    companion object {
        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE documentos ADD COLUMN rutaLocal TEXT")
                db.execSQL("ALTER TABLE documentos ADD COLUMN mime TEXT")
                db.execSQL("ALTER TABLE tramites ADD COLUMN firmaConfirmada INTEGER NOT NULL DEFAULT 0")
            }
        }

        @Volatile
        private var INSTANCE: NotixDatabase? = null

        fun getDatabase(context: Context): NotixDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotixDatabase::class.java,
                    "notix_database"
                )
                .addMigrations(MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
