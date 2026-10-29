package com.duoc.notix.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.duoc.notix.util.formatearDocumento
import com.duoc.notix.util.formatearRut

/** Muestra el valor formateado y mantiene el cursor en su lugar. Sirve para cualquier formato que solo agregue '.' y '-'. */
open class FormatoVisualTransformation(
    private val formatear: (String) -> String
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val formateado = formatear(text.text)
        val posiciones = formateado.indices.filter { formateado[it] != '.' && formateado[it] != '-' }

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset <= 0) 0
                else posiciones.getOrNull(offset - 1)?.plus(1) ?: formateado.length

            override fun transformedToOriginal(offset: Int): Int =
                formateado.take(offset).count { it != '.' && it != '-' }
        }
        return TransformedText(AnnotatedString(formateado), mapping)
    }
}

class RutVisualTransformation : FormatoVisualTransformation(::formatearRut)
class DocumentoVisualTransformation : FormatoVisualTransformation(::formatearDocumento)