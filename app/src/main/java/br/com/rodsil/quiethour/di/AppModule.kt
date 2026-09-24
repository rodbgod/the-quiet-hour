package br.com.rodsil.quiethour.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import br.com.rodsil.quiethour.data.DeliveredPassageDao
import br.com.rodsil.quiethour.data.QuietHourDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
  @Provides
  @Singleton
  fun database(@ApplicationContext context: Context): QuietHourDatabase =
    Room.databaseBuilder(context, QuietHourDatabase::class.java, "quiet-hour.db").build()

  @Provides fun deliveredPassageDao(database: QuietHourDatabase): DeliveredPassageDao = database.deliveredPassageDao()

  @Provides
  @Singleton
  fun preferences(@ApplicationContext context: Context): DataStore<Preferences> =
    PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("settings") }

  @Provides fun clock(): Clock = Clock.systemDefaultZone()
}
