package com.crabscode.towerfortwo

import com.crabscode.towerfortwo.model.AppSettings
import com.crabscode.towerfortwo.model.PlayerGender
import com.crabscode.towerfortwo.model.SexPositionCatalog
import com.crabscode.towerfortwo.model.SexualPractice
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SexPositionCatalogTest {
    @Test
    fun levelSevenDoesNotOfferPenetration() {
        val settings = AppSettings(allowSexualPractices = true)
        val practices = SexPositionCatalog.eligiblePractices(7, settings)
        assertTrue(SexualPractice.CARESSES_INTIMES in practices)
        assertTrue(SexualPractice.MASTURBATION in practices)
        assertFalse(SexualPractice.PENETRATION in practices)
    }

    @Test
    fun disabledPracticeIsNeverEligible() {
        val settings = AppSettings(
            allowSexualPractices = true,
            allowedSexualPractices = setOf(SexualPractice.CARESSES_INTIMES),
        )
        val practices = SexPositionCatalog.eligiblePractices(10, settings)
        assertTrue(practices == listOf(SexualPractice.CARESSES_INTIMES))
    }

    @Test
    fun standingPositionsCanBeDisabled() {
        val settings = AppSettings(
            allowSexualPractices = true,
            allowStandingSexPositions = false,
        )
        val positions = SexPositionCatalog.positionsFor(SexualPractice.SEXE_ORAL, 10, settings)
        assertTrue(positions.none { it.standing })
    }

    @Test
    fun genderParsingHasSafeFallback() {
        assertTrue(PlayerGender.fromName("FEMME") == PlayerGender.FEMME)
        assertTrue(PlayerGender.fromName("unknown") == PlayerGender.NON_PRECISE)
    }
}
