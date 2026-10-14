package com.daeumgil.app

import android.app.Application

class DaeumgilApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

