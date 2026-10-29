package com.duoc.notix

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NavigationTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private fun esperar(texto: String) {
        ui.waitUntil(30_000) { ui.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun login(correo: String = "ana.demo@notariax.test", clave: String = "demo123", notario: Boolean = false) {
        esperar("Iniciar Sesión")
        ui.onNodeWithText("Iniciar Sesión").performClick()
        if (notario) ui.onNodeWithText("Notario").performClick()
        ui.onNodeWithTag("correo_login").performScrollTo().performTextInput(correo)
        ui.onNodeWithTag("clave_login").performScrollTo().performTextInput(clave)
        ui.onNodeWithText("Ingresar").performScrollTo().performClick()
    }
    @Test fun personaPuedeSalirYEntrarComoNotario() {
        login()
        esperar("Mis trámites")
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
        ui.onNodeWithText("Ingresar").performScrollTo().performClick()
        esperar("Bandeja del Notario")
        ui.onNodeWithText("Solicitudes recibidas").assertExists()
    }
    @Test fun correccionAbreFormularioYSelectorNativoYResisteRotacion() {
        login()
        esperar("Mis trámites")
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
    @Test fun registroMuestraErrorDeRutHastaIngresarElDigitoCorrecto() {
        esperar("Crear Cuenta")
        ui.onNodeWithText("Crear Cuenta").performClick()
        ui.onNodeWithText("Notario").performClick()
        ui.onNodeWithTag("registro_RUN").performScrollTo().performTextInput("22.312.422-1")
        ui.onNodeWithText("Registrarse").performScrollTo().performClick()
        ui.onNodeWithText("El dígito verificador del RUN ficticio debe ser 4.").assertExists()
        ui.onNodeWithTag("registro_RUN").performScrollTo().performTextReplacement("22.312.422-K")
        ui.onNodeWithText("El dígito verificador del RUN ficticio debe ser 4.").assertExists()
        ui.onNodeWithTag("registro_RUN").performTextReplacement("22.312.422-4")
        ui.onNodeWithText("El dígito verificador del RUN ficticio debe ser 4.").assertDoesNotExist()
    }
}
