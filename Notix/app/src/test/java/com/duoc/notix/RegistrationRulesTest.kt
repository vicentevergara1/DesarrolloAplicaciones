package com.duoc.notix

import com.duoc.notix.domain.*
import org.junit.Assert.*
import org.junit.Test

class RegistrationRulesTest {
    @Test fun allThreeExamplesPassTheirOwnValidation() {
        AccountType.entries.forEach { type ->
            assertTrue(type.label, RegistrationRules.validate(type, RegistrationRules.example(type),
                RegistrationRules.DEMO_PASSWORD, RegistrationRules.DEMO_PASSWORD).isEmpty())
        }
    }
    @Test fun numericZeroAndKDigitsDependOnTheBody() {
        listOf("11.111.111-1", "22.222.222-2", "22.312.422-4", "10.000.004-0", "12.345.670-K", "12.345.670-k").forEach {
            assertTrue(it, RegistrationRules.validRut(it))
        }
        assertFalse(RegistrationRules.validRut("22.312.422-1"))
        assertFalse(RegistrationRules.validRut("22.312.422-K"))
        assertFalse(RegistrationRules.validRut("11.111.111-K"))
    }

    @Test fun incorrectVerifierExplainsTheExpectedDigit() {
        val expected = "Para este identificador ficticio, el dígito verificador esperado es 4."
        assertEquals(expected, RegistrationRules.rutError("22.312.422-1"))
        assertEquals(expected, RegistrationRules.rutError("22.312.422-K"))
        assertNull(RegistrationRules.rutError("22.312.422-4"))
    }

    @Test fun blankPasswordsAreRejectedEvenWhenLongEnough() {
        for (password in listOf("      ", "        ", " ".repeat(20))) {
            assertTrue(RegistrationRules.validate(AccountType.PERSON, RegistrationRules.example(AccountType.PERSON),
                password, password).containsKey("password"))
        }
    }
    @Test fun wrongCompanyCheckDigitIsRejected() {
        assertFalse(RegistrationRules.validRut("76.111.111-K"))
        assertTrue(RegistrationRules.validRut("76.111.111-6"))
        assertFalse(RegistrationRules.validRut("K1.111.111-1"))
    }
    @Test fun formattingKeepsTheCheckDigitAndDocumentLength() {
        assertEquals("11.111.111-1", RegistrationRules.format("rut", "11111111-1"))
        assertEquals("123.456.789", RegistrationRules.format("doc", "123456789"))
        assertEquals("000000000", RegistrationRules.format("telefono", "000000000X"))
    }
    @Test fun companyRequiresRepresentativeDetails() {
        val values = RegistrationRules.example(AccountType.COMPANY) - "rep_run" - "rep_doc"
        val errors = RegistrationRules.validate(AccountType.COMPANY, values, "prueba123", "prueba123")
        assertTrue(errors.containsKey("rep_run"))
        assertTrue(errors.containsKey("rep_doc"))
    }
    @Test fun notaryNumberAndPasswordConfirmationAreValidated() {
        val values = RegistrationRules.example(AccountType.NOTARY) + ("notaria_n" to "0")
        val errors = RegistrationRules.validate(AccountType.NOTARY, values, "prueba123", "otra1234")
        assertTrue(errors.containsKey("notaria_n"))
        assertTrue(errors.containsKey("confirmation"))
    }
}
