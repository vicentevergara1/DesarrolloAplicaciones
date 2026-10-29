package com.duoc.notix.data.local

import android.content.Context
import android.util.Log
import com.duoc.notix.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

object DatabaseSeeder {

    suspend fun seedIfEmpty(context: Context) = withContext(Dispatchers.IO) {
        val db = NotixDatabase.getDatabase(context)
        val tramiteDao = db.tramiteDao()
        val usuarioDao = db.usuarioDao()
        val avisoDao = db.avisoDao()

        // crear/asegurar usuario demo persona
        val usuarioDemo = Usuario(
            id = "USR-001",
            tipoCuenta = TipoCuenta.PERSONA,
            nombreCompleto = "Ana Demo Prueba",
            correo = "ana.demo@notariax.test",
            contrasena = "demo123",
            run = "111111111",
            numeroDocumento = "123456789",
            telefono = "912345678",
            comuna = "Ñuñoa"
        )
        usuarioDao.upsertUsuario(usuarioDemo.toEntity())

        // crear/asegurar usuario demo notario
        val notarioDemo = Usuario(
            id = "USR-NOTARIO-01",
            tipoCuenta = TipoCuenta.NOTARIO,
            nombreCompleto = "Carlos Notario Silva",
            correo = "notario.demo@notariax.test",
            contrasena = "notario123",
            run = "222222222",
            numeroDocumento = "987654321",
            telefono = "987654321",
            comuna = "Santiago",
            numeroNotaria = 1
        )
        usuarioDao.upsertUsuario(notarioDemo.toEntity())

        val tramitesExistentes = tramiteDao.getTodosLosTramites().firstOrNull().orEmpty()
        if (tramitesExistentes.isNotEmpty()) {
            Log.d("DatabaseSeeder", "La base de datos ya contiene ${tramitesExistentes.size} trámites.")
            return@withContext
        }

        tramitesDemo(usuarioDemo).forEach { t ->
            val (te, de, he) = t.toEntities()
            tramiteDao.insertTramiteCompleto(te, de, he)
        }


        // Crear trámite demo
        val tramiteDemo = Tramite(
            id = "TX-2026-0001",
            formato = FormatoTramite("F01", "Poder simple", "Poderes", TipoFirma.SIMPLE),
            solicitanteId = usuarioDemo.id,
            solicitanteNombre = usuarioDemo.nombreCompleto,
            tipoFirma = TipoFirma.SIMPLE,
            estado = EstadoTramite.ENVIADO,
            documentos = listOf(
                Documento(nombre = "poder_simple_demo.pdf", tamanoKb = 250, estado = EstadoDocumento.ACEPTADO)
            ),
            antecedentes = Antecedentes(
                nombreCompleto = usuarioDemo.nombreCompleto,
                documentoIdentidad = usuarioDemo.run,
                correoContacto = usuarioDemo.correo,
                comuna = "Providencia"
            ),
            historial = listOf(
                EventoHistorial(
                    descripcion = "Trámite ingresado correctamente",
                    fechaHora = "28/10/2026 15:00",
                    rol = TipoCuenta.PERSONA
                )
            )
        )

        val (tramiteEntity, docEntities, histEntities) = tramiteDemo.toEntities()
        tramiteDao.insertTramiteCompleto(tramiteEntity, docEntities, histEntities)

        // Crear aviso demo
        val avisoDemo = Aviso(
            id = "AVS-001",
            destinatarioId = usuarioDemo.id,
            tramiteId = tramiteDemo.id,
            mensaje = "Tu trámite TX-2026-0001 ha sido enviado al notario con éxito.",
            fechaHora = "28/10/2026 15:01",
            leido = false
        )
        avisoDao.insertAviso(avisoDemo.toEntity())

        Log.d("DatabaseSeeder", "Base de datos inicializada correctamente con datos demo.")
    }

    private fun tramitesDemo(u: Usuario): List<Tramite> {
        fun antecedentes() = Antecedentes(u.nombreCompleto, u.run, u.correo, u.comuna)
        return listOf(
            Tramite(
                id = "TX-2026-0042",
                formato = FormatoTramite("F01", "Poder simple", "Poderes", TipoFirma.SIMPLE),
                solicitanteId = u.id, solicitanteNombre = u.nombreCompleto,
                tipoFirma = TipoFirma.SIMPLE, estado = EstadoTramite.DEVUELTO,
                documentos = listOf(
                    Documento("poder_demo.pdf", 90, EstadoDocumento.ACEPTADO),
                    Documento("anexo_demo.jpg", 300, EstadoDocumento.OBSERVADO)
                ),
                antecedentes = antecedentes(),
                observacion = "Falta indicar la comuna del domicilio. El anexo está ilegible, vuelve a cargarlo.",
                historial = listOf(
                    EventoHistorial("Enviado al notario", "12/10/2026 10:00", TipoCuenta.PERSONA),
                    EventoHistorial("Devuelto con observaciones", "13/10/2026 09:30", TipoCuenta.NOTARIO)
                )
            ),
            Tramite(
                id = "TX-2026-0039",
                formato = FormatoTramite("F02", "Declaración jurada simple", "Declaraciones", TipoFirma.AVANZADA),
                solicitanteId = u.id, solicitanteNombre = u.nombreCompleto,
                tipoFirma = TipoFirma.AVANZADA, estado = EstadoTramite.ENVIADO,
                documentos = listOf(Documento("declaracion_demo.pdf", 120, EstadoDocumento.ACEPTADO)),
                antecedentes = antecedentes(),
                historial = listOf(EventoHistorial("Enviado al notario", "10/10/2026 16:20", TipoCuenta.PERSONA))
            ),
            Tramite(
                id = "TX-2026-0031",
                formato = FormatoTramite("F03", "Autorización de viaje", "Familia", TipoFirma.NOTARIADA),
                solicitanteId = u.id, solicitanteNombre = u.nombreCompleto,
                tipoFirma = TipoFirma.NOTARIADA, estado = EstadoTramite.FINALIZADO,
                documentos = listOf(Documento("viaje_demo.pdf", 150, EstadoDocumento.ACEPTADO)),
                antecedentes = antecedentes(),
                historial = listOf(
                    EventoHistorial("Enviado al notario", "02/10/2026 11:00", TipoCuenta.PERSONA),
                    EventoHistorial("Aprobado y finalizado por el notario", "03/10/2026 12:00", TipoCuenta.NOTARIO)
                )
            )
        )
    }


}
