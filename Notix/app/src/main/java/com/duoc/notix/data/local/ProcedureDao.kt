package com.duoc.notix.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProcedureDao {
    @Query("SELECT * FROM procedures WHERE userId = :userId ORDER BY createdAt DESC, id DESC")
    fun observeForUser(userId: Long): Flow<List<ProcedureEntity>>

    @Insert
    suspend fun insert(procedure: ProcedureEntity): Long
}
