package com.example.shared.data.repository

import com.example.shared.data.dao.AccountDao
import com.example.shared.data.dao.IncomeStreamDao
import com.example.shared.data.dao.LoanDao
import com.example.shared.data.dao.SavingsGoalDao
import com.example.shared.data.dao.SpendingPlanDao
import com.example.shared.data.dao.TaskDao
import com.example.shared.data.dao.TransactionDao
import com.example.shared.data.dao.UserSettingsDao
import com.example.shared.data.model.AccountEntity
import com.example.shared.data.model.IncomeStreamEntity
import com.example.shared.data.model.LoanEntity
import com.example.shared.data.model.SavingsGoalEntity
import com.example.shared.data.model.SpendingPlanEntity
import com.example.shared.data.model.TaskEntity
import com.example.shared.data.model.TransactionEntity
import com.example.shared.data.model.UserSettingsEntity

class CashflowRepository(
    private val accountDao: AccountDao,
    private val incomeStreamDao: IncomeStreamDao,
    private val transactionDao: TransactionDao,
    private val spendingPlanDao: SpendingPlanDao,
    private val savingsGoalDao: SavingsGoalDao,
    private val loanDao: LoanDao,
    private val taskDao: TaskDao,
    private val settingsDao: UserSettingsDao
) {
    val accounts = accountDao.getActiveAccounts()
    val incomeStreams = incomeStreamDao.getActive()
    val transactions = transactionDao.getAllTransactions()
    val plans = spendingPlanDao.getAll()
    val goals = savingsGoalDao.getAllSavingsGoals()
    val loans = loanDao.getAllLoans()
    val tasks = taskDao.getAllTasks()
    val settings = settingsDao.getUserSettings()

    suspend fun addAccount(account: AccountEntity) = accountDao.insert(account)
    suspend fun updateAccount(account: AccountEntity) = accountDao.update(account)
    suspend fun addIncomeStream(stream: IncomeStreamEntity) = incomeStreamDao.insert(stream)
    suspend fun updateIncomeStream(stream: IncomeStreamEntity) = incomeStreamDao.update(stream)
    suspend fun addTransaction(transaction: TransactionEntity) = transactionDao.insertTransaction(transaction)
    suspend fun updateTransaction(transaction: TransactionEntity) = transactionDao.updateTransaction(transaction)
    suspend fun deleteTransaction(id: Int) = transactionDao.deleteTransactionById(id)
    suspend fun addPlan(plan: SpendingPlanEntity) = spendingPlanDao.insert(plan)
    suspend fun updatePlan(plan: SpendingPlanEntity) = spendingPlanDao.update(plan)
    suspend fun addGoal(goal: SavingsGoalEntity) = savingsGoalDao.insertSavingsGoal(goal)
    suspend fun updateGoal(goal: SavingsGoalEntity) = savingsGoalDao.updateSavingsGoal(goal)
    suspend fun addLoan(loan: LoanEntity) = loanDao.insertLoan(loan)
    suspend fun updateLoan(loan: LoanEntity) = loanDao.updateLoan(loan)
    suspend fun addTask(task: TaskEntity) = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun saveSettings(settings: UserSettingsEntity) = settingsDao.saveUserSettings(settings)

    suspend fun clearDevelopmentData() {
        transactionDao.deleteAllTransactions()
        accountDao.deleteAll()
        incomeStreamDao.deleteAll()
        spendingPlanDao.deleteAll()
        savingsGoalDao.deleteAll()
        loanDao.deleteAll()
        taskDao.deleteAllTasks()
    }
}
