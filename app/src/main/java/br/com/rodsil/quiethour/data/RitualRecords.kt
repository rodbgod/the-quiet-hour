package br.com.rodsil.quiethour.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/** A day whose ritual reached the end of the pause. [pauseSkipped] flags questions users would not sit with. */
@Entity(tableName = "completed_days")
data class CompletedDay(@PrimaryKey val epochDay: Long, val passageId: String, val pauseSkipped: Boolean)

/** Never leaves the device: no sync, no analytics payload carries this text. */
@Entity(tableName = "reflections")
data class Reflection(@PrimaryKey val epochDay: Long, val passageId: String, val text: String)

@Dao
interface RitualRecordDao {
  @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertCompletedDay(day: CompletedDay)

  @Query("SELECT epochDay FROM completed_days") suspend fun completedEpochDays(): List<Long>

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertReflection(reflection: Reflection)
}
