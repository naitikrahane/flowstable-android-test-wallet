package com.antigravity.cryptowallet.ui.walletconnect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.cryptowallet.data.walletconnect.WalletConnectManager
import com.walletconnect.web3.wallet.client.Wallet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WalletConnectViewModel @Inject constructor(
    private val walletConnectManager: WalletConnectManager
) : ViewModel() {

    val sessionProposals = walletConnectManager.sessionProposals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val sessionRequests = walletConnectManager.sessionRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun pair(uri: String) {
        walletConnectManager.pair(uri)
    }

    fun approveSession(proposal: Wallet.Model.SessionProposal) {
        walletConnectManager.approveSession(proposal)
    }

    fun rejectSession(proposal: Wallet.Model.SessionProposal) {
        walletConnectManager.rejectSession(proposal)
    }

    fun approveRequest(request: Wallet.Model.SessionRequest, result: String) {
        walletConnectManager.respondRequest(request.request.id, request.topic, result)
    }

    fun rejectRequest(request: Wallet.Model.SessionRequest) {
        // Implement reject logic if needed, usually responding with error
         // For brevity, we might just ignore or send generic error
    }
}
