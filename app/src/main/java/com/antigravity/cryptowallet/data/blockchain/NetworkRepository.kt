package com.antigravity.cryptowallet.data.blockchain

import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

data class Network(
    val id: String,
    val name: String,
    val rpcUrl: String,
    val initialRpc: String, // Keep a default one
    val chainId: Long,
    val symbol: String
)

@Singleton
class NetworkRepository @Inject constructor() {
    val networks = listOf(
        Network("eth", "Ethereum", "https://rpc.ankr.com/eth", "https://rpc.ankr.com/eth", 1, "ETH"),
        Network("arb", "Arbitrum One", "https://arb1.arbitrum.io/rpc", "https://arb1.arbitrum.io/rpc", 42161, "ETH"),
        Network("op", "Optimism", "https://mainnet.optimism.io", "https://mainnet.optimism.io", 10, "ETH"),
        Network("matic", "Polygon", "https://polygon-rpc.com", "https://polygon-rpc.com", 137, "MATIC"),
        Network("bsc", "BNB Chain", "https://bsc-dataseed.binance.org", "https://bsc-dataseed.binance.org", 56, "BNB")
    )
    
    fun getNetwork(id: String) = networks.find { it.id == id } ?: networks.first()
}
