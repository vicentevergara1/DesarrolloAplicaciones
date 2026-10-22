package com.duoc.notix

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.notix.data.LocalAuthRepository
import com.duoc.notix.data.LocalProcedureRepository
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.ui.NotixApp
import com.duoc.notix.ui.theme.NotixTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AuthenticationFlowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private lateinit var database: NotixDatabase

    @Before fun setup() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NotixDatabase::class.java).build()
        val auth = LocalAuthRepository(database.userDao())
        val procedures = LocalProcedureRepository(database.procedureDao())
        compose.setContent { NotixTheme { NotixApp(auth, procedures) } }
    }

    @After fun cleanup() { database.close() }

    private fun click(text: String) = compose.onNodeWithText(text).performScrollTo().performClick()
    private fun fill(label: String, text: String) = compose.onNodeWithText(label).performScrollTo().performTextInput(text)
    private fun awaitText(text: String) {
        compose.waitUntil(20_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun screenshot(name: String) {
        val file = File("build/reports/ui/$name.png")
        file.parentFile!!.mkdirs()
        // Dibujo directo de la vista: Robolectric no ofrece PixelCopy de una ventana real.
        compose.runOnIdle {
            val view = compose.activity.window.decorView
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            file.outputStream().use { stream ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
            }
            bitmap.recycle()
        }
    }

    @Test fun registerCreateDraftLogoutAndLogin() {
        screenshot("login")
        click("Crear una cuenta de prueba")
        fill("Nombre de prueba", "Persona Demo")
        fill("Correo electrónico ficticio", "demo@example.com")
        fill("Contraseña", "prueba123")
        fill("Confirmar contraseña", "prueba123")
        click("Crear cuenta")
        awaitText("Mis trámites")
        awaitText("Aún no tienes trámites")
        compose.onNodeWithText("Aún no tienes trámites").performScrollTo().assertIsDisplayed()
        click("Nuevo trámite de prueba")
        compose.onNodeWithText("Guardar borrador").performClick()
        awaitText("TRÁMITE #1")
        compose.onNodeWithText("TRÁMITE #1").performScrollTo().assertIsDisplayed()
        screenshot("borrador")
        click("Cerrar sesión")
        compose.onNode(hasText("Cerrar sesión") and hasAnyAncestor(isDialog())).performClick()
        awaitText("Bienvenido de nuevo")
        fill("Correo electrónico ficticio", "DEMO@example.com")
        fill("Contraseña", "prueba123")
        click("Iniciar sesión")
        awaitText("TRÁMITE #1")
        compose.onNodeWithText("Borrador").performScrollTo().assertIsDisplayed()
    }
}
