package br.com.rodsil.quiethour.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "delivered_passages")
data class DeliveredPassage(@PrimaryKey val epochDay: Long, val passageId: String)

@Dao
interface DeliveredPassageDao {
  @Query("SELECT passageId FROM delivered_passages WHERE epochDay = :epochDay")
  suspend fun passageIdOn(epochDay: Long): String?

  @Query("SELECT * FROM delivered_passages WHERE epochDay >= :fromEpochDay AND epochDay < :toEpochDay")
  suspend fun deliveredBetween(fromEpochDay: Long, toEpochDay: Long): List<DeliveredPassage>

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(delivery: DeliveredPassage)
}
