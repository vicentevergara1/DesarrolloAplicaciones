package com.duoc.notix.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.notix.model.FormatoTramite
import com.duoc.notix.model.catalogoFormatos
import com.duoc.notix.ui.theme.NotixColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoFormatosScreen(
    onSeleccionarFormato: (FormatoTramite) -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        containerColor = NotixColors.Fondo,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Seleccionar formato",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "encabezado") {
                Column {
                    Text(
                        "¿Qué trámite deseas realizar?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = NotixColors.Tinta
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Selecciona un formato del catálogo para comenzar tu solicitud digital.",
                        fontSize = 14.sp,
                        color = NotixColors.Gris
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            items(catalogoFormatos, key = { it.id }) { formato ->
                FormatoItemCard(
                    formato = formato,
                    onClick = { onSeleccionarFormato(formato) }
                )
            }
        }
    }
}

@Composable
private fun FormatoItemCard(
    formato: FormatoTramite,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, NotixColors.Borde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NotixColors.Fondo,
                modifier = Modifier.padding(end = 14.dp)
            ) {
                Icon(
                    Icons.Outlined.Description,
                    contentDescription = null,
                    tint = NotixColors.Brand,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = NotixColors.RevisionFondo,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = formato.categoria,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NotixColors.RevisionTexto,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = formato.nombre,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = NotixColors.Tinta
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "Firma requerida: ${formato.firmaRequerida.etiqueta}",
                    fontSize = 13.sp,
                    color = NotixColors.Gris
                )
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Seleccionar",
                tint = NotixColors.Gris
            )
        }
    }
}
