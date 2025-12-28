package com.antigravity.cryptowallet.data.security

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    private val encryptedPrefs: SharedPreferences
) {


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

    // Security Features
    fun savePin(pin: String) {
        encryptedPrefs.edit().putString(KEY_PIN, pin).apply()
    }

    fun getPin(): String? {
        return encryptedPrefs.getString(KEY_PIN, null)
    }

    fun hasPin(): Boolean {
        return encryptedPrefs.contains(KEY_PIN)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(): Boolean {
        return encryptedPrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    companion object {
        private const val KEY_MNEMONIC = "wallet_mnemonic"
        private const val KEY_WALLET_CREATED = "wallet_created"
        private const val KEY_PIN = "wallet_pin"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }
}
