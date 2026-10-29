package com.duoc.notix.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.duoc.notix.ui.theme.NotixColors

private data class ItemBarra(val etiqueta: String, val icono: ImageVector, val onClick: () -> Unit)

@Composable
fun BarraInferiorPersona(
    seleccionado: Int,
    onInicio: () -> Unit,
    onTramites: () -> Unit,
    onAvisos: () -> Unit,
    onPerfil: () -> Unit
) {
    val items = listOf(
        ItemBarra("Inicio", Icons.Outlined.Home, onInicio),
        ItemBarra("Trámites", Icons.Outlined.Description, onTramites),
        ItemBarra("Avisos", Icons.Outlined.Notifications, onAvisos),
        ItemBarra("Perfil", Icons.Outlined.Person, onPerfil)
    )
    NavigationBar(containerColor = Color.White) {
        items.forEachIndexed { i, item ->
            NavigationBarItem(
                selected = i == seleccionado,
                onClick = item.onClick,
                icon = { Icon(item.icono, contentDescription = item.etiqueta) },
                label = { Text(item.etiqueta) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NotixColors.Brand,
                    selectedTextColor = NotixColors.Brand,
                    indicatorColor = NotixColors.Fondo,
                    unselectedIconColor = NotixColors.Gris,
                    unselectedTextColor = NotixColors.Gris
                )
            )
        }
    }
}
