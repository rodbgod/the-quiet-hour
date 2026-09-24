package br.com.rodsil.quiethour.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DeliveredPassage::class], version = 1)
abstract class QuietHourDatabase : RoomDatabase() {
  abstract fun deliveredPassageDao(): DeliveredPassageDao
}
