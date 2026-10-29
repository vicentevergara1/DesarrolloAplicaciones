package com.duoc.notix.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.notix.util.limpiarRut
import com.duoc.notix.data.validation.RutValidator
import com.duoc.notix.ui.components.DocumentoVisualTransformation
import com.duoc.notix.ui.components.FormatoVisualTransformation
import com.duoc.notix.ui.components.RutVisualTransformation
import com.duoc.notix.util.limpiarDocumento
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.model.Usuario
import com.duoc.notix.ui.components.notixTextFieldColors
import com.duoc.notix.viewmodel.AuthViewModel

private val Brand = Color(0xFF0F4C45)
private val Fondo = Color(0xFFEEF4F1)
private val Morado = Color(0xFF5B3FC4)
private val MoradoFondo = Color(0xFFECE8FB)
private val Tinta = Color(0xFF12302B)
private val Gris = Color(0xFF5B726C)

// ---------------------------------------------------------------------------
// Definición de campos según el tipo de cuenta
// ---------------------------------------------------------------------------

private class CampoDef(
    val key: String,
    val titulo: String,
    val placeholder: String,
    val teclado: KeyboardType = KeyboardType.Text,
    val ayuda: String? = null,
    val formato: (String) -> String = { it },
    val validar: (String) -> String?,
    val visual: VisualTransformation = VisualTransformation.None
)

private fun texto(min: Int, msg: String): (String) -> String? =
    { if (it.trim().length < min) msg else null }

private fun soloDigitos(v: String, max: Int) = v.filter { it.isDigit() }.take(max)

private fun validarDocumento(valor: String): String? =
    if (soloDigitos(valor, 9).length == 9) null else "Ingresa los 9 dígitos del N° de documento."

private fun validarCorreo(valor: String): String? =
    if (android.util.Patterns.EMAIL_ADDRESS.matcher(valor.trim()).matches()) null
    else "Ingresa un correo válido."

private fun validarTelefono(valor: String): String? =
    if (soloDigitos(valor, 9).length == 9) null else "Ingresa 9 dígitos, ej. 912345678."

private fun validarNumeroNotaria(valor: String): String? {
    val n = soloDigitos(valor, 3).toIntOrNull()
    return if (n == null || n < 1) "Ingresa el N° de notaría (1 a 999)." else null
}

private const val AYUDA_RUN = "Ingresa tu RUT."
private const val AYUDA_DOC = "Número de serie de la cédula, ej. 123.456.789."

private fun runDef(
    key: String,
    titulo: String,
    placeholder: String = "11.111.111-1",
    ayuda: String = AYUDA_RUN,
    etiqueta: String = "RUN"
) = CampoDef(
    key, titulo, placeholder, KeyboardType.Ascii, ayuda,
    formato = ::limpiarRut,
    validar = { RutValidator.validar(it, etiqueta) },
    visual = RutVisualTransformation()
)

private fun docDef(key: String, titulo: String) = CampoDef(
    key, titulo, "123.456.789", KeyboardType.Number, AYUDA_DOC,
    formato = ::limpiarDocumento,
    validar = ::validarDocumento,
    visual = DocumentoVisualTransformation()
)

private fun correoDef(titulo: String) = CampoDef(
    "correo", titulo, "EjemploEmail@Gmail.com", KeyboardType.Email, validar = ::validarCorreo
)

private fun telefonoDef() = CampoDef(
    "telefono", "Teléfono de contacto", "912345678", KeyboardType.Phone,
    formato = { soloDigitos(it, 9) }, validar = ::validarTelefono
)

