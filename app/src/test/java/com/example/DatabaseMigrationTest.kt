package com.example

import android.content.Context
import androidx.room.Room
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.core.app.ApplicationProvider
import com.example.shared.data.database.AppDatabase
import com.example.shared.data.database.buildAppDatabase
import com.example.shared.data.model.AccountEntity
import com.example.shared.data.model.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode
import java.io.File
import java.nio.file.Files

/** Robolectric native SQLite + generated Room DAOs; only disposable synthetic fixtures. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24, 36])
@SQLiteMode(SQLiteMode.Mode.NATIVE)
class DatabaseMigrationTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun freshDatabaseReopensWithoutLosingRecords() = fixture { file ->
        open(file).use { db ->
            val accountId = db.accountDao().insert(AccountEntity(name = "Synthetic cash", openingBalance = 100.25)).toInt()
            db.transactionDao().insertTransaction(TransactionEntity(type = "EXPENSE", amount = 20.25, category = "Transport", subCategory = "", description = "SYNTHETIC_রিকশা", accountId = accountId, status = "COMPLETED"))
        }
        open(file).use { db ->
            assertEquals(100.25, db.accountDao().getActiveAccounts().first().single().openingBalance, 0.0)
            assertEquals("SYNTHETIC_রিকশা", db.transactionDao().getAllTransactions().first().single().description)
        }
    }

    @Test fun versionFivePreservesAllFinancialTablesAndAddsDefaults() = fixture { file ->
        legacy(file, 5)
        open(file).use { db ->
            assertEquals(100.25, db.accountDao().getActiveAccounts().first().single().openingBalance, 0.0)
            assertEquals(0L, db.incomeStreamDao().getActive().first().single().lastConfirmedEpochMillis)
            assertEquals(0.0, db.userSettingsDao().getUserSettings().first()!!.savingsRatePercent, 0.0)
            assertEquals(20.25, db.transactionDao().getAllTransactions().first().single().amount, 0.0)
            assertEquals("SYNTHETIC_রিকশা", db.transactionDao().getAllTransactions().first().single().description)
            assertEquals("Synthetic goal", db.savingsGoalDao().getAllSavingsGoals().first().single().title)
            assertEquals("Synthetic loan", db.loanDao().getAllLoans().first().single().title)
            assertEquals("Synthetic task", db.taskDao().getAllTasks().first().single().title)
            assertEquals("Synthetic plan", db.spendingPlanDao().getAll().first().single().name)
        }
        assertEquals(6L, scalar(file, "PRAGMA user_version"))
        open(file).use { db -> assertEquals(1, db.transactionDao().getAllTransactions().first().size) }
    }

    @Test fun versionFourPreservesRecordsWithoutInventingWalletBalances() = fixture { file ->
        legacy(file, 4)
        open(file).use { db ->
            val entry = db.transactionDao().getAllTransactions().first().single()
            assertEquals(20.25, entry.amount, 0.0)
            assertEquals("SYNTHETIC_রিকশা", entry.description)
            assertEquals("PENDING_SOURCE", entry.status)
            assertNull(entry.accountId)
            assertTrue(db.accountDao().getActiveAccounts().first().isEmpty())
            assertEquals("BDT", db.userSettingsDao().getUserSettings().first()!!.currencyCode)
            assertEquals(100.25, db.userSettingsDao().getUserSettings().first()!!.initialCash, 0.0)
            assertFalse(db.userSettingsDao().getUserSettings().first()!!.onboardingComplete)
            assertEquals("NONE", db.loanDao().getAllLoans().first().single().interestModel)
            assertEquals("Synthetic goal", db.savingsGoalDao().getAllSavingsGoals().first().single().title)
            assertEquals("Synthetic task", db.taskDao().getAllTasks().first().single().title)
        }
        assertEquals(6L, scalar(file, "PRAGMA user_version"))
    }

    @Test fun unknownUpgradeFailsWithoutDeletingOriginal() = fixture { file ->
        legacy(file, 5)
        sql(file) { it.execSQL("PRAGMA user_version = 3") }
        expectOpenFailure(file, "migration from 3 to 6")
        assertEquals(3L, scalar(file, "PRAGMA user_version"))
        assertEquals(1L, scalar(file, "SELECT count(*) FROM transactions"))
        assertEquals(1L, scalar(file, "SELECT count(*) FROM accounts"))
    }

    @Test fun downgradeFailsWithoutDeletingOriginal() = fixture { file ->
        legacy(file, 5)
        sql(file) { it.execSQL("PRAGMA user_version = 7") }
        expectOpenFailure(file, "migration from 7 to 6")
        assertEquals(7L, scalar(file, "PRAGMA user_version"))
        assertEquals(1L, scalar(file, "SELECT count(*) FROM transactions"))
    }

    @Test fun failedMigrationRollsBackEarlierAlterWithoutDeletingRecords() = fixture { file ->
        legacy(file, 5)
        // The first ALTER succeeds; the second must fail. Room must roll back the whole upgrade.
        sql(file) { it.execSQL("ALTER TABLE user_settings ADD COLUMN savingsRatePercent REAL NOT NULL DEFAULT 0") }
        expectOpenFailure(file, "duplicate column name: savingsRatePercent")
        assertEquals(5L, scalar(file, "PRAGMA user_version"))
        assertEquals(1L, scalar(file, "SELECT count(*) FROM transactions"))
        sql(file) { connection ->
            connection.prepare("PRAGMA table_info(income_streams)").use { statement ->
                val columns = mutableListOf<String>()
                while (statement.step()) columns.add(statement.getText(1))
                assertFalse(columns.contains("lastConfirmedEpochMillis"))
            }
        }
    }

    private fun expectOpenFailure(file: File, expectedReason: String) {
        val db = open(file)
        try {
            var failed = false
            try { runBlocking { db.transactionDao().getAllTransactions().first() } } catch (error: Exception) {
                assertTrue(error.toString(), error.message.orEmpty().contains(expectedReason, ignoreCase = true))
                failed = true
            }
            assertTrue("Unsupported or invalid schema must fail closed", failed)
        } finally {
            // Room 2.7's Android driver pool may retry its lazy open during close after an open failure.
            try { db.close() } catch (error: Exception) {
                assertTrue(error.toString(), error.message.orEmpty().contains(expectedReason, ignoreCase = true))
            }
        }
    }

    private fun open(file: File): AppDatabase = buildAppDatabase(
        Room.databaseBuilder<AppDatabase>(context, file.absolutePath), AndroidSQLiteDriver()
    )

    private inline fun <T> AppDatabase.use(block: (AppDatabase) -> T): T = try { block(this) } finally { close() }

    private fun scalar(file: File, query: String): Long {
        var result = 0L
        sql(file) { connection -> connection.prepare(query).use { assertTrue(it.step()); result = it.getLong(0) } }
        return result
    }

    private fun sql(file: File, block: (SQLiteConnection) -> Unit) = AndroidSQLiteDriver().open(file.absolutePath).use(block)

    private fun fixture(block: suspend (File) -> Unit) = runBlocking {
        val directory = Files.createTempDirectory("takakoi-migration-test-").toFile()
        try { block(File(directory, "synthetic.sqlite")) } finally {
            // Only the fresh test-owned directory; never a product database or shared parent.
            check(directory.deleteRecursively())
        }
    }

    private fun legacy(file: File, version: Int) = sql(file) { connection ->
        // Independent legacy DDL from the v4 main / v5 cashflow entity definitions, not current Room output.
        connection.execSQL("CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, type TEXT NOT NULL, amount REAL NOT NULL, category TEXT NOT NULL, subCategory TEXT NOT NULL, description TEXT NOT NULL, dateEpochMillis INTEGER NOT NULL, dayName TEXT NOT NULL, isRecurring INTEGER NOT NULL, recurringFrequency TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE savings_goals (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, targetAmount REAL NOT NULL, currentAmount REAL NOT NULL, targetDateEpochMillis INTEGER NOT NULL, category TEXT NOT NULL, note TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE loans (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, personName TEXT NOT NULL, amount REAL NOT NULL, paidAmount REAL NOT NULL, loanType TEXT NOT NULL, direction TEXT NOT NULL, dueDateEpochMillis INTEGER NOT NULL, isSettled INTEGER NOT NULL, note TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE user_settings (id INTEGER NOT NULL PRIMARY KEY, profileType TEXT NOT NULL, userName TEXT NOT NULL, initialCash REAL NOT NULL, salaryDay INTEGER NOT NULL, currencySymbol TEXT NOT NULL, targetSavings REAL NOT NULL, targetBudget REAL NOT NULL, incomeFrequency TEXT NOT NULL, colorTheme TEXT NOT NULL, isDarkMode INTEGER NOT NULL, isDataLoaded INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE financial_tasks (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, category TEXT NOT NULL, dueDate TEXT NOT NULL, priority TEXT NOT NULL, isCompleted INTEGER NOT NULL, createdAtEpochMillis INTEGER NOT NULL)")
        connection.execSQL("INSERT INTO transactions VALUES (1, 'EXPENSE', 20.25, 'Transport', '', 'SYNTHETIC_রিকশা', 1000, '', 0, 'One-time')")
        connection.execSQL("INSERT INTO savings_goals VALUES (1, 'Synthetic goal', 200, 25, 2000, 'Other', '')")
        connection.execSQL("INSERT INTO loans VALUES (1, 'Synthetic loan', 'Synthetic person', 50, 10, 'SHORT_TERM', 'I_OWE', 2000, 0, '')")
        connection.execSQL("INSERT INTO user_settings VALUES (1, 'CUSTOM', 'Synthetic profile', 100.25, 25, '৳', 10, 100, 'Monthly', 'INDIGO', 0, 0)")
        connection.execSQL("INSERT INTO financial_tasks VALUES (1, 'Synthetic task', 'General', '', 'Medium', 0, 1000)")
        if (version == 5) {
            // Spell out the historical v5 transition independently of the production migration under test.
            listOf(
                "CREATE TABLE accounts (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, type TEXT NOT NULL, openingBalance REAL NOT NULL, currencyCode TEXT NOT NULL, isArchived INTEGER NOT NULL, isDefault INTEGER NOT NULL, createdAtEpochMillis INTEGER NOT NULL)",
                "CREATE TABLE income_streams (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, amount REAL NOT NULL, frequency TEXT NOT NULL, nextDueEpochMillis INTEGER NOT NULL, accountId INTEGER, isActive INTEGER NOT NULL)",
                "CREATE TABLE spending_plans (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, limitAmount REAL NOT NULL, cycleType TEXT NOT NULL, anchorDayOfMonth INTEGER NOT NULL, customStartEpochMillis INTEGER NOT NULL, customEndEpochMillis INTEGER NOT NULL, isActive INTEGER NOT NULL)",
                "ALTER TABLE transactions ADD COLUMN accountId INTEGER",
                "ALTER TABLE transactions ADD COLUMN destinationAccountId INTEGER",
                "ALTER TABLE transactions ADD COLUMN status TEXT NOT NULL DEFAULT 'PENDING_SOURCE'",
                "ALTER TABLE transactions ADD COLUMN source TEXT NOT NULL DEFAULT 'MANUAL'",
                "ALTER TABLE loans ADD COLUMN template TEXT NOT NULL DEFAULT 'FRIEND_FAMILY'",
                "ALTER TABLE loans ADD COLUMN interestModel TEXT NOT NULL DEFAULT 'NONE'",
                "ALTER TABLE loans ADD COLUMN annualInterestRate REAL NOT NULL DEFAULT 0",
                "ALTER TABLE loans ADD COLUMN compoundingFrequency TEXT NOT NULL DEFAULT 'MONTHLY'",
                "ALTER TABLE loans ADD COLUMN fees REAL NOT NULL DEFAULT 0",
                "ALTER TABLE user_settings ADD COLUMN currencyCode TEXT NOT NULL DEFAULT 'BDT'",
                "ALTER TABLE user_settings ADD COLUMN localeTag TEXT NOT NULL DEFAULT 'en-BD'",
                "ALTER TABLE user_settings ADD COLUMN onboardingComplete INTEGER NOT NULL DEFAULT 1"
            ).forEach { connection.execSQL(it) }
            connection.execSQL("INSERT INTO accounts VALUES (1, 'Synthetic cash', 'CASH', 100.25, 'BDT', 0, 1, 1000)")
            connection.execSQL("INSERT INTO income_streams VALUES (1, 'Synthetic allowance', 500, 'DAILY', 2000, 1, 1)")
            connection.execSQL("INSERT INTO spending_plans VALUES (1, 'Synthetic plan', 100, 'MONTHLY', 1, 0, 0, 1)")
        }
        connection.execSQL("PRAGMA user_version = $version")
    }
}
