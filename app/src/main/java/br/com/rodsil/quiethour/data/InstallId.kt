package br.com.rodsil.quiethour.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.util.UUID
import javax.inject.Inject

private val INSTALL_ID = stringPreferencesKey("install_id")

class InstallId @Inject constructor(private val preferences: DataStore<Preferences>) {
  suspend fun value(): String =
    checkNotNull(
      preferences.edit { if (INSTALL_ID !in it) it[INSTALL_ID] = UUID.randomUUID().toString() }[INSTALL_ID]
    )
}
