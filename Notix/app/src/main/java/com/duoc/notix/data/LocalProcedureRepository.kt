package com.duoc.notix.data

import androidx.room.withTransaction
import com.duoc.notix.data.local.*
import com.duoc.notix.domain.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class LocalProcedureRepository(private val database: NotixDatabase) : ProcedureRepository {
    private val dao get() = database.procedureDao()

    private suspend fun verifyActor(actor: User) {
        val saved = database.userDao().findById(actor.id) ?: throw DomainException("La cuenta ya no está disponible.")
        if (saved.toUser() != actor) throw DomainException("La sesión no corresponde a la cuenta guardada.")
    }

    override fun observe(actor: User): Flow<List<Procedure>> = flow {
        verifyActor(actor)
        val records = if (actor.type == AccountType.NOTARY) dao.observeInbox() else dao.observeForUser(actor.id)
        emitAll(records.map { list -> list.map { it.toDomain() } })
    }

    override suspend fun createDraft(actor: User, type: ProcedureType): Long = database.withTransaction {
        verifyActor(actor)
        if (actor.type == AccountType.NOTARY) throw DomainException("El perfil Notario revisa trámites enviados.")
        val now = System.currentTimeMillis()
        val id = dao.insert(ProcedureEntity(userId = actor.id, title = type.label, createdAt = now))
        dao.insertDocument(DocumentEntity(procedureId = id, name = type.fileName, mimeType = "text/plain",
            sizeBytes = type.template.toByteArray(Charsets.UTF_8).size.toLong(), templateText = type.template))
        event(id, ProcedureStatus.DRAFT, actor, "Trámite ficticio creado con un formato de prueba.", now)
        id
    }

    private suspend fun editable(actor: User, id: Long): ProcedureEntity {
        verifyActor(actor)
        val item = dao.find(id) ?: throw DomainException("El trámite ya no está disponible.")
        if (actor.type == AccountType.NOTARY || item.userId != actor.id) throw DomainException("Solo el solicitante puede editar este trámite.")
        if (!ProcedureStatus.valueOf(item.status).editable) throw DomainException("El trámite no admite cambios en su estado actual.")
        return item
    }

    override suspend fun save(actor: User, id: Long, form: ProcedureForm, submit: Boolean) = database.withTransaction {
        val item = editable(actor, id)
        val errors = ProcedureRules.errors(form, dao.documents(id).size, submit)
        if (errors.isNotEmpty()) throw DomainException(errors.values.first())
        val status = if (submit) ProcedureStatus.UNDER_REVIEW else ProcedureStatus.valueOf(item.status)
        dao.update(item.copy(details = form.details.trim(), signatureType = form.signature.name,
            signatureConfirmed = form.signatureConfirmed, status = status.name))
        if (submit) event(id, status, actor, if (item.status == ProcedureStatus.OBSERVED.name) "Correcciones reenviadas a revisión." else "Documentación enviada a revisión.")
    }

    override suspend fun attach(actor: User, id: Long, document: Document) = database.withTransaction {
        editable(actor, id)
        if (dao.documents(id).size >= 5) throw DomainException("Puedes asociar hasta cinco documentos por trámite.")
        dao.insertDocument(DocumentEntity(procedureId = id, name = document.name, mimeType = document.mimeType,
            sizeBytes = document.sizeBytes, fileUri = document.fileUri, templateText = document.templateText))
        event(id, ProcedureStatus.valueOf(dao.find(id)!!.status), actor, "Documento agregado: " + document.name)
        Unit
    }

    override suspend fun removeDocument(actor: User, id: Long, documentId: Long) = database.withTransaction {
        val item = editable(actor, id)
        val document = dao.documents(id).firstOrNull { it.id == documentId } ?: throw DomainException("El documento ya no está disponible.")
        dao.deleteDocument(id, documentId)
        event(id, ProcedureStatus.valueOf(item.status), actor, "Documento retirado: " + document.name)
    }

    private suspend fun reviewable(actor: User, id: Long): ProcedureEntity {
        verifyActor(actor)
        val item = dao.find(id) ?: throw DomainException("El trámite ya no está disponible.")
        if (actor.type != AccountType.NOTARY) throw DomainException("Esta acción requiere el perfil Notario de prueba.")
        if (item.status != ProcedureStatus.UNDER_REVIEW.name) throw DomainException("Solo se revisan trámites en revisión.")
        return item
    }

    override suspend fun returnForCorrection(actor: User, id: Long, note: String) = database.withTransaction {
        val item = reviewable(actor, id)
        if (note.trim().length !in 5..1000) throw DomainException("Escribe una observación de entre 5 y 1000 caracteres.")
        dao.update(item.copy(status = ProcedureStatus.OBSERVED.name))
        event(id, ProcedureStatus.OBSERVED, actor, note.trim())
    }

    override suspend fun complete(actor: User, id: Long) = database.withTransaction {
        val item = reviewable(actor, id)
        dao.update(item.copy(status = ProcedureStatus.COMPLETED.name))
        event(id, ProcedureStatus.COMPLETED, actor, "Revisión de prueba finalizada. Sin validez legal.")
    }

    private suspend fun event(id: Long, status: ProcedureStatus, actor: User, note: String, time: Long = System.currentTimeMillis()) {
        dao.insertEvent(EventEntity(procedureId = id, status = status.name, actorName = actor.displayName,
            actorRole = actor.type.name, note = note, createdAt = time))
    }
}

private fun ProcedureRecord.toDomain(): Procedure = Procedure(
    id = procedure.id, userId = procedure.userId, title = procedure.title, createdAt = procedure.createdAt,
    ownerName = owner.displayName, ownerEmail = owner.email, ownerType = AccountType.valueOf(owner.accountType),
    ownerProfile = decodeProfile(owner.profileJson), status = ProcedureStatus.valueOf(procedure.status),
    form = ProcedureForm(procedure.details, SignatureType.valueOf(procedure.signatureType), procedure.signatureConfirmed),
    documents = documents.sortedBy { it.id }.map { Document(it.id, it.name, it.mimeType, it.sizeBytes, it.fileUri, it.templateText) },
    history = events.sortedWith(compareBy<EventEntity> { it.createdAt }.thenBy { it.id }).map {
        HistoryEvent(it.id, ProcedureStatus.valueOf(it.status), it.actorName, AccountType.valueOf(it.actorRole), it.note, it.createdAt)
    },
)
