package com.duoc.notix.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.notix.model.EstadoTramite
import com.duoc.notix.model.Tramite
import com.duoc.notix.ui.components.notixTextFieldColors
import com.duoc.notix.ui.theme.NotixColors
import com.duoc.notix.viewmodel.BandejaNotarioViewModel
import com.duoc.notix.viewmodel.FiltroTramites

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandejaNotarioScreen(
    usuarioId: String,
    onCerrarSesion: () -> Unit,
    vm: BandejaNotarioViewModel = viewModel(
        key = usuarioId,
        factory = BandejaNotarioViewModel.factory(LocalContext.current, usuarioId)
    )
) {
    val state by vm.uiState.collectAsState()
    var tramiteParaRevisar by remember { mutableStateOf<Tramite?>(null) }

    Scaffold(
        containerColor = NotixColors.Fondo,
        snackbarHost = { vm.error?.let { Text(it, color = NotixColors.AlertaTexto, modifier = Modifier.padding(16.dp)) } },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Bandeja del Notario",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NotixColors.Tinta
                    )
                },
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = NotixColors.Tinta
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NotixColors.Fondo)
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "encabezado") {
                    EncabezadoNotario(
                        notaria = state.numeroNotaria,
                        nombre = state.nombreNotario
                    )
                }

                item(key = "resumen") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ResumenCardNotario(state.enRevision, "En revisión", Modifier.weight(1f))
                        ResumenCardNotario(state.observados, "Devueltos", Modifier.weight(1f))
                        ResumenCardNotario(state.finalizados, "Aprobados", Modifier.weight(1f))
                    }
                }

                item(key = "titulo") {
                    Text(
                        "Solicitudes recibidas",
                        color = NotixColors.Tinta, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                item(key = "filtros") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(FiltroTramites.entries) { f ->
                            FiltroChipNotario(
                                texto = f.etiqueta,
                                seleccionado = state.filtro == f,
                                onClick = { vm.onFiltro(f) }
                            )
                        }
                    }
                }

                items(state.tramites, key = { it.id }) { tramite ->
                    TramiteNotarioCard(
                        tramite = tramite,
                        onRevisar = { if (!vm.procesando) tramiteParaRevisar = tramite }
                    )
                }

                if (state.tramites.isEmpty() && !state.cargando) {
                    item(key = "vacio") {
                        Text(
                            "No hay solicitudes registradas en esta categoría.",
                            color = NotixColors.Gris, fontSize = 15.sp,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                }
            }
        }
    }

    tramiteParaRevisar?.let { tramite ->
        RevisionTramiteDialog(
            tramite = tramite,
            onDismiss = { tramiteParaRevisar = null },
            onAprobar = {
                vm.aprobarTramite(tramite)
                tramiteParaRevisar = null
            },
            onDevolver = { obs ->
                vm.devolverTramite(tramite, obs)
                tramiteParaRevisar = null
            }
        )
    }
}

@Composable
private fun EncabezadoNotario(
    notaria: Int,
    nombre: String
) {
    Column(Modifier.fillMaxWidth()) {
        Text("Notaría N° $notaria", color = NotixColors.Gris, fontSize = 13.sp)
        Text(
            "Hola, ${nombre.substringBefore(" ")}",
            color = NotixColors.Tinta, fontSize = 30.sp,
            fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif
        )
    }
}

