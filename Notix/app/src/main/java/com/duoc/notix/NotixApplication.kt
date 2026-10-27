package com.duoc.notix

import android.app.Application
import androidx.room.Room
import com.duoc.notix.data.AndroidDocumentStore
import com.duoc.notix.data.LocalAuthRepository
import com.duoc.notix.data.LocalProcedureRepository
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.domain.AuthRepository
import com.duoc.notix.domain.DocumentStore
import com.duoc.notix.domain.ProcedureRepository

class NotixApplication : Application() {
    private val database by lazy {
        Room.databaseBuilder(applicationContext, NotixDatabase::class.java, "notix.db")
            .addMigrations(NotixDatabase.MIGRATION_1_2).build()
    }
    val authRepository: AuthRepository by lazy { LocalAuthRepository(database.userDao()) }
    val procedureRepository: ProcedureRepository by lazy { LocalProcedureRepository(database) }
    val documentStore: DocumentStore by lazy { AndroidDocumentStore(applicationContext) }
}
