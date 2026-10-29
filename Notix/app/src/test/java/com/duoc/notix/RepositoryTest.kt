package com.duoc.notix

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.notix.data.local.*
import com.duoc.notix.model.*
import com.duoc.notix.repository.*
import com.duoc.notix.data.validation.PasswordStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class RepositoryTest {
    private lateinit var db: NotixDatabase
    private lateinit var repo: TramiteRepository
    private lateinit var auth: AuthRepository
    private val persona = usuario("persona")
    private val notario = usuario("notario", TipoCuenta.NOTARIO)
    private fun usuario(id: String, tipo: TipoCuenta = TipoCuenta.PERSONA) = Usuario(id, tipo, "Persona Ficticia", "$id@example.com", "demo123", "111111111", "123456789", "900000000", "Comuna demo")
    private fun tramite(id: String = "TX-PRUEBA") = Tramite(id, catalogoFormatos.first(), persona.id, persona.nombreCompleto,
        tipoFirma = TipoFirma.SIMPLE, estado = EstadoTramite.ENVIADO,
        documentos = listOf(Documento("ficticio.txt", 1, rutaLocal = "copia-ficticia", mime = "text/plain")),
        antecedentes = Antecedentes(persona.nombreCompleto, persona.run, persona.correo, persona.comuna),
        firmaConfirmada = true, historial = listOf(EventoHistorial("Enviado", "29/10/2026 12:00", TipoCuenta.PERSONA)))

    @Before fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NotixDatabase::class.java).build()
        repo = TramiteRepository(db.tramiteDao(), db.avisoDao(), db.usuarioDao(), db)
        auth = AuthRepository(db.usuarioDao())
        db.usuarioDao().insertUsuario(persona.toEntity())
        db.usuarioDao().insertUsuario(notario.toEntity())
    }
    @After fun close() { db.close() }
    private suspend fun revisar(t: Tramite, estado: EstadoTramite, nota: String? = null, actor: String = notario.id, legible: Boolean = true) =
        repo.actualizarEstadoTramite(t, estado, nota, "Evaluación simulada", TipoCuenta.NOTARIO, "Estado actualizado", actor, legible)
    private suspend fun rechazado(block: suspend () -> Unit) {
        try { block(); fail("La operación debe rechazarse") } catch (_: IllegalArgumentException) { }
    }
    @Test fun registroNormalizaCorreoYGuardaHash() = runBlocking {
        val u = usuario("nueva").copy(correo = " Nueva@Example.com ")
        val r = auth.registrar(u) as AuthResult.Success
        assertEquals("", r.usuario.contrasena)
        val stored = db.usuarioDao().getUsuarioPorCorreo("nueva@example.com")!!
        assertTrue(PasswordStorage.esHash(stored.contrasena))
        assertTrue(PasswordStorage.coincide("demo123", stored.contrasena))
        assertFalse(PasswordStorage.coincide("erronea", stored.contrasena))
        assertTrue(auth.login(TipoCuenta.PERSONA, "NUEVA@example.com", "demo123") is AuthResult.Success)
        assertTrue(auth.registrar(u.copy(id = "otra")) is AuthResult.Error)
    }
    @Test fun contrasenaSoloEspaciosNoSeRegistra() = runBlocking {
        assertTrue(auth.registrar(usuario("vacia").copy(contrasena = "      ")) is AuthResult.Error)
        assertNull(db.usuarioDao().getUsuarioPorCorreo("vacia@example.com"))
    }
    @Test fun loginActualizaClaveAntiguaSinBorrarLaCuenta() = runBlocking {
        assertTrue(auth.login(TipoCuenta.PERSONA, persona.correo, "demo123") is AuthResult.Success)
        assertTrue(PasswordStorage.esHash(db.usuarioDao().getUsuarioPorCorreo(persona.correo)!!.contrasena))
        assertTrue(auth.login(TipoCuenta.NOTARIO, persona.correo, "demo123") is AuthResult.Error)
        assertTrue(auth.login(TipoCuenta.PERSONA, persona.correo, "otra") is AuthResult.Error)
    }
    @Test fun crearDevolverCorregirYFinalizarConHistorialYAvisos() = runBlocking {
        val t = tramite()
        repo.crearTramite(t, persona.id)
        revisar(t, EstadoTramite.DEVUELTO, "Corrige la comuna ficticia.")
        val devuelto = repo.obtenerTramite(t.id)!!
        assertEquals(EstadoTramite.DEVUELTO, devuelto.estado)
        val corregido = devuelto.copy(antecedentes = devuelto.antecedentes.copy(comuna = "Nueva comuna"))
        repo.reenviarTramite(corregido, persona.id)
        val enviado = repo.obtenerTramite(t.id)!!
        assertEquals(EstadoTramite.ENVIADO, enviado.estado)
        assertNull(enviado.observacion)
        assertEquals(1, enviado.documentos.size)
        revisar(enviado, EstadoTramite.FINALIZADO)
        val fin = repo.obtenerTramite(t.id)!!
        assertEquals(EstadoTramite.FINALIZADO, fin.estado)
        assertTrue(fin.legibilidadConfirmada)
        assertEquals(4, fin.historial.size)
        assertEquals(4, repo.observarAvisos(persona.id).first().size)
        val aviso = repo.observarAvisos(persona.id).first().first()
        repo.marcarAvisoLeido(aviso.id)
        assertTrue(repo.observarAvisos(persona.id).first().first { it.id == aviso.id }.leido)
    }
    @Test fun estadosFinalesYEvaluacionesDuplicadasSeRechazan() = runBlocking {
        val t = tramite(); repo.crearTramite(t, persona.id)
        revisar(t, EstadoTramite.FINALIZADO)
        rechazado { revisar(t, EstadoTramite.DEVUELTO, "No permitido") }
        rechazado { repo.reenviarTramite(t, persona.id) }
        assertEquals(2, repo.obtenerTramite(t.id)!!.historial.size)
        assertEquals(2, repo.observarAvisos(persona.id).first().size)
    }
    @Test fun exigeObservacionYConfirmacionDeLegibilidad() = runBlocking {
        val t = tramite(); repo.crearTramite(t, persona.id)
        rechazado { revisar(t, EstadoTramite.DEVUELTO, " ") }
        rechazado { revisar(t, EstadoTramite.FINALIZADO, legible = false) }
        assertEquals(1, repo.obtenerTramite(t.id)!!.historial.size)
        assertEquals(EstadoTramite.ENVIADO, repo.obtenerTramite(t.id)!!.estado)
    }
    @Test fun rolesYPropietariosNoPuedenModificarCasosAjenos() = runBlocking {
        val t = tramite(); repo.crearTramite(t, persona.id)
        rechazado { revisar(t, EstadoTramite.DEVUELTO, "Sin permiso", persona.id) }
        revisar(t, EstadoTramite.DEVUELTO, "Revisa los datos")
        rechazado { repo.reenviarTramite(t, notario.id) }
        rechazado { repo.crearTramite(t.copy(id = "otro"), notario.id) }
    }
    @Test fun formularioSinArchivoOFirmaNoCreaFilas() = runBlocking {
        rechazado { repo.crearTramite(tramite().copy(documentos = emptyList()), persona.id) }
        rechazado { repo.crearTramite(tramite().copy(firmaConfirmada = false), persona.id) }
        assertTrue(repo.observarMisTramites(persona.id).first().isEmpty())
        assertTrue(repo.observarAvisos(persona.id).first().isEmpty())
    }
    @Test fun idDuplicadoNoReemplazaHistorialNiDocumentos() = runBlocking {
        val t = tramite(); repo.crearTramite(t, persona.id)
        try { repo.crearTramite(t, persona.id); fail("Debe rechazar id duplicado") } catch (_: android.database.sqlite.SQLiteConstraintException) { }
        assertEquals(1, repo.obtenerTramite(t.id)!!.historial.size)
        assertEquals(1, repo.obtenerTramite(t.id)!!.documentos.size)
    }
}
