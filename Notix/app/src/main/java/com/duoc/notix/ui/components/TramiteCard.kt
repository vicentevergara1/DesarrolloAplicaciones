package com.duoc.notix.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.notix.data.model.EstadoTramite
import com.duoc.notix.data.model.TipoFirma
import com.duoc.notix.data.model.Tramite
import com.duoc.notix.ui.theme.NotixColors
import com.duoc.notix.util.fechaCorta
import androidx.compose.runtime.Composable

@Composable
fun TramiteCard(
    tramite: Tramite,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, NotixColors.Borde)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(tramite.id, color = NotixColors.Gris, fontSize = 14.sp)
                EstadoChip(tramite.estado)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                tramite.formato.nombre,
                color = NotixColors.Tinta, fontSize = 20.sp, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(subtitulo(tramite), color = NotixColors.Gris, fontSize = 14.sp)

            if (tramite.estado == EstadoTramite.DEVUELTO && !tramite.observacion.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NotixColors.AlertaFondo,
                    border = BorderStroke(1.dp, NotixColors.AlertaBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.WarningAmber,
                                contentDescription = null,
                                tint = NotixColors.AlertaTexto,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Motivo de devolución:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NotixColors.AlertaTexto
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            tramite.observacion,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = NotixColors.Tinta
                        )
                    }
                }
            }
        }
    }
}

private fun subtitulo(t: Tramite): String {
    val firma = when (t.tipoFirma) {
        TipoFirma.SIMPLE -> "Firma simple"
        TipoFirma.AVANZADA -> "Firma avanzada (simulada)"
        TipoFirma.NOTARIADA -> "Firma notariada (simulada)"
    }
    val n = t.documentos.size
    val docs = "$n documento${if (n == 1) "" else "s"}"
    val fecha = fechaCorta(t.historial.firstOrNull()?.fechaHora)
    return listOf(firma, docs, fecha).filter { it.isNotBlank() }.joinToString(" · ")
}

@Composable
fun EstadoChip(estado: EstadoTramite, modifier: Modifier = Modifier) {
    val (texto, icono, fondo, color) = when (estado) {
        EstadoTramite.ENVIADO -> ChipDatos(
            "En revisión", Icons.Outlined.Schedule, NotixColors.RevisionFondo, NotixColors.RevisionTexto
        )
        EstadoTramite.DEVUELTO -> ChipDatos(
            "Con observaciones", Icons.Outlined.WarningAmber, NotixColors.AlertaFondo, NotixColors.AlertaTexto
        )
        EstadoTramite.FINALIZADO -> ChipDatos(
            "Finalizado", Icons.Filled.Check, NotixColors.FinalizadoFondo, NotixColors.FinalizadoTexto
        )
    }
    Surface(modifier = modifier, shape = RoundedCornerShape(50), color = fondo) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(texto, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private data class ChipDatos(val texto: String, val icono: ImageVector, val fondo: Color, val color: Color)