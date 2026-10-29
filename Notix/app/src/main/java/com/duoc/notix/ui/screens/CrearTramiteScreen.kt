package com.duoc.notix.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.notix.data.model.FormatoTramite
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.ui.components.notixTextFieldColors
import com.duoc.notix.ui.theme.NotixColors
import com.duoc.notix.viewmodel.CrearTramiteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearTramiteScreen(
    usuarioId: String,
    formato: FormatoTramite,
    onVolver: () -> Unit,
    onTramiteCreado: () -> Unit,
    vm: CrearTramiteViewModel = viewModel(
        key = "${usuarioId}_${formato.id}",
        factory = CrearTramiteViewModel.factory(LocalContext.current, usuarioId, formato)
    )
) {
    val context = LocalContext.current
    var legibilidadConfirmada by remember { mutableStateOf(true) }
    var mostrarModalConfirmacion by remember { mutableStateOf(false) }

    // Selector nativo de archivos de Android
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val nombreArchivo = obtenerNombreArchivo(context, uri) ?: "documento_adjunto.pdf"
            vm.onDocumentoAdjuntado(nombreArchivo)
        }
    }

    Scaffold(
        containerColor = NotixColors.Fondo,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Solicitud de trámite",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NotixColors.Tinta
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = NotixColors.Tinta
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NotixColors.Fondo)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta del Formato Seleccionado
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, NotixColors.Borde),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Description,
                        contentDescription = null,
                        tint = NotixColors.Brand,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            formato.nombre,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NotixColors.Tinta
                        )
                        Text(
                            "Categoría: ${formato.categoria} · Firma: ${formato.firmaRequerida.etiqueta}",
                            fontSize = 13.sp,
                            color = NotixColors.Gris
                        )
                    }
                }
            }

            Text(
                "Antecedentes del solicitante",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NotixColors.Tinta,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Campo Nombre Completo
            OutlinedTextField(
                value = vm.nombreCompleto,
                onValueChange = vm::onNombreChange,
                label = { Text("Nombre completo") },
                singleLine = true,
                isError = vm.nombreError != null,
                supportingText = vm.nombreError?.let { { Text(it) } },
                shape = RoundedCornerShape(10.dp),
                colors = notixTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            // Campo RUN / Documento
            OutlinedTextField(
                value = vm.run,
                onValueChange = vm::onRunChange,
                label = { Text("RUN / Documento de identidad") },
                singleLine = true,
                isError = vm.runError != null,
                supportingText = vm.runError?.let { { Text(it) } },
                shape = RoundedCornerShape(10.dp),
                colors = notixTextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Correo Electrónico
            OutlinedTextField(
                value = vm.correo,
                onValueChange = vm::onCorreoChange,
                label = { Text("Correo electrónico de contacto") },
                singleLine = true,
                isError = vm.correoError != null,
                supportingText = vm.correoError?.let { { Text(it) } },
                shape = RoundedCornerShape(10.dp),
                colors = notixTextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Comuna
            OutlinedTextField(
                value = vm.comuna,
                onValueChange = vm::onComunaChange,
                label = { Text("Comuna de residencia") },
                singleLine = true,
                isError = vm.comunaError != null,
                supportingText = vm.comunaError?.let { { Text(it) } },
                shape = RoundedCornerShape(10.dp),
                colors = notixTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            // Si es empresa o si aplica actuación en representación
            if (vm.uiState.usuario?.tipoCuenta == TipoCuenta.EMPRESA || vm.actuaEnNombreDe.isNotBlank()) {
                OutlinedTextField(
                    value = vm.actuaEnNombreDe,
                    onValueChange = vm::onActuaEnNombreDeChange,
                    label = { Text("En representación de (Razón Social)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = notixTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Recurso Nativo Android: Adjuntar Documento
            Text(
                "Documentación adjunta (Recurso Nativo)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NotixColors.Tinta,
                modifier = Modifier.padding(top = 8.dp)
            )

            Surface(
                onClick = { documentPickerLauncher.launch("*/*") },
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, NotixColors.Borde),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (vm.documentoAdjuntoNombre != null) Icons.Outlined.Task else Icons.Outlined.UploadFile,
                        contentDescription = null,
                        tint = if (vm.documentoAdjuntoNombre != null) NotixColors.Brand else NotixColors.Gris,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (vm.documentoAdjuntoNombre != null) "Documento adjuntado" else "Adjuntar archivo desde el dispositivo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NotixColors.Tinta
                        )
                        Text(
                            text = vm.documentoAdjuntoNombre ?: "Toca para abrir el selector de archivos nativo de Android",
                            fontSize = 12.sp,
                            color = if (vm.documentoAdjuntoNombre != null) NotixColors.Brand else NotixColors.Gris
                        )
                    }
                    OutlinedButton(
                        onClick = { documentPickerLauncher.launch("*/*") },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(if (vm.documentoAdjuntoNombre != null) "Cambiar" else "Seleccionar", fontSize = 12.sp)
                    }
                }
            }

            // Confirmación de legibilidad
            Surface(
                onClick = { legibilidadConfirmada = !legibilidadConfirmada },
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, NotixColors.Borde),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (legibilidadConfirmada) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
                        contentDescription = null,
                        tint = if (legibilidadConfirmada) NotixColors.Brand else NotixColors.Gris
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Declaro que los antecedentes proporcionados son verídicos y legibles.",
                        fontSize = 13.sp,
                        color = NotixColors.Tinta
                    )
                }
            }

            AnimatedVisibility(
                visible = vm.uiState.errorGeneral != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                vm.uiState.errorGeneral?.let { err ->
                    Text(
                        err,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Botón Continuar al Modal de Confirmación
            Button(
                onClick = {
                    if (vm.validarFormulario()) {
                        mostrarModalConfirmacion = true
                    }
                },
                enabled = !vm.uiState.guardando && legibilidadConfirmada,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NotixColors.Brand)
            ) {
                if (vm.uiState.guardando) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Revisar y enviar solicitud",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Modal de Confirmaciones previo al guardado (Requisito D)
    if (mostrarModalConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarModalConfirmacion = false },
            containerColor = Color.White,
            title = {
                Text(
                    "Confirmación de solicitud",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NotixColors.Tinta
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Revisa el resumen antes de enviar tu trámite definitivamente:",
                        fontSize = 13.sp,
                        color = NotixColors.Gris
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = NotixColors.Fondo,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("• Formato: ${formato.nombre}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NotixColors.Tinta)
                            Text("• Tipo de Firma: ${formato.firmaRequerida.etiqueta}", fontSize = 13.sp, color = NotixColors.Tinta)
                            Text("• Solicitante: ${vm.nombreCompleto}", fontSize = 13.sp, color = NotixColors.Tinta)
                            Text("• RUN: ${vm.run}", fontSize = 13.sp, color = NotixColors.Tinta)
                            Text("• Correo: ${vm.correo}", fontSize = 13.sp, color = NotixColors.Tinta)
                            Text("• Comuna: ${vm.comuna}", fontSize = 13.sp, color = NotixColors.Tinta)
                            if (vm.actuaEnNombreDe.isNotBlank()) {
                                Text("• En representación de: ${vm.actuaEnNombreDe}", fontSize = 13.sp, color = NotixColors.Tinta)
                            }
                            Text(
                                "• Archivo adjunto: ${vm.documentoAdjuntoNombre ?: "${formato.nombre.lowercase().replace(" ", "_")}_solicitud.pdf"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NotixColors.Brand
                            )
                        }
                    }

                    Text(
                        "Nota de firma: La confirmación de firma realizada en esta app corresponde a una simulación académica.",
                        fontSize = 11.sp,
                        color = NotixColors.Gris,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarModalConfirmacion = false
                        vm.guardarTramite(onExito = onTramiteCreado)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NotixColors.Brand)
                ) {
                    Text("Confirmar y enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarModalConfirmacion = false }) {
                    Text("Volver a editar", color = NotixColors.Gris)
                }
            }
        )
    }
}

private fun obtenerNombreArchivo(context: Context, uri: Uri): String? {
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) return cursor.getString(nameIndex)
            }
        }
    }
    return uri.path?.substringAfterLast('/')
}
