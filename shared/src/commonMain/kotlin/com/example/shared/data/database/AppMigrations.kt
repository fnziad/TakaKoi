package com.example.shared.data.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/** Preserve legacy records. Wallet sources remain unknown until explicitly reviewed. */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `openingBalance` REAL NOT NULL, `currencyCode` TEXT NOT NULL, `isArchived` INTEGER NOT NULL, `isDefault` INTEGER NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `income_streams` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `amount` REAL NOT NULL, `frequency` TEXT NOT NULL, `nextDueEpochMillis` INTEGER NOT NULL, `accountId` INTEGER, `isActive` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `spending_plans` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `limitAmount` REAL NOT NULL, `cycleType` TEXT NOT NULL, `anchorDayOfMonth` INTEGER NOT NULL, `customStartEpochMillis` INTEGER NOT NULL, `customEndEpochMillis` INTEGER NOT NULL, `isActive` INTEGER NOT NULL)")
        connection.execSQL("ALTER TABLE `transactions` ADD COLUMN `accountId` INTEGER")
        connection.execSQL("ALTER TABLE `transactions` ADD COLUMN `destinationAccountId` INTEGER")
        connection.execSQL("ALTER TABLE `transactions` ADD COLUMN `status` TEXT NOT NULL DEFAULT 'PENDING_SOURCE'")
        connection.execSQL("ALTER TABLE `transactions` ADD COLUMN `source` TEXT NOT NULL DEFAULT 'MANUAL'")
        connection.execSQL("ALTER TABLE `loans` ADD COLUMN `template` TEXT NOT NULL DEFAULT 'FRIEND_FAMILY'")
        connection.execSQL("ALTER TABLE `loans` ADD COLUMN `interestModel` TEXT NOT NULL DEFAULT 'NONE'")
        connection.execSQL("ALTER TABLE `loans` ADD COLUMN `annualInterestRate` REAL NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE `loans` ADD COLUMN `compoundingFrequency` TEXT NOT NULL DEFAULT 'MONTHLY'")
        connection.execSQL("ALTER TABLE `loans` ADD COLUMN `fees` REAL NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE `user_settings` ADD COLUMN `currencyCode` TEXT NOT NULL DEFAULT 'USD'")
        connection.execSQL("ALTER TABLE `user_settings` ADD COLUMN `localeTag` TEXT NOT NULL DEFAULT 'en-US'")
        connection.execSQL("ALTER TABLE `user_settings` ADD COLUMN `onboardingComplete` INTEGER NOT NULL DEFAULT 0")
        // This is a label mapping only, never an exchange-rate conversion.
        connection.execSQL("UPDATE `user_settings` SET `currencyCode` = CASE `currencySymbol` WHEN '৳' THEN 'BDT' WHEN '₹' THEN 'INR' WHEN '€' THEN 'EUR' WHEN '£' THEN 'GBP' ELSE 'USD' END")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `income_streams` ADD COLUMN `lastConfirmedEpochMillis` INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE `user_settings` ADD COLUMN `savingsRatePercent` REAL NOT NULL DEFAULT 0")
    }
}
