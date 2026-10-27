package com.duoc.notix.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "procedures",
    foreignKeys = [ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("userId")],
)
data class ProcedureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val createdAt: Long,
    @ColumnInfo(defaultValue = "''") val details: String = "",
    @ColumnInfo(defaultValue = "'SIMPLE'") val signatureType: String = "SIMPLE",
    @ColumnInfo(defaultValue = "0") val signatureConfirmed: Boolean = false,
    @ColumnInfo(defaultValue = "'DRAFT'") val status: String = "DRAFT",
)

@Entity(
    tableName = "documents",
    foreignKeys = [ForeignKey(entity = ProcedureEntity::class, parentColumns = ["id"], childColumns = ["procedureId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("procedureId")],
)
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val procedureId: Long, val name: String, val mimeType: String, val sizeBytes: Long,
    val fileUri: String? = null, val templateText: String? = null,
)

@Entity(
    tableName = "procedure_events",
    foreignKeys = [ForeignKey(entity = ProcedureEntity::class, parentColumns = ["id"], childColumns = ["procedureId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("procedureId")],
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val procedureId: Long, val status: String, val actorName: String, val actorRole: String,
    val note: String, val createdAt: Long,
)
