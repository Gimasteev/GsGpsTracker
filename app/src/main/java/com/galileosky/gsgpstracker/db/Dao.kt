package com.galileosky.gsgpstracker.db

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface Dao {
    @Insert
    suspend fun insertTrack(trackItem: TrackItem) // асинхронная функция записи в базу данных
}