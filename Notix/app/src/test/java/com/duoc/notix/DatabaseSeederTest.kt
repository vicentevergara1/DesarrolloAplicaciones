package com.duoc.notix

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.duoc.notix.data.local.DatabaseSeeder
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.data.validation.PasswordStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DatabaseSeederTest {
    @Test fun abrirOtraVezNoSobrescribeCuentasNiDuplicaTramites() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        DatabaseSeeder.seedIfEmpty(context)
        val db = NotixDatabase.getDatabase(context)
        val original = db.usuarioDao().getUsuarioPorCorreo("ana.demo@notariax.test")!!
        val actualizada = PasswordStorage.guardar("cambioDemo123")
        db.usuarioDao().actualizarClave(original.id, actualizada)
        val tramites = db.tramiteDao().getTodosLosTramites().first().size
        DatabaseSeeder.seedIfEmpty(context)
        assertEquals(actualizada, db.usuarioDao().getUsuarioPorCorreo(original.correo)!!.contrasena)
        assertEquals(tramites, db.tramiteDao().getTodosLosTramites().first().size)
    }
}
