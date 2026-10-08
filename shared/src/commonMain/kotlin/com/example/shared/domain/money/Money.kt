package com.example.shared.domain.money

/** Explicit profile currency; no implicit conversion or platform-dependent rounding. */
data class CurrencyUnit(val code: String, val fractionDigits: Int = 2) {
    init {
        require(code.length == 3 && code.all { it in 'A'..'Z' }) { "Invalid currency code" }
        require(fractionDigits in 0..3) { "Unsupported currency precision" }
    }
}

/** Exact minor-unit money for the new domain boundary. Legacy Room amounts remain Double. */
data class Money(val minorUnits: Long, val currency: CurrencyUnit) {
    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Currency mismatch" }
        val result = minorUnits + other.minorUnits
        require(((minorUnits xor result) and (other.minorUnits xor result)) >= 0) { "Money overflow" }
        return copy(minorUnits = result)
    }

    operator fun minus(other: Money): Money {
        require(currency == other.currency) { "Currency mismatch" }
        val result = minorUnits - other.minorUnits
        require(((minorUnits xor other.minorUnits) and (minorUnits xor result)) >= 0) { "Money overflow" }
        return copy(minorUnits = result)
    }

    /** Stable machine-readable decimal, including Long.MIN_VALUE without abs overflow. */
    fun toDecimalString(): String {
        val negative = minorUnits < 0
        val digits = minorUnits.toString().removePrefix("-").padStart(currency.fractionDigits + 1, '0')
        val amount = if (currency.fractionDigits == 0) digits else {
            val split = digits.length - currency.fractionDigits
            digits.substring(0, split) + "." + digits.substring(split)
        }
        return (if (negative) "-" else "") + amount
    }

    companion object {
        /** ASCII/Bangla digits, optional minus and decimal point. Ambiguous grouping is rejected. */
        fun parseDecimal(input: String, currency: CurrencyUnit): Money? {
            // Bound work even for untrusted capture/model input; a Long needs at most 19 digits.
            if (input.length > 64) return null
            val text = input.trim().map { if (it in '০'..'৯') '0' + (it - '০') else it }.joinToString("")
            val negative = text.startsWith("-")
            val unsigned = if (negative) text.substring(1) else text
            val parts = unsigned.split('.')
            if (parts.size !in 1..2 || parts[0].isEmpty() || !parts[0].all { it in '0'..'9' }) return null
            val fraction = parts.getOrElse(1) { "" }
            if (parts.size == 2 && fraction.isEmpty()) return null
            if (fraction.length > currency.fractionDigits || !fraction.all { it in '0'..'9' }) return null
            val digits = (parts[0] + fraction.padEnd(currency.fractionDigits, '0')).trimStart('0').ifEmpty { "0" }
            val units = ((if (negative) "-" else "") + digits).toLongOrNull() ?: return null
            return Money(units, currency)
        }
    }
}
