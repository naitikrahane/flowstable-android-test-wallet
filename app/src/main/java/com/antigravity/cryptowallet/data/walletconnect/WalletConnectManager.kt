package com.antigravity.cryptowallet.data.walletconnect

import android.app.Application
import android.util.Log
import com.antigravity.cryptowallet.data.wallet.WalletRepository
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletConnectManager @Inject constructor(
    private val walletRepository: WalletRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _sessionProposals = MutableSharedFlow<Wallet.Model.SessionProposal>()
    val sessionProposals = _sessionProposals.asSharedFlow()

    private val _sessionRequests = MutableSharedFlow<Wallet.Model.SessionRequest>()
    val sessionRequests = _sessionRequests.asSharedFlow()

    fun initialize(application: Application) {
        val projectId = "c4f79cc821944d9680842e34466bfb" // Demo Project ID (Replace with real one)
        val serverUrl = "wss://relay.walletconnect.com?projectId=$projectId"
        
        val connectionType = ConnectionType.AUTOMATIC
        val appMetaData = Core.Model.AppMetaData(
            name = "Antigravity Wallet",
            description = "A secure android crypto wallet",
            url = "https://antigravity.com",
            icons = listOf("https://raw.githubusercontent.com/WalletConnect/walletconnect-assets/master/Logo/Blue%20(Default)/Logo.png"),
            redirect = "kotlin-wallet-wc:/request"
        )

        CoreClient.initialize(
            metaData = appMetaData, 
            relayServerUrl = serverUrl, 
            connectionType = connectionType, 
            application = application
        ) { error ->
            Log.e("WalletConnect", "Core Init Error: ${error.throwable.stackTraceToString()}")
        }

        Web3Wallet.initialize(Wallet.Params.Init(core = CoreClient)) { error ->
            Log.e("WalletConnect", "Web3Wallet Init Error: ${error.throwable.stackTraceToString()}")
        }

        val walletDelegate = object : Web3Wallet.WalletDelegate {
            override fun onSessionProposal(sessionProposal: Wallet.Model.SessionProposal, verifyContext: Wallet.Model.VerifyContext) {
                scope.launch { _sessionProposals.emit(sessionProposal) }
            }

            override fun onSessionRequest(sessionRequest: Wallet.Model.SessionRequest, verifyContext: Wallet.Model.VerifyContext) {
                scope.launch { _sessionRequests.emit(sessionRequest) }
            }

            override fun onAuthRequest(authRequest: Wallet.Model.AuthRequest, verifyContext: Wallet.Model.VerifyContext) {}
            override fun onSessionDelete(sessionDelete: Wallet.Model.SessionDelete) {}
            override fun onSessionSettleResponse(settleSessionResponse: Wallet.Model.SettledSessionResponse) {}
            override fun onSessionUpdateResponse(sessionUpdateResponse: Wallet.Model.SessionUpdateResponse) {}
            override fun onConnectionStateChange(state: Wallet.Model.ConnectionState) {}
            override fun onError(error: Wallet.Model.Error) {
                Log.e("WalletConnect", "Delegate Error: ${error.throwable.stackTraceToString()}")
            }
        }
        
        Web3Wallet.setWalletDelegate(walletDelegate)
    }

    fun pair(uri: String) {
        val pairingParams = Core.Params.Pair(uri)
        CoreClient.Pairing.pair(pairingParams) { error ->
            Log.e("WalletConnect", "Pairing Error: ${error.throwable.stackTraceToString()}")
        }
    }

    fun approveSession(proposal: Wallet.Model.SessionProposal) {
        val address = walletRepository.getAddress()
        val namespaces = proposal.requiredNamespaces.mapValues { (_, namespace) ->
            Wallet.Model.Namespace.Session(
                chains = namespace.chains,
                accounts = namespace.chains?.map { "$it:$address" } ?: emptyList(),
                methods = namespace.methods,
                events = namespace.events
            )
        }
        
        val approve = Wallet.Params.SessionApprove(
            proposerPublicKey = proposal.proposerPublicKey,
            namespaces = namespaces
        )
        
        Web3Wallet.approveSession(approve) { error ->
            Log.e("WalletConnect", "Approve Session Error: ${error.throwable.stackTraceToString()}")
        }
    }

    fun rejectSession(proposal: Wallet.Model.SessionProposal) {
        val reject = Wallet.Params.SessionReject(
            proposerPublicKey = proposal.proposerPublicKey,
            reason = "User rejected"
        )
        Web3Wallet.rejectSession(reject) { error ->
             Log.e("WalletConnect", "Reject Session Error: ${error.throwable.stackTraceToString()}")
        }
    }

     fun respondRequest(requestId: Long, topic: String, result: String) {
        val response = Wallet.Params.SessionRequestResponse(
            sessionTopic = topic,
            jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcResult(requestId, result)
        )
        Web3Wallet.respondSessionRequest(response) { error ->
             Log.e("WalletConnect", "Respond Request Error: ${error.throwable.stackTraceToString()}")
        }
    }
}
