package com.example.shared.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
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
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        AccountEntity::class,
        IncomeStreamEntity::class,
        TransactionEntity::class,
        SavingsGoalEntity::class,
        SpendingPlanEntity::class,
        LoanEntity::class,
        UserSettingsEntity::class,
        TaskEntity::class
    ],
    version = 6,
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun incomeStreamDao(): IncomeStreamDao
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun spendingPlanDao(): SpendingPlanDao
    abstract fun loanDao(): LoanDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun taskDao(): TaskDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>

/**
 * Platform-neutral builder helper. Each platform provides the builder
 * via an expect/actual pair in DatabaseFactory.kt.
 */
fun buildAppDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
    driver: SQLiteDriver = BundledSQLiteDriver()
): AppDatabase {
    return builder
        .setDriver(driver)
        .setQueryCoroutineContext(Dispatchers.Default)
        .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
        .build()
}
