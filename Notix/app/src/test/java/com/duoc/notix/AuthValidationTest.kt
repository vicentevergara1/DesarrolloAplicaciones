package com.duoc.notix

import com.duoc.notix.domain.AuthValidation
import org.junit.Assert.*
import org.junit.Test

class AuthValidationTest {
    @Test fun registrationRejectsEmptyFields() {
        val errors = AuthValidation.validate(true, "", "", "", "")
        assertNotNull(errors.name)
        assertNotNull(errors.email)
        assertNotNull(errors.password)
    }

    @Test fun registrationRejectsMalformedEmailAndMismatchingPassword() {
        val errors = AuthValidation.validate(true, "Persona Demo", "demo@", "prueba123", "otra1234")
        assertNotNull(errors.email)
        assertNotNull(errors.confirmation)
    }

    @Test fun passwordLengthIsEnforcedAndWhitespaceIsNotSilentlyRemoved() {
        assertNotNull(AuthValidation.validate(true, "Demo", "demo@example.com", "1234567", "1234567").password)
        assertNotNull(AuthValidation.validate(true, "Demo", "demo@example.com", " ".repeat(8), " ".repeat(8)).password)
        assertFalse(AuthValidation.validate(true, "Demo", "demo@example.com", " prueba1 ", " prueba1 ").hasErrors)
    }

    @Test fun validRegistrationAcceptsNormalizedEmail() {
        assertFalse(AuthValidation.validate(true, "Demo", " DEMO@EXAMPLE.COM ", "prueba123", "prueba123").hasErrors)
        assertEquals("demo@example.com", AuthValidation.normalizeEmail(" DEMO@EXAMPLE.COM "))
    }

    @Test fun loginDoesNotRequireRegistrationFields() {
        assertFalse(AuthValidation.validate(false, "", "demo@example.com", "prueba123", "").hasErrors)
    }
}
