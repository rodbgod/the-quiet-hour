package br.com.rodsil.quiethour.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
  entities = [DeliveredPassage::class, CompletedDay::class, Reflection::class],
  version = 2,
  autoMigrations = [AutoMigration(from = 1, to = 2)],
)
abstract class QuietHourDatabase : RoomDatabase() {
  abstract fun deliveredPassageDao(): DeliveredPassageDao

  abstract fun ritualRecordDao(): RitualRecordDao
}
