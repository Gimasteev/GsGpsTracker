package com.galileosky.gsgpstracker.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TrackItem::class], version = 1)
abstract class MainDb: RoomDatabase() {
    companion object{
        @Volatile
        var INSTANSE: MainDb? = null
        fun getDataBase(context: Context): MainDb{
            return INSTANSE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MainDb::class.java,
                    "GpsTracker.db"
                ).build()
                INSTANSE = instance
                return instance
            }
        }
    }
}