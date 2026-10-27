package com.duoc.notix.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class ProcedureRecord(
    @Embedded val procedure: ProcedureEntity,
    @Relation(parentColumn = "userId", entityColumn = "id") val owner: UserEntity,
    @Relation(parentColumn = "id", entityColumn = "procedureId") val documents: List<DocumentEntity>,
    @Relation(parentColumn = "id", entityColumn = "procedureId") val events: List<EventEntity>,
)

@Dao
interface ProcedureDao {
    @Transaction
    @Query("SELECT * FROM procedures WHERE userId = :userId ORDER BY createdAt DESC, id DESC")
    fun observeForUser(userId: Long): Flow<List<ProcedureRecord>>

    @Transaction
    @Query("SELECT * FROM procedures WHERE status != 'DRAFT' ORDER BY CASE WHEN status = 'UNDER_REVIEW' THEN 0 ELSE 1 END, createdAt DESC, id DESC")
    fun observeInbox(): Flow<List<ProcedureRecord>>

    @Query("SELECT * FROM procedures WHERE id = :id")
    suspend fun find(id: Long): ProcedureEntity?

    @Query("SELECT * FROM documents WHERE procedureId = :id ORDER BY id")
    suspend fun documents(id: Long): List<DocumentEntity>

    @Insert suspend fun insert(procedure: ProcedureEntity): Long
    @Update suspend fun update(procedure: ProcedureEntity)
    @Insert suspend fun insertDocument(document: DocumentEntity): Long
    @Insert suspend fun insertEvent(event: EventEntity): Long

    @Query("DELETE FROM documents WHERE id = :documentId AND procedureId = :procedureId")
    suspend fun deleteDocument(procedureId: Long, documentId: Long): Int
}
