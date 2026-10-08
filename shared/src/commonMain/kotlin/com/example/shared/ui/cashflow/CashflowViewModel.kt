package com.example.shared.ui.cashflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.database.AppDatabase
import com.example.shared.data.database.buildAppDatabase
import com.example.shared.data.database.getDatabaseBuilder
import com.example.shared.data.model.AccountEntity
import com.example.shared.data.model.IncomeStreamEntity
import com.example.shared.data.model.LoanEntity
import com.example.shared.data.model.SavingsGoalEntity
import com.example.shared.data.model.SpendingPlanEntity
import com.example.shared.data.model.TaskEntity
import com.example.shared.data.model.TransactionEntity
import com.example.shared.data.model.UserSettingsEntity
import com.example.shared.data.repository.CashflowRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class HomeUiState(
    val balances: List<AccountBalance> = emptyList(),
    val totalBalance: Double = 0.0,
    val plan: PlanProgress? = null,
    val pendingCount: Int = 0,
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val nextGoal: SavingsGoalEntity? = null,
    val nextLoan: LoanEntity? = null,
    val metrics: HomeMetrics = HomeMetrics()
)

private data class CashflowInputs(
    val accounts: List<AccountEntity>,
    val transactions: List<TransactionEntity>,
    val plans: List<SpendingPlanEntity>,
    val streams: List<IncomeStreamEntity>,
    val settings: UserSettingsEntity?
)

class CashflowViewModel : ViewModel() {
    private val database: AppDatabase = buildAppDatabase(getDatabaseBuilder())
    private val repository = CashflowRepository(
        accountDao = database.accountDao(),
        incomeStreamDao = database.incomeStreamDao(),
        transactionDao = database.transactionDao(),
        spendingPlanDao = database.spendingPlanDao(),
        savingsGoalDao = database.savingsGoalDao(),
        loanDao = database.loanDao(),
        taskDao = database.taskDao(),
        settingsDao = database.userSettingsDao()
    )

    val settings: StateFlow<UserSettingsEntity?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val accounts = repository.accounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val transactions = repository.transactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val plans = repository.plans.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val goals = repository.goals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val loans = repository.loans.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val tasks = repository.tasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val incomeStreams = repository.incomeStreams.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val cashflowInputs = combine(accounts, transactions, plans, incomeStreams, settings) { allAccounts, allTransactions, allPlans, allStreams, currentSettings ->
        CashflowInputs(allAccounts, allTransactions, allPlans, allStreams, currentSettings)
    }

    val home: StateFlow<HomeUiState> = combine(cashflowInputs, goals, loans) { inputs, allGoals, allLoans ->
        val balances = CashflowCalculations.balances(inputs.accounts, inputs.transactions)
        val totalBalance = balances.sumOf { it.balance }
        val activePlan = inputs.plans.firstOrNull { it.isActive }
        HomeUiState(
            balances = balances,
            totalBalance = totalBalance,
            plan = CashflowCalculations.activePlanProgress(activePlan, inputs.transactions),
            pendingCount = CashflowCalculations.pendingCount(inputs.transactions),
            recentTransactions = inputs.transactions.take(5),
            nextGoal = allGoals.minByOrNull { it.targetDateEpochMillis },
            nextLoan = allLoans.filterNot { it.isSettled }.minByOrNull { it.dueDateEpochMillis },
            metrics = CashflowCalculations.homeMetrics(
                transactions = inputs.transactions,
                incomeStreams = inputs.streams,
                totalBalance = totalBalance,
                salaryDay = inputs.settings?.salaryDay ?: 1
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun finishOnboarding(name: String, currencyCode: String, currencySymbol: String, accountName: String, openingBalance: Double) {
        viewModelScope.launch {
            repository.saveSettings(
                UserSettingsEntity(
                    userName = name.trim(),
                    currencyCode = currencyCode,
                    currencySymbol = currencySymbol,
                    localeTag = "en-US",
                    isDataLoaded = false,
                    onboardingComplete = true
                )
            )
            repository.addAccount(
                AccountEntity(
                    name = accountName.ifBlank { "Cash" },
                    type = "CASH",
                    openingBalance = openingBalance,
                    currencyCode = currencyCode,
                    isDefault = true,
                    createdAtEpochMillis = Clock.System.now().toEpochMilliseconds()
                )
            )
        }
    }

    fun addAccount(name: String, type: String, openingBalance: Double) = viewModelScope.launch {
        repository.addAccount(
            AccountEntity(
                name = name.trim(), type = type, openingBalance = openingBalance,
                currencyCode = settings.value?.currencyCode ?: "USD",
                createdAtEpochMillis = Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    fun addPlan(name: String, limit: Double, cycle: String) = viewModelScope.launch {
        repository.addPlan(SpendingPlanEntity(name = name.ifBlank { "Spending plan" }, limitAmount = limit, cycleType = cycle))
    }

    fun saveCapture(draft: CaptureDraft) = viewModelScope.launch {
        val amount = draft.amount.toDoubleOrNull() ?: return@launch
        if (amount <= 0.0) return@launch
        repository.addTransaction(
            TransactionEntity(
                type = if (draft.kind == CaptureKind.EXPENSE) "EXPENSE" else "INCOME",
                amount = amount,
                category = draft.category.ifBlank { "Everyday" },
                subCategory = "",
                description = draft.note.trim(),
                status = "PENDING_SOURCE",
                source = "MANUAL"
            )
        )
    }

    fun assignAccount(transaction: TransactionEntity, accountId: Int) = viewModelScope.launch {
        repository.updateTransaction(transaction.copy(accountId = accountId, status = "COMPLETED"))
    }

    fun deleteTransaction(id: Int) = viewModelScope.launch { repository.deleteTransaction(id) }

    fun transfer(fromAccountId: Int, toAccountId: Int, amount: Double, note: String) = viewModelScope.launch {
        if (fromAccountId == toAccountId || amount <= 0.0) return@launch
        repository.addTransaction(
            TransactionEntity(
                type = "TRANSFER", amount = amount, category = "Transfer", subCategory = "",
                description = note.ifBlank { "Account transfer" }, accountId = fromAccountId,
                destinationAccountId = toAccountId, status = "COMPLETED", source = "MANUAL"
            )
        )
    }

    fun addIncomeStream(name: String, amount: Double, frequency: String, accountId: Int?) = viewModelScope.launch {
        repository.addIncomeStream(IncomeStreamEntity(name = name, amount = amount, frequency = frequency, accountId = accountId))
    }

    fun addGoal(title: String, target: Double) = viewModelScope.launch {
        repository.addGoal(SavingsGoalEntity(title = title, targetAmount = target))
    }

    fun addTask(title: String, dueDate: String) = viewModelScope.launch {
        repository.addTask(TaskEntity(title = title, dueDate = dueDate))
    }

    fun addLoan(title: String, person: String, amount: Double, direction: String, template: String, interestModel: String, rate: Double) = viewModelScope.launch {
        repository.addLoan(
            LoanEntity(
                title = title, personName = person, amount = amount, direction = direction,
                loanType = "SHORT_TERM", template = template, interestModel = interestModel,
                annualInterestRate = rate
            )
        )
    }

    fun toggleDarkMode(enabled: Boolean) = viewModelScope.launch {
        settings.value?.let { repository.saveSettings(it.copy(isDarkMode = enabled)) }
    }
}
