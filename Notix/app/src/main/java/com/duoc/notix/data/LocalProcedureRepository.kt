package com.duoc.notix.data

import com.duoc.notix.data.local.ProcedureDao
import com.duoc.notix.data.local.ProcedureEntity
import com.duoc.notix.domain.Procedure
import com.duoc.notix.domain.ProcedureRepository
import com.duoc.notix.domain.ProcedureType
import kotlinx.coroutines.flow.map

class LocalProcedureRepository(private val procedures: ProcedureDao) : ProcedureRepository {
    override fun observeForUser(userId: Long) = procedures.observeForUser(userId).map { entries ->
        entries.map { Procedure(it.id, it.title, it.createdAt) }
    }

    override suspend fun createDraft(userId: Long, type: ProcedureType) {
        procedures.insert(ProcedureEntity(userId = userId, title = type.label, createdAt = System.currentTimeMillis()))
    }
}
