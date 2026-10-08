package com.example.shared.domain.money

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class MoneyInvariantTest {
    private val bdt = CurrencyUnit("BDT")

    @Test fun minorUnitsRemainExactBeyondDoublePrecision() {
        listOf(Long.MIN_VALUE, Long.MAX_VALUE, 9007199254740993L, -1L, 0L).forEach {
            val value = Money(it, bdt)
            assertEquals(value, Money.parseDecimal(value.toDecimalString(), bdt))
        }
        assertEquals(30L, (Money.parseDecimal("0.10", bdt)!! + Money.parseDecimal("0.20", bdt)!!).minorUnits)
    }

    @Test fun banglaInputAndAmbiguityHaveTheSameContractOnNative() {
        assertEquals(35640L, Money.parseDecimal("৩৫৬.৪০", bdt)!!.minorUnits)
        listOf("1,500", "1.001", "NaN", "1e3", "92233720368547758.08").forEach { assertNull(Money.parseDecimal(it, bdt)) }
    }

    @Test fun arithmeticRejectsOverflowAndCurrencyMismatch() {
        assertFailsWith<IllegalArgumentException> { Money(Long.MAX_VALUE, bdt) + Money(1, bdt) }
        assertFailsWith<IllegalArgumentException> { Money(Long.MIN_VALUE, bdt) - Money(1, bdt) }
        assertFailsWith<IllegalArgumentException> { Money(100, bdt) + Money(100, CurrencyUnit("USD")) }
        assertEquals(Money(0, bdt), Money(Long.MIN_VALUE, bdt) - Money(Long.MIN_VALUE, bdt))
    }
}