private fun camposPara(tipo: TipoCuenta): List<CampoDef> = when (tipo) {
    TipoCuenta.PERSONA -> listOf(
        CampoDef("nombre", "Nombre completo", "Ana Demo Prueba",
            validar = texto(3, "Ingresa un nombre.")),
        runDef("rut", "RUT", etiqueta = "RUT"),
        docDef("doc", "N° de documento"),
        correoDef("Correo electrónico"),
        telefonoDef(),
        CampoDef("comuna", "Comuna de residencia", "Ñuñoa",
            validar = texto(3, "Ingresa tu comuna."))
    )

    TipoCuenta.EMPRESA -> listOf(
        CampoDef("razon", "Nombre Empresa", "Comercial Demo SpA",
            validar = texto(3, "Ingresa el nombre de la empresa")),
        runDef("rut", "RUT de la empresa", "76.111.111-K",
            ayuda = "Ingresa el RUT de la empresa", etiqueta = "RUT"),
        CampoDef("giro", "Giro o actividad comercial", "Venta de insumos",
            validar = texto(3, "Ingresa el giro de la empresa.")),
        CampoDef("rep_nombre", "Nombre del representante legal", "Pedro Demo Prueba",
            validar = texto(3, "Ingresa el nombre del representante.")),
        runDef("rep_run", "RUN del representante"),
        docDef("rep_doc", "N° de documento del representante"),
        correoDef("Correo de contacto"),
        telefonoDef(),
        CampoDef("comuna", "Comuna del domicilio comercial", "Providencia",
            validar = texto(3, "Ingresa la comuna."))
    )

    TipoCuenta.NOTARIO -> listOf(
        CampoDef("nombre", "Nombre del notario", "Luis Demo Prueba",
            validar = texto(3, "Ingresa un nombre.")),
        runDef("run", "RUN"),
        docDef("doc", "N° de documento"),
        CampoDef("notaria_n", "N° de notaría", "12", KeyboardType.Number,
            formato = { soloDigitos(it, 3) }, validar = ::validarNumeroNotaria),
        CampoDef("notaria_comuna", "Comuna de la notaría", "Santiago",
            validar = texto(3, "Ingresa la comuna de la notaría.")),
        correoDef("Correo institucional"),
        telefonoDef()
    )
}

// ---------------------------------------------------------------------------
// Pantalla
// ---------------------------------------------------------------------------

