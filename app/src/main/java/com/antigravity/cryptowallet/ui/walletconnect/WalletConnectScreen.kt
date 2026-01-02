package com.antigravity.cryptowallet.ui.walletconnect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.cryptowallet.ui.components.BrutalistButton
import com.antigravity.cryptowallet.ui.components.BrutalistHeader
import com.walletconnect.web3.wallet.client.Wallet

@Composable
fun WalletConnectScreen(
    viewModel: WalletConnectViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    var uriInput by remember { mutableStateOf("") }
    val sessionProposal by viewModel.sessionProposals.collectAsState()
    val sessionRequest by viewModel.sessionRequests.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        BrutalistHeader("WalletConnect")
        Spacer(modifier = Modifier.height(24.dp))

        Text("Paste connection URI:", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = uriInput,
            onValueChange = { uriInput = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("wc:...") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        BrutalistButton(
            text = "Connect",
            onClick = { 
                if (uriInput.isNotBlank()) {
                    viewModel.pair(uriInput)
                    uriInput = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Pending Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        if (sessionProposal == null && sessionRequest == null) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No pending requests", color = Color.Gray)
            }
        }
    }

    // Session Proposal Dialog
    sessionProposal?.let { proposal ->
        AlertDialog(
            onDismissRequest = { viewModel.rejectSession(proposal) },
            title = { Text("Connect to ${proposal.name}?") },
            text = { 
                Column {
                    Text("DApp: ${proposal.name}")
                    Text("URL: ${proposal.url}")
                    Text("Description: ${proposal.description}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Chains: ${proposal.requiredNamespaces.values.flatMap { it.chains ?: emptyList() }.joinToString()}")
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.approveSession(proposal) }) {
                    Text("Approve")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.rejectSession(proposal) }) {
                    Text("Reject")
                }
            }
        )
    }

    // Session Request Dialog
    sessionRequest?.let { request ->
        AlertDialog(
            onDismissRequest = { viewModel.rejectRequest(request) },
            title = { Text("Sign Request") },
            text = {
                Column {
                    Text("Method: ${request.request.method}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Params: ${request.request.params}")
                }
            },
            confirmButton = {
                Button(onClick = { 
                    // For demo purposes, we auto-sign/approve with a dummy signature/hash depending on method
                    // In a real app, you'd show transaction details and sign with private key
                    val dummyResult = when (request.request.method) {
                        "personal_sign", "eth_sign" -> "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef" // Dummy signature
                        "eth_sendTransaction" -> "0xhash..." // Dummy tx hash
                        else -> "true"
                    }
                    viewModel.approveRequest(request, dummyResult) 
                }) {
                    Text("Approve")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.rejectRequest(request) }) {
                    Text("Reject")
                }
            }
        )
    }
}
