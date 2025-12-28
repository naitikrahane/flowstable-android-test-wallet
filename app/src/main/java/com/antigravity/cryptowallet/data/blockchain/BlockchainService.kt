package com.antigravity.cryptowallet.data.blockchain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockchainService @Inject constructor() {

    suspend fun getBalance(rpcUrl: String, address: String): BigInteger = withContext(Dispatchers.IO) {
        try {
            val web3j = Web3j.build(HttpService(rpcUrl))
            val ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send()
            ethGetBalance.balance
        } catch (e: Exception) {
            e.printStackTrace()
            BigInteger.ZERO
        }
    }
}
