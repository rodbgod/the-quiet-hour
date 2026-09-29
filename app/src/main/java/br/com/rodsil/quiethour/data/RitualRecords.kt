package br.com.rodsil.quiethour.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** A day whose ritual reached the end of the pause. [pauseSkipped] flags questions users would not sit with. */
@Entity(tableName = "completed_days")
data class CompletedDay(@PrimaryKey val epochDay: Long, val passageId: String, val pauseSkipped: Boolean)

/** Never leaves the device: no sync, no analytics payload carries this text. */
@Entity(tableName = "reflections")
data class Reflection(@PrimaryKey val epochDay: Long, val passageId: String, val text: String)

@Entity(tableName = "favorites") data class Favorite(@PrimaryKey val passageId: String)

data class JournalRow(val epochDay: Long, val passageId: String, val reflection: String?)

@Dao
interface RitualRecordDao {
  @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertCompletedDay(day: CompletedDay)

  @Query("SELECT epochDay FROM completed_days") suspend fun completedEpochDays(): List<Long>

  @Query("SELECT EXISTS(SELECT 1 FROM completed_days WHERE epochDay = :epochDay)")
  suspend fun isCompleted(epochDay: Long): Boolean

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertReflection(reflection: Reflection)

  @Query(
    """
    SELECT completed_days.epochDay, completed_days.passageId, reflections.text AS reflection
    FROM completed_days LEFT JOIN reflections ON reflections.epochDay = completed_days.epochDay
    ORDER BY completed_days.epochDay DESC
    """
  )
  fun journal(): Flow<List<JournalRow>>

  @Query("SELECT passageId FROM favorites") fun favoritePassageIds(): Flow<List<String>>

  @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertFavorite(favorite: Favorite)

  @Query("DELETE FROM favorites WHERE passageId = :passageId") suspend fun deleteFavorite(passageId: String)
}
