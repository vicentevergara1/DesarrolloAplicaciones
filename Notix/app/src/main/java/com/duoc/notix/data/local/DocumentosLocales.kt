package com.duoc.notix.data.local

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.duoc.notix.model.Documento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class DocumentosLocales(private val context: Context) {
    companion object {
        const val MAX_BYTES = 5 * 1024 * 1024
        val TIPOS = arrayOf("application/pdf", "text/plain", "image/png", "image/jpeg")
    }
    suspend fun importar(uri: Uri): Documento {
        var copia: File? = null
        return try { withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri) ?: error("No se reconoce el tipo del archivo.")
        require(mime in TIPOS) { "Selecciona un PDF, TXT, PNG o JPEG ficticio." }
        var nombre = "documento"
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val n = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (n >= 0) nombre = c.getString(n) ?: nombre
                val t = c.getColumnIndex(OpenableColumns.SIZE)
                if (t >= 0 && !c.isNull(t)) require(c.getLong(t) <= MAX_BYTES) { "El archivo supera los 5 MB." }
            }
        }
        val directorio = File(context.filesDir, "adjuntos").apply { check(isDirectory || mkdirs()) }
        val destino = File(directorio, UUID.randomUUID().toString()).also { copia = it }
        try {
            var total = 0
            resolver.openInputStream(uri)?.use { entrada ->
                destino.outputStream().use { salida ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        val n = entrada.read(buffer)
                        if (n < 0) break
                        total += n
                        require(total <= MAX_BYTES) { "El archivo supera los 5 MB." }
                        salida.write(buffer, 0, n)
                    }
                }
            } ?: error("No se pudo abrir el archivo.")
            require(total > 0) { "El archivo está vacío." }
            Documento(nombre.take(160), (total + 1023) / 1024, rutaLocal = destino.name, mime = mime)
        } catch (e: Exception) { destino.delete(); throw e }
        } } catch (e: Exception) { copia?.delete(); throw e }
    }
    fun borrar(documento: Documento) {
        documento.rutaLocal?.let { ruta -> if (ruta == File(ruta).name) File(context.filesDir, "adjuntos/$ruta").delete() }
    }
}

fun abrirDocumento(context: Context, documento: Documento): String? = try {
    val nombre = documento.rutaLocal
    require(nombre != null && nombre == File(nombre).name) { "Este adjunto de demostración contiene solo metadatos. Reemplázalo por un archivo ficticio." }
    val archivo = File(context.filesDir, "adjuntos/$nombre")
    require(archivo.isFile) { "El archivo ya no está disponible; vuelve a adjuntarlo." }
    val uri = FileProvider.getUriForFile(context, context.packageName + ".documentos", archivo)
    context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, documento.mime)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
    null
} catch (_: android.content.ActivityNotFoundException) {
    "No hay una aplicación instalada para abrir este tipo de archivo."
} catch (e: Exception) { e.message ?: "No se pudo abrir el documento." }
