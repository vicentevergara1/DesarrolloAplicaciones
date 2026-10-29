package com.duoc.notix.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.notix.model.EstadoTramite
import com.duoc.notix.model.Tramite
import com.duoc.notix.ui.components.BarraInferiorPersona
import com.duoc.notix.ui.components.EstadoChip
import com.duoc.notix.ui.components.TramiteCard
import com.duoc.notix.ui.theme.NotixColors
import com.duoc.notix.viewmodel.FiltroTramites
import com.duoc.notix.viewmodel.MisTramitesUiState
import com.duoc.notix.viewmodel.MisTramitesViewModel

@Composable
fun MisTramitesScreen(
    usuarioId: String,
    onNuevoTramite: () -> Unit = {},
    onVerTramite: (String) -> Unit = {},
    onAvisos: () -> Unit = {},
    onPerfil: () -> Unit = {},
    onCorregir: (Tramite) -> Unit = {},
    vm: MisTramitesViewModel = viewModel(
        key = usuarioId,
        factory = MisTramitesViewModel.factory(LocalContext.current, usuarioId)
    )
) {
    val state by vm.uiState.collectAsState()
    var tramiteDetalle by remember { mutableStateOf<Tramite?>(null) }

    MisTramitesContent(
        state = state,
        onFiltro = vm::onFiltro,
        onNuevoTramite = onNuevoTramite,
        onVerTramite = { id ->
            val e = state.tramites.find { it.id == id } ?: state.alerta
            if (e != null) {
                tramiteDetalle = e
            } else {
                onVerTramite(id)
            }
        },
        onAvisos = onAvisos, onPerfil = onPerfil
    )

    tramiteDetalle?.let { tramite ->
        DetalleClienteDialog(
            tramite = tramite,
            onCorregir = { tramiteDetalle = null; onCorregir(tramite) },
            onDismiss = { tramiteDetalle = null }
        )
    }
}

@Composable
fun MisTramitesContent(
    state: MisTramitesUiState,
    onFiltro: (FiltroTramites) -> Unit,
    onNuevoTramite: () -> Unit,
    onVerTramite: (String) -> Unit,
    onAvisos: () -> Unit,
    onPerfil: () -> Unit
) {
    Scaffold(
        containerColor = NotixColors.Fondo,
        bottomBar = {
            BarraInferiorPersona(
                seleccionado = 0,
                onInicio = { onFiltro(FiltroTramites.TODOS) }, onTramites = { onFiltro(FiltroTramites.TODOS) }, onAvisos = onAvisos, onPerfil = onPerfil
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "encabezado") {
                    Encabezado(
                        perfil = state.perfil,
                        nombre = state.nombre,
                        avisosSinLeer = state.avisosSinLeer,
                        onCampana = { state.alerta?.let { onVerTramite(it.id) } ?: onAvisos() }
                    )
                }

                item(key = "alerta") {
                    AnimatedContent(
                        targetState = state.alerta,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        modifier = Modifier.animateContentSize(),
                        label = "alerta"
                    ) { tramite ->
                        if (tramite != null) {
                            AlertaBanner(tramite, onClick = { onVerTramite(tramite.id) })
                        } else {
                            Spacer(Modifier.height(0.dp))
                        }
                    }
                }

                item(key = "resumen") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ResumenCard(state.enRevision, "En revisión", Modifier.weight(1f))
                        ResumenCard(state.observados, "Observados", Modifier.weight(1f))
                        ResumenCard(state.finalizados, "Finalizados", Modifier.weight(1f))
                    }
                }

                item(key = "titulo") {
                    Text(
                        "Mis trámites",
                        color = NotixColors.Tinta, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                item(key = "filtros") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(FiltroTramites.entries) { f ->
                            FiltroChip(
                                texto = f.etiqueta,
                                seleccionado = state.filtro == f,
                                onClick = { onFiltro(f) }
                            )
                        }
                    }
                }

                items(state.tramites, key = { it.id }) { tramite ->
                    TramiteCard(
                        tramite = tramite,
                        onClick = { onVerTramite(tramite.id) },
                        modifier = Modifier.animateItem()
                    )
                }

                if (state.tramites.isEmpty() && !state.cargando) {
                    item(key = "vacio") {
                        Text(
                            "No tienes trámites en esta categoría.",
                            color = NotixColors.Gris, fontSize = 15.sp,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                }
            }

            Button(
                onClick = onNuevoTramite,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NotixColors.Brand, contentColor = Color.White)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Nuevo trámite", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun Encabezado(
    perfil: String,
    nombre: String,
    avisosSinLeer: Boolean,
    onCampana: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(Modifier.weight(1f)) {
            Text("Perfil: $perfil", color = NotixColors.Gris, fontSize = 13.sp)
            Text(
                "Hola, $nombre",
                color = NotixColors.Tinta, fontSize = 30.sp,
                fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif
            )
        }
        Campana(avisosSinLeer = avisosSinLeer, onClick = onCampana)
    }
}

