package com.duoc.notix

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.data.local.toEntity
import com.duoc.notix.model.TipoCuenta
import com.duoc.notix.model.Usuario
import com.duoc.notix.repository.SesionRepository
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SesionRepositoryTest {
    private lateinit var context: Context
    private lateinit var db: NotixDatabase
    private lateinit var repo: SesionRepository
    private val nombreDb = "sesion-test.db"
    private fun usuario(tipo: TipoCuenta = TipoCuenta.PERSONA) = Usuario(
        "cuenta_${tipo.name}", tipo, "Persona Demo", "${tipo.name.lowercase()}@example.com", "clave_ficticia", "111111111", "123456789", "900000000", "Comuna demo")
    private fun abrir() = Room.databaseBuilder(context, NotixDatabase::class.java, nombreDb).build()

    @Before fun setup() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences(SesionRepository.ARCHIVO, Context.MODE_PRIVATE).edit().clear().commit()
        context.deleteDatabase(nombreDb)
        db = abrir()
        repo = SesionRepository(context, db.usuarioDao())
        db.usuarioDao().insertUsuario(usuario().toEntity())
    }
    @After fun limpiar() {
        db.close()
        context.deleteDatabase(nombreDb)
        context.getSharedPreferences(SesionRepository.ARCHIVO, Context.MODE_PRIVATE).edit().clear().commit()
    }
    @Test fun restauraLaCuentaAlReabrirLaBaseYElRepositorio() = runBlocking {
        repo.guardar(usuario().id, true)
        db.close()
        db = abrir()
        val nuevo = SesionRepository(context, db.usuarioDao()).restaurar()!!
        assertEquals(usuario().id, nuevo.id)
        assertEquals(TipoCuenta.PERSONA, nuevo.tipoCuenta)
        assertEquals("", nuevo.contrasena)
    }
    @Test fun noRecordarEliminaUnaPreferenciaAnterior() = runBlocking {
        repo.guardar(usuario().id, true)
        repo.guardar(usuario().id, false)
        assertNull(SesionRepository(context, db.usuarioDao()).restaurar())
        assertTrue(context.getSharedPreferences(SesionRepository.ARCHIVO, Context.MODE_PRIVATE).all.isEmpty())
    }
    @Test fun cerrarSesionImpideRestaurarDespues() = runBlocking {
        repo.guardar(usuario().id, true)
        repo.borrar()
        assertNull(SesionRepository(context, db.usuarioDao()).restaurar())
    }
    @Test fun unaCuentaInexistenteNoAbreSesionYSeLimpia() = runBlocking {
        repo.guardar("cuenta_eliminada", true)
        assertNull(repo.restaurar())
        assertTrue(context.getSharedPreferences(SesionRepository.ARCHIVO, Context.MODE_PRIVATE).all.isEmpty())
    }
    @Test fun soloGuardaElIdentificadorSinCorreoNiContrasena() = runBlocking {
        repo.guardar(usuario().id, true)
        val valores = context.getSharedPreferences(SesionRepository.ARCHIVO, Context.MODE_PRIVATE).all
        assertEquals(1, valores.size)
        assertEquals(usuario().id, valores.values.single())
        assertFalse(valores.values.contains(usuario().correo))
        assertFalse(valores.values.contains(usuario().contrasena))
    }
    @Test fun restauraElPerfilActualDeRoomParaCadaTipoDeCuenta() = runBlocking {
        for (tipo in TipoCuenta.entries) {
            val u = usuario(tipo)
            if (tipo != TipoCuenta.PERSONA) db.usuarioDao().insertUsuario(u.toEntity())
            repo.guardar(u.id, true)
            assertEquals(tipo, repo.restaurar()!!.tipoCuenta)
        }
        repo.guardar(usuario().id, true)
        db.openHelper.writableDatabase.execSQL("UPDATE usuarios SET tipoCuenta = 'NOTARIO' WHERE id = ?", arrayOf(usuario().id))
        assertEquals(TipoCuenta.NOTARIO, repo.restaurar()!!.tipoCuenta)
    }
}
