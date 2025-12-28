package com.antigravity.cryptowallet.data.wallet

import com.antigravity.cryptowallet.data.security.SecureStorage
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Credentials
import org.web3j.crypto.MnemonicUtils
import org.web3j.utils.Numeric
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor(
    private val secureStorage: SecureStorage
) {
    // In-memory cache of credentials (cleared on lock)
    var activeCredentials: Credentials? = null
        private set

    fun createWallet(): String {
        val initialEntropy = ByteArray(16)
        SecureRandom().nextBytes(initialEntropy)
        val mnemonic = MnemonicUtils.generateMnemonic(initialEntropy)
        secureStorage.saveMnemonic(mnemonic)
        loadWallet(mnemonic)
        return mnemonic
    }

    fun importWallet(mnemonic: String): Boolean {
        if (!MnemonicUtils.validateMnemonic(mnemonic)) return false
        secureStorage.saveMnemonic(mnemonic)
        loadWallet(mnemonic)
        return true
    }

    fun loadWallet(mnemonic: String? = null) {
        val seedMnemonic = mnemonic ?: secureStorage.getMnemonic() ?: return
        
        // Derive Private Key (BIP-44: m/44'/60'/0'/0/0)
        val seed = MnemonicUtils.generateSeed(seedMnemonic, null)
        val masterKeyPair = Bip32ECKeyPair.generateKeyPair(seed)
        val path = intArrayOf(44 or Bip32ECKeyPair.HARDENED_BIT, 60 or Bip32ECKeyPair.HARDENED_BIT, 0 or Bip32ECKeyPair.HARDENED_BIT, 0, 0)
        val derivedKeyPair = Bip32ECKeyPair.deriveKeyPair(masterKeyPair, path)
        
        activeCredentials = Credentials.create(derivedKeyPair)
    }

    fun getAddress(): String {
        return activeCredentials?.address ?: ""
    }
    
    fun getPrivateKey(): String {
        // Only for viewing, highly sensitive
        return activeCredentials?.ecKeyPair?.privateKey?.toString(16) ?: ""
    }

    fun isWalletCreated(): Boolean = secureStorage.hasWallet()
}
