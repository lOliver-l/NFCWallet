package com.nfcwallet.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NfcCardEntity::class], version = 2, exportSchema = false)
abstract class NfcDatabase : RoomDatabase() {

    abstract fun nfcCardDao(): NfcCardDao

    companion object {
        @Volatile
        private var INSTANCE: NfcDatabase? = null

        fun getDatabase(context: Context): NfcDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NfcDatabase::class.java,
                    "nfc_wallet_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