@Composable
private fun Campana(avisosSinLeer: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(1.dp, NotixColors.Borde),
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Notifications, contentDescription = "Avisos", tint = NotixColors.Tinta)
            AnimatedVisibility(
                visible = avisosSinLeer,
                enter = scaleIn(),
                exit = scaleOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 10.dp)
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(NotixColors.Punto, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun AlertaBanner(tramite: Tramite, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = NotixColors.AlertaFondo,
        border = BorderStroke(1.dp, NotixColors.AlertaBorde)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.WarningAmber, contentDescription = null,
                    tint = NotixColors.AlertaTexto, modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Requiere tu atención",
                    color = NotixColors.AlertaTexto, fontSize = 13.sp, fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "El notario devolvió tu ${tramite.formato.nombre.lowercase()} con observaciones.",
                color = NotixColors.Tinta, fontSize = 18.sp, fontWeight = FontWeight.SemiBold
            )
            if (!tramite.observacion.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Motivo: \"${tramite.observacion}\"",
                    color = NotixColors.AlertaTexto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(tramite.id, color = NotixColors.Gris, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ver detalle", color = NotixColors.Tinta, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = NotixColors.Tinta)
                }
            }
        }
    }
}

@Composable
private fun DetalleClienteDialog(
    tramite: Tramite,
    onDismiss: () -> Unit,
    onCorregir: () -> Unit
) {
    val context = LocalContext.current
    var errorDocumento by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    tramite.id,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NotixColors.Gris
                )
                EstadoChip(tramite.estado)
            }
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    tramite.formato.nombre,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NotixColors.Tinta
                )

                if (tramite.estado == EstadoTramite.DEVUELTO && !tramite.observacion.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NotixColors.AlertaFondo,
                        border = BorderStroke(1.dp, NotixColors.AlertaBorde),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.WarningAmber,
                                    contentDescription = null,
                                    tint = NotixColors.AlertaTexto,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Observaciones del Notario",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NotixColors.AlertaTexto
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                tramite.observacion,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NotixColors.Tinta
                            )
                        }
                    }
                }

                Text("Firma: ${tramite.tipoFirma.etiqueta} (${if (tramite.firmaConfirmada) "simulada y confirmada" else "demostración anterior"})", fontSize = 13.sp)
                Text("Texto de la solicitud:", fontWeight = FontWeight.Bold, color = NotixColors.Tinta)
                Text(tramite.textoSolicitud.ifBlank { "Solicitud anterior sin texto registrado." }, fontSize = 14.sp, color = NotixColors.Tinta)
                Text("Antecedentes del trámite:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NotixColors.Tinta)
                Text("• Solicitante: ${tramite.antecedentes.nombreCompleto}", fontSize = 13.sp, color = NotixColors.Tinta)
                Text("• RUN: ${tramite.antecedentes.documentoIdentidad}", fontSize = 13.sp, color = NotixColors.Tinta)
                Text("• Correo: ${tramite.antecedentes.correoContacto}", fontSize = 13.sp, color = NotixColors.Tinta)
                Text("• Comuna: ${tramite.antecedentes.comuna}", fontSize = 13.sp, color = NotixColors.Tinta)
                tramite.actuaEnNombreDe?.let {
                    Text("• En representación de: $it", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NotixColors.Tinta)
                }

                if (tramite.documentos.isEmpty()) Text("Sin documento de respaldo", fontSize = 13.sp)
                Text("Documentos adjuntos:", fontWeight = FontWeight.Bold)
                tramite.documentos.forEach { doc ->
                    androidx.compose.material3.TextButton(onClick = { errorDocumento = com.duoc.notix.data.local.abrirDocumento(context, doc) }) { Text(doc.nombre) }
                }
                errorDocumento?.let { Text(it, color = NotixColors.AlertaTexto) }
                if (tramite.historial.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Historial del trámite:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NotixColors.Tinta)
                    tramite.historial.forEach { h ->
                        Text("• ${h.fechaHora} (${h.rol.etiqueta}): ${h.descripcion}", fontSize = 12.sp, color = NotixColors.Gris)
                    }
                }
            }
        },
        dismissButton = {
            if (tramite.estado == EstadoTramite.DEVUELTO) androidx.compose.material3.TextButton(onClick = onCorregir) { Text("Corregir y reenviar") }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NotixColors.Brand)
            ) {
                Text("Entendido")
            }
        }
    )
}

@Composable
private fun ResumenCard(valor: Int, etiqueta: String, modifier: Modifier = Modifier) {
    val animado by animateIntAsState(valor, label = "contador")
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, NotixColors.Borde)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                animado.toString(),
                color = NotixColors.Tinta, fontSize = 26.sp,
                fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif
            )
            Text(etiqueta, color = NotixColors.Gris, fontSize = 13.sp)
        }
    }
}

@Composable
private fun FiltroChip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    val fondo by animateColorAsState(if (seleccionado) NotixColors.Tinta else Color.White, label = "chipFondo")
    val contenido by animateColorAsState(if (seleccionado) Color.White else NotixColors.Tinta, label = "chipTexto")
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = fondo,
        border = BorderStroke(1.dp, if (seleccionado) NotixColors.Tinta else NotixColors.Borde)
    ) {
        Text(
            texto,
            color = contenido, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
        )
    }
}
