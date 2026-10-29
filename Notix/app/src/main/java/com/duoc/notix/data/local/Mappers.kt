package com.duoc.notix.data.local

import com.duoc.notix.model.*

fun UsuarioEntity.toDomain(): Usuario = Usuario(
    id = id,
    tipoCuenta = tipoCuenta,
    nombreCompleto = nombreCompleto,
    correo = correo,
    contrasena = contrasena,
    run = run,
    numeroDocumento = numeroDocumento,
    telefono = telefono,
    comuna = comuna,
    empresa = if (razonSocialEmpresa != null && rutEmpresa != null)
        DatosEmpresa(razonSocialEmpresa, rutEmpresa, giroEmpresa.orEmpty()) else null,
    numeroNotaria = numeroNotaria
)

fun Usuario.toEntity(): UsuarioEntity = UsuarioEntity(
    id = id,
    tipoCuenta = tipoCuenta,
    nombreCompleto = nombreCompleto,
    correo = correo,
    contrasena = contrasena,
    run = run,
    numeroDocumento = numeroDocumento,
    telefono = telefono,
    comuna = comuna,
    razonSocialEmpresa = empresa?.razonSocial,
    rutEmpresa = empresa?.rutEmpresa,
    giroEmpresa = empresa?.giro,
    numeroNotaria = numeroNotaria
)

fun TramiteConDetalles.toDomain(): Tramite {
    return Tramite(
        id = tramite.id,
        formato = FormatoTramite(
            id = tramite.formatoId,
            nombre = tramite.formatoNombre,
            categoria = tramite.formatoCategoria,
            firmaRequerida = tramite.tipoFirma
        ),
        solicitanteId = tramite.solicitanteId,
        solicitanteNombre = tramite.solicitanteNombre,
        actuaEnNombreDe = tramite.actuaEnNombreDe,
        tipoFirma = tramite.tipoFirma,
        estado = tramite.estado,
        documentos = documentos.map {
            Documento(
                nombre = it.nombre,
                tamanoKb = it.tamanoKb,
                estado = it.estado, rutaLocal = it.rutaLocal, mime = it.mime
            )
        },
        antecedentes = Antecedentes(
            nombreCompleto = tramite.antecedentesNombreCompleto,
            documentoIdentidad = tramite.antecedentesDocumentoIdentidad,
            correoContacto = tramite.antecedentesCorreoContacto,
            comuna = tramite.antecedentesComuna
        ),
        observacion = tramite.observacion,
        legibilidadConfirmada = tramite.legibilidadConfirmada,
        firmaConfirmada = tramite.firmaConfirmada,
        historial = historial.sortedBy { it.id }.map {
            EventoHistorial(
                descripcion = it.descripcion,
                fechaHora = it.fechaHora,
                rol = it.rol
            )
        }
    )
}

fun Tramite.toEntities(): Triple<TramiteEntity, List<DocumentoEntity>, List<EventoHistorialEntity>> {
    val tramiteEntity = TramiteEntity(
        id = id,
        formatoId = formato.id,
        formatoNombre = formato.nombre,
        formatoCategoria = formato.categoria,
        solicitanteId = solicitanteId,
        solicitanteNombre = solicitanteNombre,
        actuaEnNombreDe = actuaEnNombreDe,
        tipoFirma = tipoFirma,
        estado = estado,
        antecedentesNombreCompleto = antecedentes.nombreCompleto,
        antecedentesDocumentoIdentidad = antecedentes.documentoIdentidad,
        antecedentesCorreoContacto = antecedentes.correoContacto,
        antecedentesComuna = antecedentes.comuna,
        observacion = observacion,
        legibilidadConfirmada = legibilidadConfirmada, firmaConfirmada = firmaConfirmada
    )

    val documentoEntities = documentos.map {
        DocumentoEntity(
            tramiteId = id,
            nombre = it.nombre,
            tamanoKb = it.tamanoKb,
            estado = it.estado, rutaLocal = it.rutaLocal, mime = it.mime
        )
    }

    val historialEntities = historial.map {
        EventoHistorialEntity(
            tramiteId = id,
            descripcion = it.descripcion,
            fechaHora = it.fechaHora,
            rol = it.rol
        )
    }

    return Triple(tramiteEntity, documentoEntities, historialEntities)
}

fun AvisoEntity.toDomain(): Aviso = Aviso(
    id = id,
    destinatarioId = destinatarioId,
    tramiteId = tramiteId,
    mensaje = mensaje,
    fechaHora = fechaHora,
    leido = leido
)

fun Aviso.toEntity(): AvisoEntity = AvisoEntity(
    id = id,
    destinatarioId = destinatarioId,
    tramiteId = tramiteId,
    mensaje = mensaje,
    fechaHora = fechaHora,
    leido = leido
)
