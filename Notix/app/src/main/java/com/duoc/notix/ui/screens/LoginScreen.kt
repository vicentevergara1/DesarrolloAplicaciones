package com.duoc.notix.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.notix.model.TipoCuenta
import com.duoc.notix.model.Usuario
import com.duoc.notix.ui.components.notixTextFieldColors
import com.duoc.notix.ui.theme.NotixColors
import com.duoc.notix.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (Usuario) -> Unit,
    onCreateAccount: () -> Unit,
    vm: LoginViewModel = viewModel()
) {
    var perfil by remember { mutableStateOf(TipoCuenta.PERSONA) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var verClave by remember { mutableStateOf(false) }

    fun ingresar() = vm.ingresar(perfil, email, password, onLoginSuccess)

    Surface(modifier = Modifier.fillMaxSize(), color = NotixColors.Fondo) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            Text("NotariaX", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NotixColors.Brand)
            Text(
                "Tus trámites notariales, siempre a la vista",
                fontSize = 26.sp, lineHeight = 32.sp,
                fontWeight = FontWeight.Bold, color = NotixColors.Brand
            )
            Text(
                "Carga documentos, firma y sigue cada trámite sin ir a la notaría ni llamar por teléfono.",
                fontSize = 14.sp, color = NotixColors.Gris
            )

            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFE8F1EF)) {
                Text(
                    "Simulación académica. Usa solo datos ficticios.",
                    modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = NotixColors.Brand
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TipoCuenta.entries.forEach { tipo ->
                    val seleccionado = tipo == perfil
                    Surface(
                        onClick = { perfil = tipo },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (seleccionado) NotixColors.Brand else Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                tipo.etiqueta,
                                fontSize = 13.sp,
                                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                                color = if (seleccionado) Color.White else NotixColors.Brand
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = email,
                modifier = Modifier.fillMaxWidth().testTag("correo_login"),
                onValueChange = { email = it },
                label = { Text("Correo") },
                placeholder = { Text("ej. ana.demo@notariax.test") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = notixTextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )

            OutlinedTextField(
                value = password,
                modifier = Modifier.fillMaxWidth().testTag("clave_login"),
                onValueChange = { password = it },
                label = { Text("Ingresa tu clave") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = notixTextFieldColors(),
                visualTransformation =
                    if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    TextButton(
                        onClick = { verClave = !verClave },
                        colors = ButtonDefaults.textButtonColors(contentColor = NotixColors.Brand)
                    ) {
                        Text(if (verClave) "Ocultar" else "Ver", fontSize = 12.sp)
                    }
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth().testTag("recordar_sesion").toggleable(
                    value = vm.recordar,
                    enabled = !vm.cargando,
                    role = Role.Checkbox,
                    onValueChange = vm::onRecordarChange
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = vm.recordar, onCheckedChange = null, enabled = !vm.cargando)
                Text("Recordar sesión", color = NotixColors.Tinta, fontSize = 14.sp)
            }

            Button(
                onClick = ::ingresar,
                enabled = !vm.cargando,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NotixColors.Brand)
            ) {
                Text("Ingresar", fontWeight = FontWeight.SemiBold)
            }

            vm.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = ::ingresar,
                enabled = !vm.cargando,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, NotixColors.Borde)
            ) {
                Text("Simular huella digital", color = NotixColors.Brand)
            }

            Text(
                "La verificación biométrica es solo simulación. No se usa el sensor del dispositivo.",
                fontSize = 11.sp, color = NotixColors.Gris,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¿No tienes cuenta?", fontSize = 13.sp)
                TextButton(onClick = onCreateAccount) {
                    Text("Crear cuenta", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NotixColors.Brand)
                }
            }
        }
    }
}
