package com.example.shared.ui.cashflow

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.shared.data.model.AccountEntity
import com.example.shared.data.model.LoanEntity
import com.example.shared.data.model.TransactionEntity
import com.example.shared.ui.theme.PremiumEmerald
import com.example.shared.ui.theme.PremiumEmeraldBg
import com.example.shared.ui.theme.PremiumRose
import com.example.shared.ui.theme.PremiumRoseBg
import com.example.shared.ui.theme.PremiumViolet
import com.example.shared.ui.theme.PremiumVioletSoft

private val ScreenShape = RoundedCornerShape(24.dp)
private val CardShape = RoundedCornerShape(20.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashflowApp(viewModel: CashflowViewModel) {
    val settings by viewModel.settings.collectAsState()
    if (settings?.onboardingComplete != true) {
        OnboardingScreen(onComplete = viewModel::finishOnboarding)
        return
    }

    var destination by remember { mutableStateOf(AppDestination.HOME) }
    var showCapture by remember { mutableStateOf(false) }
    var showAccount by remember { mutableStateOf(false) }
    val destinations = listOf(
        Triple(AppDestination.HOME, "Home", Icons.Default.AccountBalanceWallet),
        Triple(AppDestination.ACTIVITY, "Activity", Icons.AutoMirrored.Filled.ReceiptLong),
        Triple(AppDestination.PLAN, "Plan", Icons.AutoMirrored.Filled.Assignment)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                destinations.forEach { (item, label, icon) ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { destination = item },
                        icon = { Icon(icon, label) },
                        label = { Text(label) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCapture = true },
                containerColor = PremiumViolet,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) { Icon(Icons.Default.Add, "Quick capture") }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (destination) {
                AppDestination.HOME -> HomeScreen(viewModel, onAccount = { showAccount = true }, onReview = { destination = AppDestination.ACTIVITY })
                AppDestination.ACTIVITY -> ActivityScreen(viewModel)
                AppDestination.PLAN -> PlanScreen(viewModel)
            }
        }
    }
    if (showCapture) QuickCaptureSheet(viewModel, onDismiss = { showCapture = false })
    if (showAccount) AccountSheet(viewModel, onDismiss = { showAccount = false })
}