/**
 * Pantalla "Crear tu cuenta" (mapa de navegación: 1c y 1d).
 * Los campos cambian según el tipo de cuenta. Registrarse simula la creación
 * de la cuenta: no guarda ningún dato real.
 *
 * @param onVolver     flecha atrás -> Ingreso
 * @param onIngresar   "¿Ya tienes cuenta? Ingresar" -> Ingreso
 * @param onRegistrado Persona/Empresa -> Mis trámites, Notario -> Bandeja del notario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearCuentaScreen(
    onVolver: () -> Unit = {},
    onIngresar: () -> Unit = {},
    onRegistrado: (Usuario) -> Unit = {},
    vm: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var tipo by rememberSaveable { mutableStateOf(TipoCuenta.PERSONA) }
    var valores by rememberSaveable { mutableStateOf<Map<String, String>>(emptyMap()) }
    var pass by rememberSaveable { mutableStateOf("") }
    var pass2 by rememberSaveable { mutableStateOf("") }
    var intento by rememberSaveable { mutableStateOf(false) }
    var registrado by rememberSaveable { mutableStateOf(false) }

    val campos = camposPara(tipo)
    val errores = campos.associate { it.key to it.validar(valores[it.key].orEmpty()) }
    val errPass = if (pass.length < 6) "Debe tener al menos 6 caracteres." else null
    val errPass2 = if (pass2.isEmpty() || pass2 != pass) "Las contraseñas no coinciden." else null
    val valido = errores.values.all { it == null } && errPass == null && errPass2 == null

    val focus = LocalFocusManager.current
    val siguiente = { focus.moveFocus(FocusDirection.Down); Unit }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onVolver) {
                Text("‹", color = Brand, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
            Text("NotariaX", color = Brand, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Surface(
                shape = RoundedCornerShape(50),
                color = MoradoFondo,
                border = BorderStroke(1.dp, Morado)
            ) {
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "Crea tu cuenta",
            color = Tinta,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        )
        // Tipo de cuenta (arriba, porque define qué campos se piden)
        Text(
            "Tipo de cuenta",
            color = Tinta, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            TipoCuenta.entries.forEachIndexed { i, t ->
                SegmentedButton(
                    selected = tipo == t,
                    onClick = {
                        tipo = t
                        intento = false
                        registrado = false
                    },
                    shape = SegmentedButtonDefaults.itemShape(i, TipoCuenta.entries.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = Brand,
                        activeContentColor = Color.White,
                        inactiveContainerColor = Color.White,
                        inactiveContentColor = Gris
                    )
                ) { Text(t.etiqueta) }
            }
        }

        // Campos propios del tipo de cuenta
        campos.forEach { c ->
            key(tipo, c.key) {
                Campo(
                    titulo = c.titulo,
                    valor = valores[c.key].orEmpty(),
                    onCambio = { valores = valores + (c.key to c.formato(it)) },
                    placeholder = c.placeholder,
                    ayuda = c.ayuda,
                    error = errores[c.key].takeIf { intento },
                    teclado = c.teclado,
                    ime = ImeAction.Next,
                    onNext = siguiente,
                    visual = c.visual
                )
            }
        }

        // Contraseña (común a todos)
        Campo(
            titulo = "Contraseña",
            valor = pass, onCambio = { pass = it },
            placeholder = "Mínimo 6 caracteres",
            error = errPass.takeIf { intento },
            teclado = KeyboardType.Password, ime = ImeAction.Next,
            esPassword = true,
            onNext = siguiente
        )
        Campo(
            titulo = "Confirmar contraseña",
            valor = pass2, onCambio = { pass2 = it },
            placeholder = "",
            error = errPass2.takeIf { intento },
            teclado = KeyboardType.Password, ime = ImeAction.Done,
            esPassword = true,
            onNext = { focus.clearFocus() }
        )

        // Registrarse
// Registrarse
        Button(
            onClick = {
                intento = true
                if (valido) vm.registrar(tipo, valores, pass) {
                    usuario -> registrado = true
                    onRegistrado(usuario)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White)
        ) {
            Text("Registrarse", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }

        vm.errorRegistro?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error, fontSize = 14.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        if (intento && !valido) {
            Text(
                "Revisa los campos marcados en rojo.",
                color = MaterialTheme.colorScheme.error, fontSize = 14.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        if (registrado) {
            val destino = if (tipo == TipoCuenta.NOTARIO) "Bandeja del notario" else "Mis trámites"
            Text(
                "Cuenta creada correctamente ($destino).",
                color = Brand, fontSize = 14.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¿Ya tienes cuenta?", color = Gris, fontSize = 14.sp)
            TextButton(onClick = onIngresar) {
                Text("Ingresar", color = Brand, fontWeight = FontWeight.Bold)
            }
        }
    }   // ← cierra el Column
}       // ← cierra CrearCuentaScreen

        @Composable
        private fun Campo(
            titulo: String,
            valor: String,
            onCambio: (String) -> Unit,
            placeholder: String,
            error: String?,
            teclado: KeyboardType,
            ime: ImeAction,
            onNext: () -> Unit,
            esPassword: Boolean = false,
            ayuda: String? = null,
            visual: VisualTransformation = VisualTransformation.None
        ) {
            Text(
                titulo,
                color = Tinta, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
            )
            OutlinedTextField(
                value = valor,
                onValueChange = onCambio,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                placeholder = if (placeholder.isNotEmpty()) ({
                    Text(
                        placeholder,
                        color = Gris
                    )
                }) else null,
                isError = error != null,
                supportingText = { Text(error ?: ayuda ?: "") },
                visualTransformation = if (esPassword) PasswordVisualTransformation() else visual,
                keyboardOptions = KeyboardOptions(keyboardType = teclado, imeAction = ime),
                keyboardActions = KeyboardActions(onNext = { onNext() }, onDone = { onNext() }),
                colors = notixTextFieldColors()
            )
        }

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CrearCuentaPreview() {
    MaterialTheme { CrearCuentaScreen() }
}