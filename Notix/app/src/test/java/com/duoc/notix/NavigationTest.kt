package com.duoc.notix

import android.content.Intent
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import com.duoc.notix.data.local.NotixDatabase
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NavigationTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>(effectContext = kotlinx.coroutines.test.StandardTestDispatcher())

    @Before fun prepararInicioSinSesionAnterior() {
        ui.waitUntil(30_000) {
            listOf("Iniciar Sesión", "Ingresar", "Nuevo trámite", "Bandeja del Notario").any {
                ui.onAllNodesWithText(it).fetchSemanticsNodes().isNotEmpty()
            }
        }
        if (ui.onAllNodesWithText("Bandeja del Notario").fetchSemanticsNodes().isNotEmpty()) {
            ui.onNodeWithContentDescription("Cerrar sesión").performClick()
            esperar("Ingresar")
        } else if (ui.onAllNodesWithText("Nuevo trámite").fetchSemanticsNodes().isNotEmpty()) {
            ui.onNodeWithText("Perfil").performClick()
            esperar("Mi perfil")
            ui.onNodeWithText("Cerrar sesión").performClick()
            ui.onAllNodesWithText("Cerrar sesión").onLast().performClick()
            esperar("Ingresar")
        }
        if (ui.onAllNodesWithText("Ingresar").fetchSemanticsNodes().isNotEmpty()) {
            ui.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        }
        esperar("Iniciar Sesión")
    }

    private fun esperar(texto: String) {
        try {
            ui.waitUntil(30_000) { ui.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty() }
        } catch (e: androidx.compose.ui.test.ComposeTimeoutException) {
            throw AssertionError("No se encontró: $texto\n${ui.onRoot().printToString()}", e)
        }
    }
    private fun login(correo: String = "ana.demo@notariax.test", clave: String = "demo123", notario: Boolean = false, recordar: Boolean = false) {
        esperar("Iniciar Sesión")
        ui.onNodeWithText("Iniciar Sesión").performClick()
        if (notario) ui.onNodeWithText("Notario").performClick()
        ui.onNodeWithTag("correo_login").performScrollTo().performTextInput(correo)
        ui.onNodeWithTag("clave_login").performScrollTo().performTextInput(clave)
        ui.onNodeWithTag("recordar_sesion").performScrollTo().assertIsOff()
        if (recordar) ui.onNodeWithTag("recordar_sesion").performClick()
        ui.onNodeWithText("Ingresar").performScrollTo().assertIsEnabled().performClick()
    }
    @Test fun personaPuedeSalirYEntrarComoNotario() {
        login()
        esperar("Nuevo trámite")
        ui.onNodeWithText("Perfil").performClick()
        esperar("Mi perfil")
        esperar("ana.demo@notariax.test")
        ui.onNodeWithText("ana.demo@notariax.test").assertIsDisplayed()
        ui.onNodeWithText("Cerrar sesión").performClick()
        ui.onAllNodesWithText("Cerrar sesión").onLast().performClick()
        esperar("Ingresar")
        ui.onNodeWithText("Notario").performClick()
        ui.onNodeWithTag("correo_login").performScrollTo().performTextInput("notario.demo@notariax.test")
        ui.onNodeWithTag("clave_login").performScrollTo().performTextInput("notario123")
        ui.onNodeWithText("Ingresar").performScrollTo().assertIsEnabled().performClick()
        esperar("Bandeja del Notario")
        ui.onNodeWithText("Solicitudes recibidas").assertExists()
    }
    @Test fun correccionAbreFormularioYSelectorNativoYResisteRotacion() {
        login()
        esperar("Nuevo trámite")
        esperar("Ver detalle")
        ui.waitForIdle()
        ui.onNodeWithText("Ver detalle").performScrollTo().performClick()
        ui.onNodeWithText("Corregir y reenviar").performClick()
        esperar("Solicitud de trámite")
        ui.onNodeWithText("Seleccionar").performScrollTo().performClick()
        val request = shadowOf(ui.activity).nextStartedActivityForResult
        assertEquals(Intent.ACTION_OPEN_DOCUMENT, request.intent.action)
        assertEquals("*/*", request.intent.type)
        assertTrue(request.intent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES)!!.contains("application/pdf"))
        shadowOf(ui.activity).receiveResult(request.intent, android.app.Activity.RESULT_CANCELED, null)
        ui.activityRule.scenario.recreate()
        esperar("Solicitud de trámite")
        ui.onNodeWithText("Nombre completo").assertExists()
    }
    @Test fun registroAceptaTodosLosVerificadoresConFormatoValido() {
        esperar("Crear Cuenta")
        ui.onNodeWithText("Crear Cuenta").performClick()
        ui.onNodeWithText("Notario").performClick()
        ui.onNodeWithTag("registro_RUN").performScrollTo().performTextInput("123")
        ui.onNodeWithText("Registrarse").performScrollTo().performClick()
        ui.onNodeWithTag("registro_RUN").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
        "0123456789Kk".forEach { dv ->
            ui.onNodeWithTag("registro_RUN").performScrollTo().performTextReplacement("22.312.422-$dv")
            ui.onNodeWithTag("registro_RUN").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Error))
        }
    }
    @Test fun solicitaConTextoSinDocumentoYMuestraElTextoAlNotario() {
        login()
        esperar("Nuevo trámite")
        ui.onNodeWithText("Nuevo trámite").performClick()
        ui.onNodeWithText("Poder simple").performClick()
        esperar("Solicitud de trámite")
        val texto = "Solicito un poder ficticio de demostración usando únicamente este texto."
        ui.onNodeWithTag("texto_solicitud").performScrollTo().performTextInput(texto)
        ui.activityRule.scenario.recreate()
        esperar("Solicitud de trámite")
        ui.onNodeWithTag("texto_solicitud").assertTextContains(texto)
        ui.onNodeWithText("Confirmo los antecedentes ficticios y mi firma simulada (Firma simple). Esta acción no tiene validez legal.")
            .performScrollTo().performClick()
        ui.onNodeWithText("Revisar y enviar solicitud").performScrollTo().performClick()
        ui.onNode(hasText(texto) and !hasSetTextAction()).assertExists()
        ui.onNodeWithText("Confirmar y enviar").performClick()
        esperar("Nuevo trámite")
        val tramiteId = runBlocking {
            NotixDatabase.getDatabase(ui.activity).tramiteDao().getTodosLosTramites().first().single { it.tramite.textoSolicitud == texto }.tramite.id
        }
        ui.onNodeWithText("Perfil").performClick()
        esperar("Mi perfil")
        ui.onNodeWithText("Cerrar sesión").performClick()
        ui.onAllNodesWithText("Cerrar sesión").onLast().performClick()
        esperar("Ingresar")
        ui.onNodeWithText("Notario").performClick()
        ui.onNodeWithTag("correo_login").performScrollTo().performTextInput("notario.demo@notariax.test")
        ui.onNodeWithTag("clave_login").performScrollTo().performTextInput("notario123")
        ui.onNodeWithText("Ingresar").performScrollTo().assertIsEnabled().performClick()
        esperar("Bandeja del Notario")
        ui.waitUntil(30_000) { ui.onAllNodesWithText("Revisar / Evaluar solicitud").fetchSemanticsNodes().isNotEmpty() }
        ui.onNode(hasScrollToIndexAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)).performScrollToNode(hasTestTag("revisar_$tramiteId"))
        ui.onNodeWithTag("revisar_$tramiteId").performClick()
        ui.onNode(hasText(texto) and !hasSetTextAction()).assertExists()
        ui.onNodeWithText("Sin documento de respaldo").assertExists()
    }
    private fun reiniciarEstadoEnMemoria() {
        ui.activityRule.scenario.onActivity { it.viewModelStore.clear() }
        ui.activityRule.scenario.recreate()
    }

    @Test fun recordarPersonaEvitaLoginAlReabrirYCerrarSesionLoBorra() {
        login(recordar = true)
        esperar("Nuevo trámite")
        ui.onNodeWithText("Perfil").performClick()
        esperar("Mi perfil")
        reiniciarEstadoEnMemoria()
        esperar("Nuevo trámite")
        ui.onNodeWithText("Perfil").performClick()
        esperar("ana.demo@notariax.test")
        ui.onNodeWithText("Cerrar sesión").performClick()
        ui.onAllNodesWithText("Cerrar sesión").onLast().performClick()
        esperar("Ingresar")
        reiniciarEstadoEnMemoria()
        esperar("Ingresar")
        ui.onNodeWithTag("recordar_sesion").performScrollTo().assertIsOff()
    }

    @Test fun sinRecordarVuelveAIngresoAlReiniciar() {
        login()
        esperar("Nuevo trámite")
        reiniciarEstadoEnMemoria()
        esperar("Iniciar Sesión")
        ui.onNodeWithText("Nuevo trámite").assertDoesNotExist()
    }

    @Test fun recordarNotarioRestauraSuBandeja() {
        login("notario.demo@notariax.test", "notario123", notario = true, recordar = true)
        esperar("Bandeja del Notario")
        reiniciarEstadoEnMemoria()
        esperar("Bandeja del Notario")
        ui.onNodeWithText("Solicitudes recibidas").assertExists()
    }

    @Test fun credencialesIncorrectasNoGuardanSesionAunqueLaCasillaEsteMarcada() {
        login(clave = "clave_incorrecta", recordar = true)
        esperar("Correo, contraseña o perfil incorrectos.")
        val valores = ui.activity.getSharedPreferences(com.duoc.notix.repository.SesionRepository.ARCHIVO, android.content.Context.MODE_PRIVATE).all
        assertTrue(valores.isEmpty())
        ui.onNodeWithTag("recordar_sesion").performScrollTo().assertIsOn()
    }

}