@Composable
private fun ResumenCardNotario(valor: Int, etiqueta: String, modifier: Modifier = Modifier) {
    val animado by animateIntAsState(valor, label = "contadorNotario")
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
private fun FiltroChipNotario(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    val fondo by animateColorAsState(if (seleccionado) NotixColors.Tinta else Color.White, label = "chipFondoNotario")
    val contenido by animateColorAsState(if (seleccionado) Color.White else NotixColors.Tinta, label = "chipTextoNotario")
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

@Composable
private fun TramiteNotarioCard(
    tramite: Tramite,
    onRevisar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, NotixColors.Borde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(tramite.id, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NotixColors.Gris)

                val (badgeFondo, badgeTexto, badgeLabel) = when (tramite.estado) {
                    EstadoTramite.ENVIADO -> Triple(NotixColors.RevisionFondo, NotixColors.RevisionTexto, "En revisión")
                    EstadoTramite.DEVUELTO -> Triple(NotixColors.AlertaFondo, NotixColors.AlertaTexto, "Devuelto")
                    EstadoTramite.FINALIZADO -> Triple(NotixColors.FinalizadoFondo, NotixColors.FinalizadoTexto, "Finalizado")
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = badgeFondo
                ) {
                    Text(
                        badgeLabel,
                        color = badgeTexto,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                tramite.formato.nombre,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NotixColors.Tinta
            )

            Text(
                "Solicitante: ${tramite.antecedentes.nombreCompleto} (${tramite.antecedentes.documentoIdentidad})",
                fontSize = 14.sp,
                color = NotixColors.Tinta,
                modifier = Modifier.padding(top = 2.dp)
            )

            Text(
                "Comuna: ${tramite.antecedentes.comuna} · Correo: ${tramite.antecedentes.correoContacto}",
                fontSize = 12.sp,
                color = NotixColors.Gris,
                modifier = Modifier.padding(top = 2.dp)
            )

            if (!tramite.observacion.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Obs: ${tramite.observacion}",
                    fontSize = 12.sp,
                    color = NotixColors.AlertaTexto,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onRevisar,
                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("revisar_${tramite.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NotixColors.Brand)
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("Revisar / Evaluar solicitud", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun RevisionTramiteDialog(
    tramite: Tramite,
    onDismiss: () -> Unit,
    onAprobar: () -> Unit,
    onDevolver: (String) -> Unit
) {
    val context = LocalContext.current
    var errorDocumento by remember { mutableStateOf<String?>(null) }
    var legibilidad by remember { mutableStateOf(false) }
    val editable = tramite.estado == EstadoTramite.ENVIADO
    var modoDevolver by remember { mutableStateOf(false) }
    var observacion by remember { mutableStateOf("") }
    var errorObservacion by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                "Evaluación del trámite",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NotixColors.Tinta
            )
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Trámite ${tramite.id} - ${tramite.formato.nombre}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = NotixColors.Tinta
                )

                Text("Texto de la solicitud:", fontWeight = FontWeight.Bold, color = NotixColors.Tinta)
                Text(tramite.textoSolicitud.ifBlank { "Solicitud anterior sin texto registrado." }, fontSize = 14.sp, color = NotixColors.Tinta)
                Text("Solicitante: ${tramite.antecedentes.nombreCompleto}", fontSize = 13.sp, color = NotixColors.Tinta)
                Text("RUN: ${tramite.antecedentes.documentoIdentidad}", fontSize = 13.sp, color = NotixColors.Tinta)
                Text("Correo: ${tramite.antecedentes.correoContacto}", fontSize = 13.sp, color = NotixColors.Tinta)
                Text("Comuna: ${tramite.antecedentes.comuna}", fontSize = 13.sp, color = NotixColors.Tinta)
                tramite.actuaEnNombreDe?.let {
                    Text("En representación de: $it", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NotixColors.Tinta)
                }

                if (tramite.documentos.isEmpty()) Text("Sin documento de respaldo", fontSize = 13.sp)
                if (tramite.documentos.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Documentos adjuntos:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NotixColors.Tinta)
                    tramite.documentos.forEach { doc ->
                        TextButton(onClick = { errorDocumento = com.duoc.notix.data.local.abrirDocumento(context, doc) }) { Text("${doc.nombre} (${doc.tamanoKb} KB)", fontSize = 12.sp) }
                    }
                }

                errorDocumento?.let { Text(it, color = NotixColors.AlertaTexto) }
                Text("Evaluación y firma notariales simuladas; sin validez legal.", fontSize = 12.sp)
                if (editable) Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = legibilidad, onCheckedChange = { legibilidad = it })
                    Text("Confirmo que revisé el texto y, si hay respaldos, su legibilidad.", fontSize = 12.sp)
                }
                if (!editable) Text("Este trámite ya fue evaluado. Una devolución requiere que el solicitante lo corrija y reenvíe.", fontSize = 12.sp)
                if (modoDevolver) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = observacion,
                        onValueChange = {
                            observacion = it
                            if (it.isNotBlank()) errorObservacion = null
                        },
                        label = { Text("Motivo u observaciones") },
                        isError = errorObservacion != null,
                        supportingText = errorObservacion?.let { { Text(it) } },
                        colors = notixTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (!editable) {
                TextButton(onClick = onDismiss) { Text("Cerrar") }
            } else if (modoDevolver) {
                Button(
                    onClick = {
                        if (observacion.isBlank()) {
                            errorObservacion = "Ingresa el motivo de devolución"
                        } else {
                            onDevolver(observacion.trim())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B))
                ) {
                    Text("Confirmar devolución")
                }
            } else {
                Button(
                    onClick = onAprobar,
                    enabled = legibilidad,
                    colors = ButtonDefaults.buttonColors(containerColor = NotixColors.Brand)
                ) {
                    Icon(Icons.Outlined.Check, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Aprobar trámite")
                }
            }
        },
        dismissButton = {
            if (!editable) {
                TextButton(onClick = onDismiss) { Text("Volver") }
            } else if (modoDevolver) {
                TextButton(onClick = { modoDevolver = false }) {
                    Text("Cancelar", color = NotixColors.Gris)
                }
            } else {
                OutlinedButton(
                    onClick = { modoDevolver = true },
                    border = BorderStroke(1.dp, NotixColors.AlertaBorde)
                ) {
                    Text("Devolver con obs.", color = NotixColors.AlertaTexto)
                }
            }
        }
    )
}
