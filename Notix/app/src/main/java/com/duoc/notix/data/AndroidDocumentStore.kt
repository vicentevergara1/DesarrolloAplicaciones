package com.duoc.notix.data

import android.content.Context
import androidx.core.net.toUri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.duoc.notix.domain.Document
import com.duoc.notix.domain.DocumentStore
import com.duoc.notix.domain.DomainException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class AndroidDocumentStore(private val context: Context) : DocumentStore {
    companion object {
        const val MAX_BYTES = 5L * 1024 * 1024
        val ALLOWED_TYPES = setOf("application/pdf", "text/plain", "image/jpeg", "image/png")
    }

    override suspend fun copyFrom(uri: String): Document = withContext(Dispatchers.IO) {
        val source = uri.toUri()
        if (source.scheme != "content") throw DomainException("Selecciona un archivo desde el selector de Android.")
        val resolver = context.contentResolver
        val mime = resolver.getType(source) ?: throw DomainException("No se pudo identificar el tipo del archivo.")
        if (mime !in ALLOWED_TYPES) throw DomainException("Usa un PDF, TXT, PNG o JPG ficticio.")
        var name = "documento_de_prueba"
        resolver.query(source, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && !cursor.isNull(index)) name = cursor.getString(index).take(120)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex) && cursor.getLong(sizeIndex) > MAX_BYTES)
                    throw DomainException("El archivo debe pesar como máximo 5 MB.")
            }
        }
        val directory = File(context.filesDir, "attachments")
        check(directory.isDirectory || directory.mkdirs())
        val target = File(directory, UUID.randomUUID().toString())
        try {
            var size = 0L
            val input = resolver.openInputStream(source) ?: throw DomainException("No se pudo abrir el documento.")
            input.use { stream ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        ensureActive()
                        val count = stream.read(buffer)
                        if (count < 0) break
                        size += count
                        if (size > MAX_BYTES) throw DomainException("El archivo debe pesar como máximo 5 MB.")
                        output.write(buffer, 0, count)
                    }
                }
            }
            if (size == 0L) throw DomainException("El archivo está vacío.")
            val localUri = FileProvider.getUriForFile(context, context.packageName + ".documents", target)
            Document(name = name.ifBlank { "Documento ficticio" }, mimeType = mime, sizeBytes = size, fileUri = localUri.toString())
        } catch (error: Exception) {
            target.delete()
            throw error
        }
    }

    override suspend fun delete(document: Document) = withContext(Dispatchers.IO) {
        val uri = document.fileUri?.toUri() ?: return@withContext
        // Solo borra copias privadas creadas por este adaptador; nunca el original del usuario.
        if (uri.authority == context.packageName + ".documents" && uri.pathSegments.firstOrNull() == "attachments") {
            val directory = File(context.filesDir, "attachments").canonicalFile
            val file = File(directory, uri.lastPathSegment.orEmpty()).canonicalFile
            if (file.parentFile == directory) file.delete()
        }
        Unit
    }
}
