package com.duoc.notix.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [UserEntity::class, ProcedureEntity::class, DocumentEntity::class, EventEntity::class],
    version = 2, exportSchema = true,
)
abstract class NotixDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun procedureDao(): ProcedureDao

    companion object {
        /** Conserva las cuentas y borradores de la versión anterior; no borra la base. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN accountType TEXT NOT NULL DEFAULT 'PERSON'")
                db.execSQL("ALTER TABLE users ADD COLUMN profileJson TEXT NOT NULL DEFAULT '{}'")
                db.execSQL("ALTER TABLE procedures ADD COLUMN details TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE procedures ADD COLUMN signatureType TEXT NOT NULL DEFAULT 'SIMPLE'")
                db.execSQL("ALTER TABLE procedures ADD COLUMN signatureConfirmed INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE procedures ADD COLUMN status TEXT NOT NULL DEFAULT 'DRAFT'")
                db.execSQL("CREATE TABLE IF NOT EXISTS documents (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, procedureId INTEGER NOT NULL, name TEXT NOT NULL, mimeType TEXT NOT NULL, sizeBytes INTEGER NOT NULL, fileUri TEXT, templateText TEXT, FOREIGN KEY(procedureId) REFERENCES procedures(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_documents_procedureId ON documents(procedureId)")
                db.execSQL("CREATE TABLE IF NOT EXISTS procedure_events (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, procedureId INTEGER NOT NULL, status TEXT NOT NULL, actorName TEXT NOT NULL, actorRole TEXT NOT NULL, note TEXT NOT NULL, createdAt INTEGER NOT NULL, FOREIGN KEY(procedureId) REFERENCES procedures(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_procedure_events_procedureId ON procedure_events(procedureId)")
                db.execSQL("INSERT INTO procedure_events (procedureId, status, actorName, actorRole, note, createdAt) SELECT p.id, 'DRAFT', u.displayName, 'PERSON', 'Borrador anterior conservado.', p.createdAt FROM procedures p JOIN users u ON u.id = p.userId")
            }
        }
    }
}
