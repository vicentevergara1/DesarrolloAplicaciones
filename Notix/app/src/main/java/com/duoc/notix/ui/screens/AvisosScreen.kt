package com.duoc.notix.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.notix.data.model.Aviso
import com.duoc.notix.ui.components.BarraInferiorPersona
import com.duoc.notix.ui.theme.NotixColors
import com.duoc.notix.viewmodel.AvisosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvisosScreen(
    usuarioId: String,
    onVolver: () -> Unit,
    vm: AvisosViewModel = viewModel(
        key = usuarioId,
        factory = AvisosViewModel.factory(LocalContext.current, usuarioId)
    )
) {
    val avisos by vm.avisos.collectAsState()

    Scaffold(
        containerColor = NotixColors.Fondo,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notificaciones y Avisos",
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
        },
        bottomBar = {
            BarraInferiorPersona(
                seleccionado = 2,
                onInicio = onVolver,
                onTramites = onVolver,
                onAvisos = {},
                onPerfil = {}
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (avisos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No tienes notificaciones por el momento.",
                        color = NotixColors.Gris,
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(avisos, key = { it.id }) { aviso ->
                        AvisoCard(
                            aviso = aviso,
                            onClick = { vm.marcarComoLeido(aviso.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvisoCard(
    aviso: Aviso,
    onClick: () -> Unit
) {
    val esAvisoDevuelto = aviso.mensaje.contains("devuelto", ignoreCase = true) || aviso.mensaje.contains("atención", ignoreCase = true)
    val esAvisoAprobado = aviso.mensaje.contains("aprobado", ignoreCase = true) || aviso.mensaje.contains("finalizado", ignoreCase = true)

    val (icono, tint) = when {
        esAvisoDevuelto -> Pair(Icons.Outlined.WarningAmber, NotixColors.AlertaTexto)
        esAvisoAprobado -> Pair(Icons.Outlined.CheckCircle, NotixColors.Brand)
        else -> Pair(Icons.Outlined.Notifications, NotixColors.Brand)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (!aviso.leido) NotixColors.Brand else NotixColors.Borde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = NotixColors.Fondo,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icono,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (esAvisoDevuelto) "Atención requerida" else if (esAvisoAprobado) "Trámite Aprobado" else "Actualización de trámite",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (esAvisoDevuelto) NotixColors.AlertaTexto else NotixColors.Gris
                    )

                    if (!aviso.leido) {
                        Surface(
                            shape = CircleShape,
                            color = NotixColors.Punto,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = aviso.mensaje,
                    fontSize = 14.sp,
                    fontWeight = if (!aviso.leido) FontWeight.Bold else FontWeight.Normal,
                    color = NotixColors.Tinta
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = aviso.fechaHora,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = NotixColors.Gris
                )
            }
        }
    }
}
