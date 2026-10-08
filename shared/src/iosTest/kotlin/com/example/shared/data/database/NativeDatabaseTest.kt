package com.example.shared.data.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.example.shared.data.model.AccountEntity
import com.example.shared.data.model.TransactionEntity
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Native simulator execution, not UIKit, encrypted storage, or device-lock verification. */
@OptIn(ExperimentalForeignApi::class)
class NativeDatabaseTest {
    @Test fun generatedRoomReopensBundledDatabaseWithBanglaRecord() = fixture { path ->
        val first = buildAppDatabase(Room.databaseBuilder<AppDatabase>(name = path))
        try {
            val accountId = first.accountDao().insert(AccountEntity(name = "Synthetic cash", openingBalance = 100.25)).toInt()
            first.transactionDao().insertTransaction(TransactionEntity(type = "EXPENSE", amount = 20.25, category = "Transport", subCategory = "", description = "SYNTHETIC_রিকশা", accountId = accountId, status = "COMPLETED"))
        } finally { first.close() }
        val reopened = buildAppDatabase(Room.databaseBuilder<AppDatabase>(name = path))
        try {
            assertEquals(100.25, reopened.accountDao().getActiveAccounts().first().single().openingBalance)
            assertEquals("SYNTHETIC_রিকশা", reopened.transactionDao().getAllTransactions().first().single().description)
        } finally { reopened.close() }
    }

    @Test fun unsupportedVersionPreservesOriginalFileAndRecords() = fixture { path ->
        BundledSQLiteDriver().open(path).use { connection ->
            connection.execSQL("CREATE TABLE synthetic_sentinel (id INTEGER PRIMARY KEY, note TEXT NOT NULL)")
            connection.execSQL("INSERT INTO synthetic_sentinel VALUES (1, 'SYNTHETIC_ONLY')")
            connection.execSQL("PRAGMA user_version = 3")
        }
        val database = buildAppDatabase(Room.databaseBuilder<AppDatabase>(name = path))
        try {
            var failed = false
            try { database.transactionDao().getAllTransactions().first() } catch (_: Exception) { failed = true }
            assertTrue(failed, "Missing migration must fail closed")
        } finally { database.close() }
        BundledSQLiteDriver().open(path).use { connection ->
            connection.prepare("PRAGMA user_version").use { assertTrue(it.step()); assertEquals(3L, it.getLong(0)) }
            connection.prepare("SELECT note FROM synthetic_sentinel WHERE id = 1").use { assertTrue(it.step()); assertEquals("SYNTHETIC_ONLY", it.getText(0)) }
        }
    }

    private fun fixture(block: suspend (String) -> Unit) = runBlocking {
        val directory = NSTemporaryDirectory() + "takakoi-test-" + NSUUID().UUIDString
        val files = NSFileManager.defaultManager
        check(files.createDirectoryAtPath(directory, true, null, null))
        try { block("$directory/synthetic.sqlite") } finally {
            // The exact fresh test-owned directory only; never the app's Documents/database path.
            check(files.removeItemAtPath(directory, null))
        }
    }
}
