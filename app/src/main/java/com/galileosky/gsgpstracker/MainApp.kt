package com.galileosky.gsgpstracker

import android.app.Application
import com.galileosky.gsgpstracker.db.MainDb

class MainApp: Application() {
    val database by lazy { MainDb.getDataBase(this) }

}