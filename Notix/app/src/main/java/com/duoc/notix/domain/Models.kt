package com.duoc.notix.domain

import kotlinx.coroutines.flow.Flow

enum class AccountType(val label: String, val nameKey: String) {
    PERSON("Persona", "nombre"), COMPANY("Empresa", "razon"), NOTARY("Notario", "nombre")
}

data class User(val id: Long, val displayName: String, val email: String, val type: AccountType = AccountType.PERSON)

data class Registration(val type: AccountType, val fields: Map<String, String>, val password: String) {
    val name: String get() = fields[type.nameKey].orEmpty().trim()
    val email: String get() = AuthValidation.normalizeEmail(fields["correo"].orEmpty())
}

sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    data object DuplicateEmail : AuthResult
    data object InvalidCredentials : AuthResult
}

interface AuthRepository {
    suspend fun register(registration: Registration): AuthResult
    suspend fun login(email: String, password: String): AuthResult
}

enum class ProcedureStatus(val label: String) {
    DRAFT("Borrador"), UNDER_REVIEW("En revisión"), OBSERVED("Con observaciones"), COMPLETED("Finalizado");
    val editable: Boolean get() = this == DRAFT || this == OBSERVED
}

enum class SignatureType(val label: String, val explanation: String) {
    SIMPLE("Firma simple simulada", "Confirmación de prueba; no captura una firma manuscrita real."),
    ADVANCED("Firma avanzada simulada", "Mock local de certificado y e-token; no se conecta a un proveedor."),
    NOTARIZED("Firma notariada simulada", "Representa la revisión académica del perfil Notario.")
}

enum class ProcedureType(val label: String, val fileName: String, val template: String) {
    DECLARATION("Declaración jurada de prueba", "declaracion_ficticia.txt",
        "NOTARIAX · DOCUMENTO FICTICIO\nDeclaración de ejemplo para la asignatura DSY1105.\nLa persona de prueba declara antecedentes sintéticos para demostrar el flujo móvil.\nEste documento no tiene validez legal."),
    AUTHORIZATION("Autorización de prueba", "autorizacion_ficticia.txt",
        "NOTARIAX · DOCUMENTO FICTICIO\nAutorización de ejemplo entre participantes ficticios.\nSu único propósito es probar envío, observaciones y correcciones.\nEste documento no tiene validez legal."),
    POWER_OF_ATTORNEY("Poder simple de prueba", "poder_ficticio.txt",
        "NOTARIAX · DOCUMENTO FICTICIO\nPoder de ejemplo otorgado a un representante ficticio.\nNo autoriza actuaciones ni contiene datos reales.\nEste documento no tiene validez legal.")
}

data class ProcedureForm(
    val details: String = "",
    val signature: SignatureType = SignatureType.SIMPLE,
    val signatureConfirmed: Boolean = false,
)

data class Document(
    val id: Long = 0, val name: String, val mimeType: String, val sizeBytes: Long,
    val fileUri: String? = null, val templateText: String? = null,
)

data class HistoryEvent(
    val id: Long, val status: ProcedureStatus, val actorName: String,
    val actorRole: AccountType, val note: String, val createdAt: Long,
)

data class Procedure(
    val id: Long, val userId: Long, val title: String, val createdAt: Long,
    val ownerName: String, val ownerEmail: String, val ownerType: AccountType,
    val ownerProfile: Map<String, String> = emptyMap(),
    val status: ProcedureStatus = ProcedureStatus.DRAFT,
    val form: ProcedureForm = ProcedureForm(),
    val documents: List<Document> = emptyList(),
    val history: List<HistoryEvent> = emptyList(),
) {
    val observation: String? get() = history.lastOrNull { it.status == ProcedureStatus.OBSERVED && it.actorRole == AccountType.NOTARY }?.note
}

class DomainException(message: String) : IllegalStateException(message)

interface ProcedureRepository {
    fun observe(actor: User): Flow<List<Procedure>>
    suspend fun createDraft(actor: User, type: ProcedureType): Long
    suspend fun save(actor: User, id: Long, form: ProcedureForm, submit: Boolean)
    suspend fun attach(actor: User, id: Long, document: Document)
    suspend fun removeDocument(actor: User, id: Long, documentId: Long)
    suspend fun returnForCorrection(actor: User, id: Long, note: String)
    suspend fun complete(actor: User, id: Long)
}

/** Adaptador Android: copia archivos elegidos con el selector nativo al almacenamiento privado. */
interface DocumentStore {
    suspend fun copyFrom(uri: String): Document
    suspend fun delete(document: Document)
}

object ProcedureRules {
    fun errors(form: ProcedureForm, documentCount: Int, submitting: Boolean): Map<String, String> = buildMap {
        if (form.details.length > 2000) put("details", "Usa como máximo 2000 caracteres.")
        else if (submitting && form.details.trim().length < 10) put("details", "Describe los antecedentes ficticios con al menos 10 caracteres.")
        if (submitting && !form.signatureConfirmed) put("signature", "Confirma la representación de firma de prueba.")
        if (submitting && documentCount == 0) put("documents", "Agrega un documento ficticio antes de enviar.")
    }
}
