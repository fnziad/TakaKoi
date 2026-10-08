package com.example

import com.example.shared.domain.money.CurrencyUnit
import com.example.shared.domain.money.Money
import org.junit.Assert.*
import org.junit.Test

class MoneyTest {
    private val bdt = CurrencyUnit("BDT")

    @Test fun decimalMathIsExact() {
        assertEquals(Money(30, bdt), Money.parseDecimal("0.10", bdt)!! + Money.parseDecimal("0.20", bdt)!!)
        assertEquals("-0.01", (Money(29, bdt) - Money(30, bdt)).toDecimalString())
    }

    @Test fun banglaAndMixedDigitsParseWithoutFloatingPoint() {
        assertEquals(35640L, Money.parseDecimal(" ৩৫৬.৪০ ", bdt)!!.minorUnits)
        assertEquals(135L, Money.parseDecimal("১.3৫", bdt)!!.minorUnits)
        assertEquals(50000L, Money.parseDecimal("500", bdt)!!.minorUnits)
    }

    @Test fun ambiguousOrInvalidCaptureInputIsRejected() {
        listOf("", "-", ".50", "1.", "1,500", "1e3", "NaN", "Infinity", "৳50", "+5", "1.001", "1..2", "--1", "1 2", "١٢", "0".repeat(65)).forEach {
            assertNull(it, Money.parseDecimal(it, bdt))
        }
    }

    @Test fun fullInt64BoundariesRoundTrip() {
        listOf(Long.MIN_VALUE, Long.MAX_VALUE, 9007199254740993L, -1L, 0L).forEach {
            val money = Money(it, bdt)
            assertEquals(money, Money.parseDecimal(money.toDecimalString(), bdt))
        }
        assertNull(Money.parseDecimal("92233720368547758.08", bdt))
        assertNull(Money.parseDecimal("-92233720368547758.09", bdt))
    }

    @Test fun zeroAndThreeDecimalCurrenciesAreExplicit() {
        val jpy = CurrencyUnit("JPY", 0)
        val kwd = CurrencyUnit("KWD", 3)
        assertEquals("150", Money.parseDecimal("150", jpy)!!.toDecimalString())
        assertNull(Money.parseDecimal("150.0", jpy))
        assertEquals("1.250", Money.parseDecimal("1.25", kwd)!!.toDecimalString())
        assertEquals("0.00", Money.parseDecimal("-0.00", bdt)!!.toDecimalString())
    }

    @Test fun leadingZerosDoNotHideOverflow() {
        assertEquals(100L, Money.parseDecimal("0001", bdt)!!.minorUnits)
        assertNull(Money.parseDecimal("00092233720368547758.08", bdt))
    }

    @Test fun additionAndSubtractionFailRatherThanWrap() {
        rejects { Money(Long.MAX_VALUE, bdt) + Money(1, bdt) }
        rejects { Money(Long.MIN_VALUE, bdt) + Money(-1, bdt) }
        rejects { Money(Long.MIN_VALUE, bdt) - Money(1, bdt) }
        rejects { Money(Long.MAX_VALUE, bdt) - Money(-1, bdt) }
        assertEquals(Money(0, bdt), Money(Long.MIN_VALUE, bdt) - Money(Long.MIN_VALUE, bdt))
    }

    @Test fun currenciesAndPrecisionCannotBeMixed() {
        rejects { Money(100, bdt) + Money(100, CurrencyUnit("USD")) }
        rejects { Money(100, bdt) - Money(100, CurrencyUnit("BDT", 0)) }
        rejects { CurrencyUnit("bdt") }
        rejects { CurrencyUnit("BDT", 4) }
    }

    private fun rejects(block: () -> Unit) {
        try { block(); fail("Expected invalid-money rejection") } catch (_: IllegalArgumentException) { }
    }
}
