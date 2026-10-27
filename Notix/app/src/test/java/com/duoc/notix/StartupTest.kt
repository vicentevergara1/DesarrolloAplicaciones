package com.duoc.notix

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = NotixApplication::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class StartupTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    @Test fun installedEntryPointUsesTheRealApplicationAndDatabase() {
        ui.onNodeWithText("Ingresa a NotariaX").assertIsDisplayed()
        ui.onNodeWithText("Crear una cuenta de prueba").performScrollTo().performClick()
        ui.onNodeWithText("Completar ejemplo ficticio").performScrollTo().performClick()
        ui.onNodeWithText("Registrarse").performScrollTo().performClick()
        ui.waitUntil(20_000) { ui.onAllNodesWithText("Aún no tienes trámites").fetchSemanticsNodes().isNotEmpty() }
    }
}
