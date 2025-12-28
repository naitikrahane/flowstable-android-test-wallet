package com.antigravity.cryptowallet.data.security

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    private val encryptedPrefs: SharedPreferences
) {
    companion object {
        private const val KEY_MNEMONIC = "wallet_mnemonic"
        private const val KEY_WALLET_CREATED = "wallet_created"
    }

    fun saveMnemonic(mnemonic: String) {
        encryptedPrefs.edit()
            .putString(KEY_MNEMONIC, mnemonic)
            .putBoolean(KEY_WALLET_CREATED, true)
            .apply()
    }

    fun getMnemonic(): String? {
        return encryptedPrefs.getString(KEY_MNEMONIC, null)
    }

    fun hasWallet(): Boolean {
        return encryptedPrefs.getBoolean(KEY_WALLET_CREATED, false)
    }

    fun clearWallet() {
        encryptedPrefs.edit().clear().apply()
    }
}
