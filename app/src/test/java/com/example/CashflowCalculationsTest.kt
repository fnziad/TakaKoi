package com.example

import com.example.shared.data.model.AccountEntity
import com.example.shared.data.model.TransactionEntity
import com.example.shared.ui.cashflow.CashflowCalculations
import org.junit.Assert.assertEquals
import org.junit.Test

class CashflowCalculationsTest {
    private val cash = AccountEntity(id = 1, name = "Cash", openingBalance = 100.0)
    private val wallet = AccountEntity(id = 2, name = "Mobile wallet", openingBalance = 50.0)

    @Test
    fun `transfer moves money without changing total balance`() {
        val balances = CashflowCalculations.balances(
            listOf(cash, wallet),
            listOf(
                TransactionEntity(
                    type = "TRANSFER", amount = 40.0, category = "Transfer", subCategory = "", description = "",
                    accountId = 1, destinationAccountId = 2, status = "COMPLETED"
                )
            )
        )
        assertEquals(60.0, balances.first { it.account.id == 1 }.balance, 0.001)
        assertEquals(90.0, balances.first { it.account.id == 2 }.balance, 0.001)
        assertEquals(150.0, balances.sumOf { it.balance }, 0.001)
    }

    @Test
    fun `pending capture does not change a wallet balance`() {
        val balances = CashflowCalculations.balances(
            listOf(cash),
            listOf(TransactionEntity(type = "EXPENSE", amount = 20.0, category = "Food", subCategory = "", description = "", status = "PENDING_SOURCE"))
        )
        assertEquals(100.0, balances.single().balance, 0.001)
    }

    @Test
    fun `pending count includes unassigned captures`() {
        assertEquals(1, CashflowCalculations.pendingCount(listOf(TransactionEntity(type = "INCOME", amount = 10.0, category = "Income", subCategory = "", description = "", status = "PENDING_SOURCE"))))
    }
}
