package com.duoc.notix.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserEntity::class, ProcedureEntity::class], version = 1, exportSchema = true)
abstract class NotixDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun procedureDao(): ProcedureDao
}
