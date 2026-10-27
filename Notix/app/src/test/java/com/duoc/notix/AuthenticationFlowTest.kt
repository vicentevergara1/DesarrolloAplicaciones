package com.duoc.notix

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.notix.data.LocalAuthRepository
import com.duoc.notix.data.LocalProcedureRepository
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.domain.*
import com.duoc.notix.ui.NotixApp
import com.duoc.notix.ui.theme.NotixTheme
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AuthenticationFlowTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private lateinit var database: NotixDatabase

    @Before fun setup() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NotixDatabase::class.java).build()
        val auth = LocalAuthRepository(database.userDao())
        val procedures = LocalProcedureRepository(database)
        ui.setContent { NotixTheme { NotixApp(auth, procedures, FakeDocumentStore()) } }
    }
    @After fun cleanup() { database.close() }
    private fun click(text: String) = ui.onNodeWithText(text).performScrollTo().performClick()
    private fun fill(label: String, text: String) = ui.onNodeWithText(label).performScrollTo().performTextInput(text)
    private fun await(text: String) {
        ui.waitUntil(20_000) { ui.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun register(type: AccountType) {
        click("Crear una cuenta de prueba")
        if (type != AccountType.PERSON) click(type.label)
        click("Completar ejemplo ficticio")
        click("Registrarse")
        await(if (type == AccountType.NOTARY) "Bandeja del notario" else "Mis trámites")
    }
    private fun logout() {
        click("Cerrar sesión")
        ui.onNode(hasText("Cerrar sesión") and hasAnyAncestor(isDialog())).performClick()
        await("Ingresa a NotariaX")
    }
    private fun login(email: String) {
        fill("Correo electrónico ficticio", email)
        fill("Contraseña", RegistrationRules.DEMO_PASSWORD)
        click("Iniciar sesión")
    }
    private fun createAndOpen() {
        await("Aún no tienes trámites")
        click("Nuevo trámite de prueba")
        await("Crear borrador")
        ui.onNodeWithText("Crear borrador").performClick()
        await("Descripción del trámite de prueba")
    }
    private fun openFirstCase() {
        await("Declaración jurada de prueba")
        click("Declaración jurada de prueba")
        await("Antecedentes ficticios")
    }
    private fun screenshot(name: String) {
        ui.runOnIdle {
            val view = ui.activity.window.decorView
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            val file = File("build/reports/ui/" + name + ".png")
            file.parentFile!!.mkdirs()
            file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            bitmap.recycle()
        }
    }

    @Test fun completeClientAndNotaryCycle() {
        register(AccountType.PERSON)
        createAndOpen()
        fill("Descripción del trámite de prueba", "Antecedentes ficticios iniciales para revisión.")
        click("Confirmo la firma simulada")
        click("Enviar a revisión")
        ui.onNodeWithText("Confirmar envío").performClick()
        await("Trámite enviado a revisión.")
        click("‹ Volver a mis trámites")
        logout()
        register(AccountType.NOTARY)
        openFirstCase()
        click("Devolver con observaciones")
        ui.onNodeWithText("Observación de prueba").performTextInput("Completa los antecedentes ficticios del documento.")
        ui.onNodeWithText("Enviar observaciones").performClick()
        await("Observaciones enviadas al solicitante.")
        click("‹ Volver a la bandeja")
        logout()
        login("persona.demo@example.com")
        await("Mis trámites")
        openFirstCase()
        await("Correcciones solicitadas")
        ui.onNodeWithText("Descripción del trámite de prueba").performScrollTo().performTextReplacement("Antecedentes ficticios corregidos y completos.")
        click("Reenviar corrección")
        ui.onNodeWithText("Confirmar envío").performClick()
        await("Trámite enviado a revisión.")
        click("‹ Volver a mis trámites")
        logout()
        login("notario.demo@example.com")
        await("Bandeja del notario")
        openFirstCase()
        click("Finalizar trámite")
        ui.onNodeWithText("Confirmar finalización").performClick()
        await("Trámite de prueba finalizado.")
        await("Finalizado")
        ui.waitForIdle()
        ui.onNodeWithText("Finalizado").performScrollTo().assertIsDisplayed()
        screenshot("tramite-finalizado")
    }

    @Test fun nativeFilePickerIsLaunchedAndCancelKeepsDraft() {
        register(AccountType.PERSON)
        createAndOpen()
        click("Adjuntar archivo ficticio")
        val shadow = Shadows.shadowOf(ui.activity)
        val request = shadow.nextStartedActivityForResult
        assertNotNull(request)
        assertEquals(Intent.ACTION_OPEN_DOCUMENT, request.intent.action)
        assertEquals("*/*", request.intent.type)
        assertArrayEquals(arrayOf("application/pdf", "text/plain", "image/png", "image/jpeg"), request.intent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES))
        shadow.receiveResult(request.intent, Activity.RESULT_CANCELED, null)
        ui.onNodeWithText("Borrador").performScrollTo().assertIsDisplayed()
    }

    @Test fun companyRegistrationNavigatesAndBlankPasswordStaysOnForm() {
        click("Crear una cuenta de prueba")
        click("Empresa")
        click("Completar ejemplo ficticio")
        ui.onNodeWithText("Contraseña").performScrollTo().performTextReplacement("        ")
        ui.onNodeWithText("Confirmar contraseña").performScrollTo().performTextReplacement("        ")
        click("Registrarse")
        ui.onNodeWithText("La contraseña no puede estar en blanco.").performScrollTo().assertIsDisplayed()
        click("Completar ejemplo ficticio")
        click("Registrarse")
        await("Mis trámites")
        ui.onNodeWithText("Empresa · Perfil de prueba").performScrollTo().assertIsDisplayed()
    }
}
