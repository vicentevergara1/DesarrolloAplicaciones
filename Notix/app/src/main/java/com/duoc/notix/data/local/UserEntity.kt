package com.duoc.notix.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val displayName: String,
    val email: String,
    val passwordHash: ByteArray,
    val passwordSalt: ByteArray,
    @ColumnInfo(defaultValue = "'PERSON'") val accountType: String = "PERSON",
    @ColumnInfo(defaultValue = "'{}'") val profileJson: String = "{}",
)
