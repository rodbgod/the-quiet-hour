package br.com.rodsil.quiethour.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
  entities = [DeliveredPassage::class, CompletedDay::class, Reflection::class, Favorite::class],
  version = 3,
  autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3)],
)
abstract class QuietHourDatabase : RoomDatabase() {
  abstract fun deliveredPassageDao(): DeliveredPassageDao

  abstract fun ritualRecordDao(): RitualRecordDao
}
