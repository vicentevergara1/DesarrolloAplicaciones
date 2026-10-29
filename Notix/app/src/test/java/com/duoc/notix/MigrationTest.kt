package com.duoc.notix

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.notix.data.local.NotixDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MigrationTest {
    @Test fun versionDosConservaUsuariosTramitesYDocumentos() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "migracion-v2.db"
        context.deleteDatabase(name)
        val path = context.getDatabasePath(name).apply { parentFile!!.mkdirs() }
        val old = SQLiteDatabase.openOrCreateDatabase(path, null)
        val schema = JSONObject(File("schemas/com.duoc.notix.data.local.NotixDatabase/2.json").readText()).getJSONObject("database")
        val marker = "$" + "{TABLE_NAME}"
        val entities = schema.getJSONArray("entities")
        for (i in 0 until entities.length()) {
            val entity = entities.getJSONObject(i)
            val table = entity.getString("tableName")
            old.execSQL(entity.getString("createSql").replace(marker, table))
            val indices = entity.optJSONArray("indices") ?: org.json.JSONArray()
            for (n in 0 until indices.length()) old.execSQL(indices.getJSONObject(n).getString("createSql").replace(marker, table))
        }
        old.execSQL("INSERT INTO usuarios VALUES ('u','PERSONA','Nombre Ficticio','anterior@example.com','demo123','111111111','123456789','900000000','Comuna',NULL,NULL,NULL,NULL)")
        old.execSQL("INSERT INTO tramites VALUES ('t','F01','Poder simple','Poderes','u','Nombre Ficticio',NULL,'SIMPLE','DEVUELTO','Nombre Ficticio','111111111','anterior@example.com','Comuna','Corrige el adjunto',0)")
        old.execSQL("INSERT INTO documentos (tramiteId,nombre,tamanoKb,estado) VALUES ('t','anterior.txt',1,'ACEPTADO')")
        old.execSQL("INSERT INTO historial_eventos (tramiteId,descripcion,fechaHora,rol) VALUES ('t','Enviado','28/10/2026 12:00','PERSONA')")
        old.version = 2
        old.close()
        val db = Room.databaseBuilder(context, NotixDatabase::class.java, name).addMigrations(NotixDatabase.MIGRATION_2_3, NotixDatabase.MIGRATION_3_4).build()
        try {
            assertEquals("Nombre Ficticio", db.usuarioDao().getUsuarioPorCorreo("anterior@example.com")!!.nombreCompleto)
            val t = db.tramiteDao().getTramiteConDetalles("t").first()!!
            assertEquals("", t.tramite.textoSolicitud)
            assertEquals("Corrige el adjunto", t.tramite.observacion)
            assertFalse(t.tramite.firmaConfirmada)
            assertEquals(1, t.historial.size)
            assertEquals("anterior.txt", t.documentos.single().nombre)
            assertNull(t.documentos.single().rutaLocal)
        } finally { db.close(); context.deleteDatabase(name) }
    }
    @Test fun versionTresConservaTextoNuevoAlReabrirYLosRespaldosExistentes() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "migracion-v3.db"
        context.deleteDatabase(name)
        val path = context.getDatabasePath(name).apply { parentFile!!.mkdirs() }
        val old = SQLiteDatabase.openOrCreateDatabase(path, null)
        val schema = JSONObject(File("schemas/com.duoc.notix.data.local.NotixDatabase/3.json").readText()).getJSONObject("database")
        val entities = schema.getJSONArray("entities")
        val marker = "$" + "{TABLE_NAME}"
        for (i in 0 until entities.length()) {
            val entity = entities.getJSONObject(i)
            val table = entity.getString("tableName")
            old.execSQL(entity.getString("createSql").replace(marker, table))
            val indices = entity.optJSONArray("indices") ?: org.json.JSONArray()
            for (n in 0 until indices.length()) old.execSQL(indices.getJSONObject(n).getString("createSql").replace(marker, table))
        }
        old.execSQL("INSERT INTO tramites VALUES ('t','F01','Poder simple','Poderes','u','Persona Demo',NULL,'SIMPLE','DEVUELTO','Persona Demo','111111111','demo@example.com','Comuna','Amplía el texto',0,1)")
        old.execSQL("INSERT INTO documentos (tramiteId,nombre,tamanoKb,estado,rutaLocal,mime) VALUES ('t','respaldo.txt',1,'ACEPTADO','copia-existente','text/plain')")
        old.version = 3
        old.close()
        fun abrir() = Room.databaseBuilder(context, NotixDatabase::class.java, name).addMigrations(NotixDatabase.MIGRATION_2_3, NotixDatabase.MIGRATION_3_4).build()
        var db = abrir()
        try {
            val t = db.tramiteDao().getTramiteConDetalles("t").first()!!
            assertEquals("", t.tramite.textoSolicitud)
            assertTrue(t.tramite.firmaConfirmada)
            assertEquals("copia-existente", t.documentos.single().rutaLocal)
            db.tramiteDao().updateTramite(t.tramite.copy(textoSolicitud = "Solicitud ficticia guardada después de migrar."))
            db.close()
            db = abrir()
            val reabierto = db.tramiteDao().getTramiteConDetalles("t").first()!!
            assertEquals("Solicitud ficticia guardada después de migrar.", reabierto.tramite.textoSolicitud)
            assertEquals(1, reabierto.documentos.size)
        } finally { db.close(); context.deleteDatabase(name) }
    }

}
