package com.duoc.notix

import com.duoc.notix.data.validation.RutValidator
import com.duoc.notix.util.limpiarRut
import com.duoc.notix.ui.components.RutVisualTransformation
import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.*
import org.junit.Test

class RutValidatorTest {
    @Test fun verificadoresNumericosCeroYK() {
        listOf("11.111.111-1", "22.222.222-2", "22.312.422-4", "76.111.111-6", "10.000.004-0", "12.345.670-K", "12345670k").forEach {
            assertNull(it, RutValidator.validar(it))
        }
    }
    @Test fun avisaElVerificadorEsperadoAlEditar() {
        assertTrue(RutValidator.validar("22.312.422-1")!!.contains("4"))
        assertTrue(RutValidator.validar("22.312.422-K")!!.contains("4"))
        assertNull(RutValidator.validar("22.312.422-4"))
    }
    @Test fun rechazaCuerposInvalidos() {
        listOf("", "1", "00000000", "1111111111", "123K5678", "1234567X", "１２３４５６７８").forEach {
            assertNotNull(it, RutValidator.validar(it))
        }
    }
    @Test fun formatoYCursorSonValidosDuranteLaEscritura() {
        assertEquals("12345670K", limpiarRut("12.345.670-k"))
        for (raw in listOf("", "1", "12", "12345678", "12345670K")) {
            val t = RutVisualTransformation().filter(AnnotatedString(raw))
            for (i in 0..raw.length) assertTrue(t.offsetMapping.originalToTransformed(i) in 0..t.text.length)
            for (i in 0..t.text.length) assertTrue(t.offsetMapping.transformedToOriginal(i) in 0..raw.length)
        }
    }
}
