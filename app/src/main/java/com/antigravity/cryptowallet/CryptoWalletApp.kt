package com.antigravity.cryptowallet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CryptoWalletApp : Application() {
    @javax.inject.Inject
    lateinit var walletConnectManager: com.antigravity.cryptowallet.data.walletconnect.WalletConnectManager

    override fun onCreate() {
        super.onCreate()
        walletConnectManager.initialize(this)
    }
}
