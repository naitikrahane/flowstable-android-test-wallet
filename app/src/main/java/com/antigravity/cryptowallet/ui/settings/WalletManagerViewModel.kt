package com.antigravity.cryptowallet.ui.settings

import androidx.lifecycle.ViewModel
import com.antigravity.cryptowallet.data.wallet.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletManagerViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {
    
    val wallets = walletRepository.wallets
    val activeWallet = walletRepository.activeWallet

    init {
        walletRepository.refreshWallets()
    }

    fun switchWallet(walletId: String) {
        androidx.lifecycle.viewModelScope.launch {
            walletRepository.switchWallet(walletId)
        }
    }

    fun deleteWallet(walletId: String) {
        androidx.lifecycle.viewModelScope.launch {
            walletRepository.deleteWallet(walletId)
        }
    }

    fun renameWallet(walletId: String, newName: String) {
        walletRepository.renameWallet(walletId, newName)
    }
}
