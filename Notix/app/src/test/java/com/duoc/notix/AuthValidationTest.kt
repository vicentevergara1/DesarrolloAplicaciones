package com.duoc.notix

import com.duoc.notix.domain.AuthValidation
import org.junit.Assert.*
import org.junit.Test

class AuthValidationTest {
    @Test fun emailNormalizationIgnoresOuterSpacesAndCase() {
        assertEquals("demo@example.com", AuthValidation.normalizeEmail(" DEMO@EXAMPLE.COM "))
    }
    @Test fun malformedEmailsAreRejected() {
        listOf("", "demo", "demo@", "@example.com", "demo@.com", "demo @example.com").forEach {
            assertNotNull(it, AuthValidation.emailError(it))
        }
    }
    @Test fun validFictionalAddressesAreAccepted() {
        assertNull(AuthValidation.emailError("persona.demo@example.com"))
        assertNull(AuthValidation.emailError(" DEMO@EXAMPLE.COM "))
    }
    @Test fun passwordsPreserveSignificantSpacesButRejectOnlyWhitespace() {
        assertNotNull(AuthValidation.passwordError("        ", true))
        assertNull(AuthValidation.passwordError(" prueba1 ", true))
        assertNotNull(AuthValidation.passwordError("x".repeat(129), false))
    }
    @Test fun loginAllowsOlderPasswordsWithoutRegistrationFields() {
        assertTrue(AuthValidation.loginErrors("demo@example.com", "antigua").isEmpty())
        assertTrue(AuthValidation.loginErrors("", "").isNotEmpty())
    }
}
