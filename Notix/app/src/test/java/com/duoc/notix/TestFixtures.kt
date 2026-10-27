package com.duoc.notix

import com.duoc.notix.domain.*

fun registration(
    email: String = "demo@example.com", type: AccountType = AccountType.PERSON,
    name: String = "Persona Demo", password: String = "prueba123",
): Registration = Registration(type, RegistrationRules.example(type) + mapOf(type.nameKey to name, "correo" to email), password)

fun testProcedure(id: Long = 1, userId: Long = 7) = Procedure(
    id, userId, "Declaración jurada de prueba", 1, "Persona Demo", "demo@example.com", AccountType.PERSON,
    documents = listOf(Document(1, "ejemplo.txt", "text/plain", 10, templateText = "Texto ficticio")),
)

class FakeDocumentStore : DocumentStore {
    val deleted = mutableListOf<Document>()
    override suspend fun copyFrom(uri: String) = Document(name = "adjunto.txt", mimeType = "text/plain", sizeBytes = 10, fileUri = uri)
    override suspend fun delete(document: Document) { deleted += document }
}
