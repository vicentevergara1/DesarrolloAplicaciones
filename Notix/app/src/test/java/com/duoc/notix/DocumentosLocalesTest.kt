package com.duoc.notix

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.test.core.app.ApplicationProvider
import com.duoc.notix.data.local.DocumentosLocales
import com.duoc.notix.model.Documento
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import java.io.File

class FixtureDocumentProvider : ContentProvider() {
    lateinit var file: File
    var mime = "text/plain"
    var declaredSize: Long? = null
    override fun onCreate() = true
    override fun getType(uri: Uri) = mime
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor =
        MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)).apply { addRow(arrayOf<Any?>("ejemplo_ficticio.txt", declaredSize)) }
    override fun openFile(uri: Uri, mode: String) = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class DocumentosLocalesTest {
    private lateinit var context: Context
    private lateinit var provider: FixtureDocumentProvider
    private lateinit var store: DocumentosLocales
    private val uri = Uri.parse("content://fixture.documents/example")
    @Before fun setup() {
        context = ApplicationProvider.getApplicationContext()
        File(context.filesDir, "adjuntos").deleteRecursively()
        provider = FixtureDocumentProvider()
        provider.file = File(context.cacheDir, "documento_ficticio.txt").apply { writeText("Contenido ficticio para probar el selector de archivos.") }
        provider.attachInfo(context, ProviderInfo().apply { authority = "fixture.documents"; exported = false })
        ShadowContentResolver.registerProviderInternal("fixture.documents", provider)
        store = DocumentosLocales(context)
    }
    @Test fun selectedDocumentIsCopiedPrivatelyAndCanBeRemovedWithoutDeletingOriginal() = runBlocking {
        val document = store.importar(uri)
        assertEquals("ejemplo_ficticio.txt", document.nombre)
        assertEquals(1, document.tamanoKb)
        assertNotNull(document.rutaLocal)
        val copy = File(context.filesDir, "adjuntos").listFiles()!!.single()
        assertEquals(provider.file.readText(), copy.readText())
        store.borrar(Documento("externo", 1, rutaLocal = "../externo"))
        assertTrue(provider.file.exists())
        store.borrar(document)
        assertFalse(copy.exists())
        assertTrue(provider.file.exists())
    }
    @Test fun unknownSizeCannotBypassFiveMegabyteLimit() = runBlocking {
        provider.file.outputStream().use { it.write(ByteArray((DocumentosLocales.MAX_BYTES + 1).toInt())) }
        try { store.importar(uri); fail("Debe rechazar más de 5 MB.") } catch (_: IllegalArgumentException) { }
        assertTrue(File(context.filesDir, "adjuntos").listFiles().orEmpty().isEmpty())
    }
    @Test fun emptyAndUnsupportedFilesAreRejected() = runBlocking {
        provider.mime = "application/octet-stream"
        try { store.importar(uri); fail("Tipo no permitido.") } catch (_: IllegalArgumentException) { }
        provider.mime = "text/plain"
        provider.file.writeText("")
        try { store.importar(uri); fail("Archivo vacío.") } catch (_: IllegalArgumentException) { }
        assertTrue(File(context.filesDir, "adjuntos").listFiles().orEmpty().isEmpty())
    }
}
