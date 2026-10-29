package com.duoc.notix

import android.content.Context
import android.content.pm.ProviderInfo
import android.net.Uri
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.notix.data.local.*
import com.duoc.notix.model.*
import com.duoc.notix.repository.TramiteRepository
import com.duoc.notix.viewmodel.CrearTramiteViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CrearTramiteViewModelTest {
    private lateinit var context: Context
    private lateinit var db: NotixDatabase
    private lateinit var repo: TramiteRepository
    private lateinit var store: ViewModelStore
    private val uri = Uri.parse("content://fixture.documents/example")
    @Before fun setup() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, NotixDatabase::class.java).build()
        repo = TramiteRepository(db.tramiteDao(), db.avisoDao(), db.usuarioDao(), db)
        db.usuarioDao().insertUsuario(Usuario("u", TipoCuenta.PERSONA, "Persona Demo", "demo@example.com", "demo123", "111111111", "123456789", "900000000", "Comuna demo").toEntity())
        val provider = FixtureDocumentProvider().apply {
            file = File(this@CrearTramiteViewModelTest.context.cacheDir, "ficticio.txt").apply { writeText("Documento completamente ficticio.") }
            attachInfo(this@CrearTramiteViewModelTest.context, ProviderInfo().apply { authority = "fixture.documents"; exported = false })
        }
        ShadowContentResolver.registerProviderInternal("fixture.documents", provider)
        store = ViewModelStore()
    }
    @After fun close() { store.clear(); db.close(); Dispatchers.resetMain() }
    private fun crear(key: String = "nuevo") = ViewModelProvider.create(store, viewModelFactory {
        initializer { CrearTramiteViewModel(repo, "u", catalogoFormatos.first(), DocumentosLocales(context)) }
    })[key, CrearTramiteViewModel::class.java]
    private suspend fun esperar(condicion: () -> Boolean) { withTimeout(20_000) { while (!condicion()) delay(10) } }
    @Test fun aceptaCualquierVerificadorEnLaSolicitud() = runBlocking {
        val vm = crear(); esperar { !vm.uiState.cargandoUsuario }
        "0123456789Kk".forEach { dv ->
            vm.onRunChange("22.312.422-$dv"); assertNull(vm.runError)
        }
        vm.onRunChange("123"); assertNotNull(vm.runError)
    }
    @Test fun enviaTextoSinInventarNiExigirUnAdjunto() = runBlocking {
        val vm = crear(); esperar { !vm.uiState.cargandoUsuario }
        vm.onTextoSolicitudChange("Solicito un trámite ficticio con los antecedentes escritos aquí.")
        vm.onFirmaChange(true)
        vm.guardarTramite { }
        esperar { vm.uiState.guardadoExitoso }
        val t = repo.observarMisTramites("u").first().single()
        assertEquals(vm.textoSolicitud, t.textoSolicitud)
        assertTrue(t.documentos.isEmpty())
    }
    @Test fun quitarUnRespaldoPermiteContinuarConTexto() = runBlocking {
        val vm = crear(); esperar { !vm.uiState.cargandoUsuario }
        vm.onTextoSolicitudChange("Solicitud ficticia que no necesita un archivo de respaldo.")
        vm.onDocumentoAdjuntado(uri); esperar { !vm.importando }
        assertNotNull(vm.documentoAdjuntoNombre)
        vm.quitarDocumento()
        assertNull(vm.documentoAdjuntoNombre)
        assertTrue(File(context.filesDir, "adjuntos").listFiles().orEmpty().isEmpty())
        vm.onFirmaChange(true)
        assertTrue(vm.validarFormulario())
    }
    @Test fun validaTextoObligatorioYLimiteAntesDeEnviar() = runBlocking {
        val vm = crear(); esperar { !vm.uiState.cargandoUsuario }
        vm.onFirmaChange(true)
        for (texto in listOf("", "   ", "corto", "x".repeat(5001))) {
            vm.onTextoSolicitudChange(texto)
            assertFalse(vm.validarFormulario())
            assertNotNull(vm.textoError)
        }
        assertTrue(repo.observarMisTramites("u").first().isEmpty())
    }
    @Test fun evitaDuplicadosYUnaSolicitudNuevaNoReutilizaLaAnterior() = runBlocking {
        val vm = crear(); esperar { !vm.uiState.cargandoUsuario }
        vm.onDocumentoAdjuntado(uri); esperar { !vm.importando }
        vm.onFirmaChange(true)
        vm.onTextoSolicitudChange("Solicitud ficticia para verificar que solo se envía una vez.")
        var enviados = 0
        vm.guardarTramite { enviados++ }
        vm.guardarTramite { enviados++ }
        esperar { vm.uiState.guardadoExitoso && enviados == 1 }
        vm.guardarTramite { enviados++ }
        assertEquals(1, enviados)
        assertEquals(1, repo.observarMisTramites("u").first().size)
        val nueva = crear("otra_solicitud")
        esperar { !nueva.uiState.cargandoUsuario }
        assertFalse(nueva.uiState.guardadoExitoso)
        assertNull(nueva.documentoAdjuntoNombre)
        assertFalse(nueva.firmaConfirmada)
        store.clear()
        assertEquals(1, File(context.filesDir, "adjuntos").listFiles()!!.size)
    }
    @Test fun abandonarElFormularioEliminaSoloLaCopiaSinEnviar() = runBlocking {
        val vm = crear(); esperar { !vm.uiState.cargandoUsuario }
        vm.onDocumentoAdjuntado(uri); esperar { !vm.importando }
        assertNotNull(vm.documentoAdjuntoNombre)
        store.clear()
        assertTrue(File(context.filesDir, "adjuntos").listFiles().orEmpty().isEmpty())
        assertTrue(File(this@CrearTramiteViewModelTest.context.cacheDir, "ficticio.txt").exists())
    }
}
