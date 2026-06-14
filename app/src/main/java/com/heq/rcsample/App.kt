package com.heq.rcsample

import android.app.Application
import heq.v1.common.error.IHEQError
import heq.v1.common.register.HEQSDKInitEvent
import heq.v1.impl.remotecontroller.RemoteControllerSdkBootstrap
import heq.v1.manager.SDKManager
import heq.v1.manager.interfaces.SDKManagerCallback

class App: Application() {

    override fun onCreate() {
        super.onCreate()
    }

}