@Composable
private fun OnboardingScreen(
    onComplete: (String, String, String, String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("USD") }
    var accountName by remember { mutableStateOf("Cash") }
    var balance by remember { mutableStateOf("") }
    val symbols = mapOf("USD" to "$", "BDT" to "৳", "EUR" to "€", "INR" to "₹", "GBP" to "£")

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("TakaKoi", style = MaterialTheme.typography.labelLarge, color = PremiumViolet)
        Spacer(Modifier.height(12.dp))
        Text("Your money,\nmade clear.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Start with one wallet. You can add banks, cards and mobile wallets whenever you need them.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(name, { name = it }, label = { Text("What should we call you?") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Text("Base currency", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            symbols.keys.forEach { code ->
                FilterChip(selected = currency == code, onClick = { currency = code }, label = { Text(code) })
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(accountName, { accountName = it }, label = { Text("First wallet") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(balance, { balance = it.filter { char -> char.isDigit() || char == '.' } }, label = { Text("Current balance (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onComplete(name, currency, symbols[currency] ?: currency, accountName, balance.toDoubleOrNull() ?: 0.0) },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PremiumViolet)
        ) { Text("Begin with clarity") }
    }
}

@Composable
private fun HomeScreen(viewModel: CashflowViewModel, onAccount: () -> Unit, onReview: () -> Unit) {
    val home by viewModel.home.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val symbol = settings?.currencySymbol ?: "$"
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Good to see you", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(settings?.userName?.ifBlank { "Your money" } ?: "Your money", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onAccount, modifier = Modifier.clip(CircleShape).background(PremiumVioletSoft)) {
                    Icon(Icons.Default.Person, "Account", tint = PremiumViolet)
                }
            }
        }
        item {
            Card(shape = ScreenShape, colors = CardDefaults.cardColors(containerColor = PremiumViolet)) {
                Column(Modifier.padding(24.dp)) {
                    Text("Available across your wallets", color = Color.White.copy(alpha = .72f))
                    Spacer(Modifier.height(8.dp))
                    Text(money(home.totalBalance, symbol), style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(18.dp))
                    Text("${home.balances.size} active wallet${if (home.balances.size == 1) "" else "s"}", color = Color.White.copy(alpha = .75f))
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Payday", "${home.metrics.daysUntilPayday} days", Modifier.weight(1f))
                MetricCard("Spent today", money(home.metrics.spentToday, symbol), Modifier.weight(1f))
                MetricCard("Daily avg", money(home.metrics.dailyAverage, symbol), Modifier.weight(1f))
            }
        }
        home.plan?.let { plan -> item { PaceCard(plan, symbol) } }
        item {
            CashRunwayCard(home.metrics.cashRunwayDays, home.totalBalance, home.metrics.dailyAverage, symbol)
        }
        if (home.metrics.projectedMonthlyInflow > 0) item {
            SimpleRow("Recurring monthly inflow", money(home.metrics.projectedMonthlyInflow, symbol))
        }
        if (home.pendingCount > 0) item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onReview), shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MoreHoriz, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("${home.pendingCount} money moment${if (home.pendingCount == 1) "" else "s"} needs a wallet", fontWeight = FontWeight.Bold)
                        Text("Your pace is current; assign the source when you have a moment.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item { SectionLabel("Recent activity") }
        if (home.recentTransactions.isEmpty()) item { EmptyCard("Your first money moment starts here", "Tap + to quickly record income or spending.") }
        items(home.recentTransactions, key = { "recent-transaction-${it.id}" }) { transaction -> TransactionRow(transaction, symbol, null, onClick = {}) }
        if (home.metrics.categoryRanks.isNotEmpty()) item {
            CategoryRankingCard(home.metrics.categoryRanks, home.metrics.totalSpent, symbol)
        }
        home.nextGoal?.let { goal -> item { PriorityCard("Next goal", goal.title, "${money(goal.currentAmount, symbol)} of ${money(goal.targetAmount, symbol)}", Icons.Default.Savings) } }
        home.nextLoan?.let { loan -> item { PriorityCard("Upcoming debt", loan.title, "${money(loan.amount - loan.paidAmount, symbol)} remaining", Icons.AutoMirrored.Filled.Assignment) } }
    }
}

@Composable
private fun PaceCard(plan: PlanProgress, symbol: String) {
    Card(shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(plan.name.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text("${money(plan.safeToSpendToday, symbol)} safe today", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("${money(plan.remaining, symbol)} remaining • ${plan.daysRemaining} days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${money(plan.spent, symbol)} spent of ${money(plan.limit, symbol)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(Modifier.size(78.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 9.dp.toPx()
                    drawArc(PremiumVioletSoft, -90f, 360f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                    drawArc(PremiumViolet, -90f, (plan.spent / plan.limit).coerceIn(0.0, 1.0).toFloat() * 360f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                }
                Text("${((plan.spent / plan.limit) * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 14.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(5.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun CashRunwayCard(runwayDays: Int?, balance: Double, dailyAverage: Double, symbol: String) {
    Card(shape = CardShape, colors = CardDefaults.cardColors(containerColor = PremiumVioletSoft)) {
        Column(Modifier.padding(18.dp)) {
            Text("Cash runway", style = MaterialTheme.typography.labelMedium, color = PremiumViolet)
            Spacer(Modifier.height(5.dp))
            Text(
                runwayDays?.let { "Your ${money(balance, symbol)} can last about $it days" } ?: "Log spending to see how long your balance can last",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (dailyAverage > 0) Text("Based on your ${money(dailyAverage, symbol)} daily average.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CategoryRankingCard(ranks: List<CategoryRank>, totalSpent: Double, symbol: String) {
    Card(shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Where your money went", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("This month • ${money(totalSpent, symbol)} spent", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ranks.forEach { rank ->
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(rank.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(money(rank.amount, symbol), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Box(Modifier.fillMaxWidth().height(7.dp).clip(CircleShape).background(PremiumVioletSoft)) {
                        Box(Modifier.fillMaxWidth(rank.share).height(7.dp).clip(CircleShape).background(PremiumViolet))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityScreen(viewModel: CashflowViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val symbol = settings?.currencySymbol ?: "$"
    var reviewOnly by remember { mutableStateOf(false) }
    var assigning by remember { mutableStateOf<TransactionEntity?>(null) }
    val shown = if (reviewOnly) transactions.filter { it.status == "PENDING_SOURCE" } else transactions
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Activity", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Every money moment, in one calm place.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !reviewOnly, onClick = { reviewOnly = false }, label = { Text("All") })
                FilterChip(selected = reviewOnly, onClick = { reviewOnly = true }, label = { Text("Needs review") })
            }
        }
        if (shown.isEmpty()) item { EmptyCard(if (reviewOnly) "Nothing needs review" else "No activity yet", if (reviewOnly) "All captured entries have a wallet." else "Use + to record your first money moment.") }
        items(shown, key = { "activity-transaction-${it.id}" }) { transaction ->
            val account = accounts.firstOrNull { it.id == transaction.accountId }
            TransactionRow(transaction, symbol, account?.name, onClick = { if (transaction.status == "PENDING_SOURCE") assigning = transaction })
        }
    }
    assigning?.let { transaction -> AssignWalletSheet(transaction, accounts, viewModel, onDismiss = { assigning = null }) }
}

@Composable
private fun PlanScreen(viewModel: CashflowViewModel) {
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val plans by viewModel.plans.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val loans by viewModel.loans.collectAsState()
    val streams by viewModel.incomeStreams.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val symbol = settings?.currencySymbol ?: "$"
    var showWallet by remember { mutableStateOf(false) }
    var showPlan by remember { mutableStateOf(false) }
    var showTransfer by remember { mutableStateOf(false) }
    var showDebt by remember { mutableStateOf(false) }
    var showIncome by remember { mutableStateOf(false) }
    var showGoal by remember { mutableStateOf(false) }
    var showTask by remember { mutableStateOf(false) }
    val balances = CashflowCalculations.balances(accounts, transactions)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Plan", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Build a money system that fits your life.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { SectionAction("Wallets", "Add wallet", { showWallet = true }) }
        if (balances.isEmpty()) item { EmptyCard("No wallets yet", "Add cash, bank, card or mobile-wallet balances.") }
        items(balances, key = { "account-${it.account.id}" }) { balance -> AccountRow(balance, symbol) }
        item { TextButton(onClick = { showTransfer = true }, enabled = accounts.size > 1) { Text("Transfer money between wallets") } }
        item { SectionAction("Spending plans", "New plan", { showPlan = true }) }
        if (plans.isEmpty()) item { EmptyCard("Set a limit that fits your cycle", "Choose weekly, monthly, payday or a custom plan.") }
        items(plans, key = { "plan-${it.id}" }) { plan ->
            CashflowCalculations.activePlanProgress(plan, transactions)?.let { PaceCard(it, symbol) }
        }
        item { SectionAction("Income streams", "Add income", { showIncome = true }) }
        if (streams.isEmpty()) item { EmptyCard("Income can be flexible", "Add salary, allowance, tuition, freelancing or any custom stream.") }
        items(streams, key = { "income-stream-${it.id}" }) { stream -> SimpleRow(stream.name, "${stream.frequency.lowercase().replaceFirstChar { it.uppercase() }} • ${money(stream.amount, symbol)}") }
        item { SectionAction("Goals", "Add goal", { showGoal = true }) }
        if (goals.isEmpty()) item { EmptyCard("Make room for a goal", "Savings goals will live here.") }
        items(goals, key = { "goal-${it.id}" }) { goal -> SimpleRow(goal.title, "${money(goal.currentAmount, symbol)} of ${money(goal.targetAmount, symbol)}") }
        item { SectionAction("Debt and lending", "Add debt", { showDebt = true }) }
        if (loans.isEmpty()) item { EmptyCard("Keep promises visible", "Track what you owe and what people owe you.") }
        items(loans, key = { "loan-${it.id}" }) { loan -> DebtRow(loan, symbol) }
        item { SectionAction("Financial tasks", "Add task", { showTask = true }) }
        if (tasks.isEmpty()) item { EmptyCard("Keep one promise visible", "Add a bill, reminder, or a money task.") }
        items(tasks, key = { "task-${it.id}" }) { task -> SimpleRow(task.title, task.dueDate.ifBlank { "No due date" }) }
    }
    if (showWallet) AddWalletSheet(viewModel, { showWallet = false })
    if (showPlan) AddPlanSheet(viewModel, { showPlan = false })
    if (showTransfer) TransferSheet(accounts, viewModel, { showTransfer = false })
    if (showDebt) AddDebtSheet(viewModel, { showDebt = false })
    if (showIncome) AddIncomeSheet(accounts, viewModel, { showIncome = false })
    if (showGoal) AddGoalSheet(viewModel, { showGoal = false })
    if (showTask) AddTaskSheet(viewModel, { showTask = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickCaptureSheet(viewModel: CashflowViewModel, onDismiss: () -> Unit) {
    var draft by remember { mutableStateOf(CaptureDraft()) }
    ModalBottomSheet(onDismissRequest = onDismiss, shape = ScreenShape) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Quick capture", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = draft.kind == CaptureKind.EXPENSE, onClick = { draft = draft.copy(kind = CaptureKind.EXPENSE) }, label = { Text("Spent") })
                FilterChip(selected = draft.kind == CaptureKind.INCOME, onClick = { draft = draft.copy(kind = CaptureKind.INCOME) }, label = { Text("Received") })
            }
            OutlinedTextField(draft.amount, { draft = draft.copy(amount = it.filter { char -> char.isDigit() || char == '.' }) }, label = { Text("Amount") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(draft.category, { draft = draft.copy(category = it) }, label = { Text("Category") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(draft.note, { draft = draft.copy(note = it) }, label = { Text("What was it? (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Text("You can choose the wallet later. This will still update your spending pace now.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = { viewModel.saveCapture(draft); onDismiss() }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = PremiumViolet), enabled = (draft.amount.toDoubleOrNull() ?: 0.0) > 0) { Text("Save money moment") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignWalletSheet(transaction: TransactionEntity, accounts: List<AccountEntity>, viewModel: CashflowViewModel, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, shape = ScreenShape) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Where did this come from?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(transaction.description.ifBlank { transaction.category }, color = MaterialTheme.colorScheme.onSurfaceVariant)
            accounts.forEach { account ->
                Card(modifier = Modifier.fillMaxWidth().clickable { viewModel.assignAccount(transaction, account.id); onDismiss() }, shape = CardShape) {
                    Text(account.name, Modifier.padding(18.dp), fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWalletSheet(viewModel: CashflowViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }; var type by remember { mutableStateOf("CASH") }; var balance by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, shape = ScreenShape) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Add a wallet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("CASH", "BANK", "MOBILE_WALLET", "CARD").forEach { option -> FilterChip(type == option, { type = option }, { Text(option.lowercase().replace('_', ' ')) }) } }
            OutlinedTextField(balance, { balance = it.filter { char -> char.isDigit() || char == '.' } }, label = { Text("Opening balance") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { if (name.isNotBlank()) { viewModel.addAccount(name, type, balance.toDoubleOrNull() ?: 0.0); onDismiss() } }, modifier = Modifier.fillMaxWidth()) { Text("Add wallet") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPlanSheet(viewModel: CashflowViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("My spending plan") }; var amount by remember { mutableStateOf("") }; var cycle by remember { mutableStateOf("MONTHLY") }
    ModalBottomSheet(onDismissRequest = onDismiss, shape = ScreenShape) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("New spending plan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(name, { name = it }, label = { Text("Plan name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(amount, { amount = it.filter { char -> char.isDigit() || char == '.' } }, label = { Text("Maximum spending") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("WEEKLY", "MONTHLY", "PAYDAY", "CUSTOM").forEach { option -> FilterChip(cycle == option, { cycle = option }, { Text(option.lowercase().replaceFirstChar { it.uppercase() }) }) } }
            Button(onClick = { if ((amount.toDoubleOrNull() ?: 0.0) > 0) { viewModel.addPlan(name, amount.toDouble(), cycle); onDismiss() } }, modifier = Modifier.fillMaxWidth()) { Text("Create plan") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransferSheet(accounts: List<AccountEntity>, viewModel: CashflowViewModel, onDismiss: () -> Unit) {
    var from by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 0) }; var to by remember { mutableStateOf(accounts.drop(1).firstOrNull()?.id ?: 0) }; var amount by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, shape = ScreenShape) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Move money", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Choose the source and destination wallet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("From", style = MaterialTheme.typography.labelLarge)
            accounts.forEach { account -> FilterChip(from == account.id, { from = account.id }, { Text(account.name) }) }
            Text("To", style = MaterialTheme.typography.labelLarge)
            accounts.forEach { account -> FilterChip(to == account.id, { to = account.id }, { Text(account.name) }) }
            OutlinedTextField(amount, { amount = it.filter { char -> char.isDigit() || char == '.' } }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { viewModel.transfer(from, to, amount.toDoubleOrNull() ?: 0.0, ""); onDismiss() }, enabled = from != to && (amount.toDoubleOrNull() ?: 0.0) > 0, modifier = Modifier.fillMaxWidth()) { Text("Move money") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDebtSheet(viewModel: CashflowViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }; var person by remember { mutableStateOf("") }; var amount by remember { mutableStateOf("") }; var direction by remember { mutableStateOf("I_OWE") }; var template by remember { mutableStateOf("FRIEND_FAMILY") }; var rate by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, shape = ScreenShape) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Add debt or lending", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(title, { title = it }, label = { Text("What is this for?") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(person, { person = it }, label = { Text("Person or provider") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(amount, { amount = it.filter { char -> char.isDigit() || char == '.' } }, label = { Text("Principal") }, modifier = Modifier.fillMaxWidth())
            Row { FilterChip(direction == "I_OWE", { direction = "I_OWE" }, { Text("I owe") }); Spacer(Modifier.width(8.dp)); FilterChip(direction == "OWED_TO_ME", { direction = "OWED_TO_ME" }, { Text("Owed to me") }) }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("FRIEND_FAMILY", "SIMPLE_APR", "COMPOUND_APR", "MFS_FEE").forEach { option -> FilterChip(template == option, { template = option }, { Text(option.substringBefore('_').lowercase()) }) } }
            if (template != "FRIEND_FAMILY") OutlinedTextField(rate, { rate = it.filter { char -> char.isDigit() || char == '.' } }, label = { Text("Annual rate %") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { viewModel.addLoan(title, person, amount.toDoubleOrNull() ?: 0.0, direction, template, if (template == "COMPOUND_APR") "COMPOUND" else if (template == "SIMPLE_APR") "SIMPLE" else "NONE", rate.toDoubleOrNull() ?: 0.0); onDismiss() }, enabled = title.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0, modifier = Modifier.fillMaxWidth()) { Text("Save debt") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddIncomeSheet(accounts: List<AccountEntity>, viewModel: CashflowViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }; var amount by remember { mutableStateOf("") }; var frequency by remember { mutableStateOf("ONE_TIME") }; var accountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }
    ModalBottomSheet(onDismissRequest = onDismiss, shape = ScreenShape) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Add income stream", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(amount, { amount = it.filter { char -> char.isDigit() || char == '.' } }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("ONE_TIME", "DAILY", "WEEKLY", "MONTHLY", "CUSTOM").forEach { option -> FilterChip(frequency == option, { frequency = option }, { Text(option.lowercase().replace('_', ' ')) }) } }
            if (accounts.isNotEmpty()) { Text("Default destination", style = MaterialTheme.typography.labelLarge); accounts.forEach { account -> FilterChip(accountId == account.id, { accountId = account.id }, { Text(account.name) }) } }
            Button(onClick = { if (name.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0) { viewModel.addIncomeStream(name, amount.toDouble(), frequency, accountId); onDismiss() } }, modifier = Modifier.fillMaxWidth()) { Text("Save income stream") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalSheet(viewModel: CashflowViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }; var target by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, shape = ScreenShape) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Add a savings goal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(title, { title = it }, label = { Text("Goal") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(target, { target = it.filter { char -> char.isDigit() || char == '.' } }, label = { Text("Target amount") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { if (title.isNotBlank() && (target.toDoubleOrNull() ?: 0.0) > 0) { viewModel.addGoal(title, target.toDouble()); onDismiss() } }, modifier = Modifier.fillMaxWidth()) { Text("Save goal") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskSheet(viewModel: CashflowViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }; var due by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, shape = ScreenShape) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Add a money task", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(title, { title = it }, label = { Text("Task") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(due, { due = it }, label = { Text("When is it due? (optional)") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { if (title.isNotBlank()) { viewModel.addTask(title, due); onDismiss() } }, modifier = Modifier.fillMaxWidth()) { Text("Save task") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountSheet(viewModel: CashflowViewModel, onDismiss: () -> Unit) {
    val settings by viewModel.settings.collectAsState()
    ModalBottomSheet(onDismissRequest = onDismiss, shape = ScreenShape) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(settings?.userName?.ifBlank { "Account" } ?: "Account", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("Dark appearance", fontWeight = FontWeight.Medium); Text("Designed for calm, low-light use", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Switch(checked = settings?.isDarkMode == true, onCheckedChange = viewModel::toggleDarkMode)
            }
            SimpleRow("Privacy", "Your financial data stays on this device")
            SimpleRow("Future backup", "Encrypted user-owned backup will be opt-in")
        }
    }
}

@Composable
private fun TransactionRow(transaction: TransactionEntity, symbol: String, accountName: String?, onClick: () -> Unit) {
    val isExpense = transaction.type == "EXPENSE"
    val tint = if (isExpense) PremiumRose else PremiumEmerald
    val tintBg = if (isExpense) PremiumRoseBg else PremiumEmeraldBg
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = CardShape, elevation = CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(tintBg), contentAlignment = Alignment.Center) {
                Icon(if (isExpense) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward, null, tint = tint)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(transaction.description.ifBlank { transaction.category }, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (transaction.status == "PENDING_SOURCE") "Needs wallet review" else accountName ?: transaction.category, style = MaterialTheme.typography.bodySmall, color = if (transaction.status == "PENDING_SOURCE") PremiumRose else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("${if (isExpense) "−" else "+"}${money(transaction.amount, symbol)}", color = tint, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable private fun AccountRow(balance: AccountBalance, symbol: String) = Card(shape = CardShape) { Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text(balance.account.name, fontWeight = FontWeight.SemiBold); Text(balance.account.type.lowercase().replace('_', ' '), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text(money(balance.balance, symbol), fontWeight = FontWeight.Bold) } }
@Composable private fun DebtRow(loan: LoanEntity, symbol: String) { val rate = if (loan.annualInterestRate > 0) " • ${loan.annualInterestRate}%" else ""; SimpleRow(loan.title, "${if (loan.direction == "I_OWE") "You owe" else "Owed to you"} ${money(loan.amount - loan.paidAmount + loan.fees, symbol)}$rate") }
@Composable private fun PriorityCard(label: String, title: String, detail: String, icon: androidx.compose.ui.graphics.vector.ImageVector) = Card(shape = CardShape, colors = CardDefaults.cardColors(containerColor = PremiumVioletSoft)) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = PremiumViolet); Spacer(Modifier.width(12.dp)); Column { Text(label, style = MaterialTheme.typography.labelMedium, color = PremiumViolet); Text(title, fontWeight = FontWeight.Bold); Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
@Composable private fun EmptyCard(title: String, body: String) = Card(shape = CardShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f))) { Column(Modifier.padding(20.dp)) { Text(title, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(4.dp)); Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
@Composable private fun SimpleRow(title: String, detail: String) = Card(shape = CardShape) { Column(Modifier.padding(16.dp)) { Text(title, fontWeight = FontWeight.SemiBold); Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
@Composable private fun SectionLabel(label: String) = Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
@Composable private fun SectionAction(label: String, action: String, onAction: () -> Unit) = Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); TextButton(onClick = onAction) { Text(action) } }
private fun money(amount: Double, symbol: String): String = "$symbol${amount.toInt()}"
