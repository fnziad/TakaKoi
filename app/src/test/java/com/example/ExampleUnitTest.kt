package com.example

import com.example.shared.ui.cashflow.CashflowCalculations
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class CashflowUnitTest {
  @Test
  fun empty_ledger_has_no_pending_captures() {
    assertEquals(0, CashflowCalculations.pendingCount(emptyList()))
  }
}
