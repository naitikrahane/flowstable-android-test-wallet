package com.antigravity.cryptowallet.data.wallet

import com.antigravity.cryptowallet.data.api.CoinGeckoApi
import com.antigravity.cryptowallet.data.blockchain.BlockchainService
import com.antigravity.cryptowallet.data.blockchain.NetworkRepository
import com.antigravity.cryptowallet.data.db.TokenDao
import com.antigravity.cryptowallet.data.db.TokenEntity
import com.antigravity.cryptowallet.data.models.AssetUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetRepository @Inject constructor(
    private val walletRepository: WalletRepository,
    private val networkRepository: NetworkRepository,
    private val blockchainService: BlockchainService,
    private val tokenDao: TokenDao,
    private val coinGeckoApi: CoinGeckoApi,
    private val transactionRepository: TransactionRepository
) {
    private val _assets = MutableSharedFlow<List<AssetUiModel>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val assets = _assets.asSharedFlow()

    suspend fun refreshAssets() = withContext(Dispatchers.IO) {
        if (!walletRepository.isWalletCreated()) return@withContext
        val address = walletRepository.getAddress()

        // 1. Ensure Defaults
        val savedTokens = tokenDao.getAllTokens().first()
        if (savedTokens.isEmpty()) {
            tokenDao.insertToken(TokenEntity(symbol = "USDT", name = "Tether", contractAddress = "0xdac17f958d2ee523a2206206994597c13d831ec7", decimals = 6, chainId = "eth", coingeckoId = "tether"))
        }
        val allTokens = tokenDao.getAllTokens().first()

        // 2. Prepare list of CoinGecko IDs to fetch prices
        val networkIds = networkRepository.networks.map { it.coingeckoId }
        val tokenIds = allTokens.mapNotNull { it.coingeckoId }
        
        val prices = try {
            coinGeckoApi.getSimplePrice(
                ids = (networkIds + tokenIds).joinToString(","),
                vsCurrencies = "usd"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }

        val resultList = mutableListOf<AssetUiModel>()

        // 3. Fetch Native Balances (ETH, BNB, MATIC)
        // For simplicity, we only show ETH, BNB, MATIC native defaults.
        val mainNetworks = listOf("eth", "bsc", "matic")
        
        for (netId in mainNetworks) {
            val net = networkRepository.getNetwork(netId)
            val balance = blockchainService.getBalance(net.rpcUrl, address)
            val ethBalance = BigDecimal(balance).divide(BigDecimal.TEN.pow(18))
            val price = prices[net.coingeckoId]?.get("usd") ?: 0.0
            val balanceUsd = ethBalance.multiply(BigDecimal(price))

            resultList.add(
                AssetUiModel(
                    id = "native-${net.id}",
                    symbol = net.symbol,
                    name = net.name,
                    balance = String.format("%.4f %s", ethBalance, net.symbol),
                    balanceUsd = String.format("$%.2f", balanceUsd),
                    iconUrl = null,
                    networkName = net.name,
                    rawBalance = ethBalance.toDouble(),
                    price = price
                )
            )
        }

        // 4. Fetch Token Balances
        for (token in allTokens) {
            val net = networkRepository.getNetwork(token.chainId)
            if (token.contractAddress != null) {
                val balance = blockchainService.getTokenBalance(net.rpcUrl, token.contractAddress, address)
                val tokenBalance = BigDecimal(balance).divide(BigDecimal.TEN.pow(token.decimals))
                val price = token.coingeckoId?.let { prices[it]?.get("usd") } ?: 0.0
                val balanceUsd = tokenBalance.multiply(BigDecimal(price))

                resultList.add(
                    AssetUiModel(
                        id = "token-${token.id}",
                        symbol = token.symbol,
                        name = token.name,
                        balance = String.format("%.4f %s", tokenBalance, token.symbol),
                        balanceUsd = String.format("$%.2f", balanceUsd),
                        iconUrl = null,
                        networkName = net.name,
                        rawBalance = tokenBalance.toDouble(),
                        price = price
                    )
                )
            }
        }

        _assets.tryEmit(resultList)
    }

    suspend fun addToken(address: String, symbol: String, decimals: Int, chainId: String, name: String) {
        tokenDao.insertToken(
            TokenEntity(
                symbol = symbol,
                name = name,
                contractAddress = address,
                decimals = decimals,
                chainId = chainId,
                isCustom = true
            )
        )
        refreshAssets()
    }

    suspend fun sendAsset(asset: AssetUiModel, toAddress: String, amount: String): String = withContext(Dispatchers.IO) {
        val credentials = walletRepository.activeCredentials ?: throw Exception("Wallet not loaded")
        val amountValue = BigDecimal(amount)
        
        // Find network
        val netId = if (asset.id.startsWith("native-")) asset.id.removePrefix("native-") else {
             // For tokens, we need to find which chain they belong to. 
             // In this simple app, we can extract from metadata or assume.
             // Let's look up the token.
             val tokenId = asset.id.removePrefix("token-").toLongOrNull()
             val token = tokenId?.let { tokenDao.getTokenById(it) }
             token?.chainId ?: "eth"
        }
        val net = networkRepository.getNetwork(netId)
        
        val txHash = if (asset.id.startsWith("native-")) {
            val amountWei = amountValue.multiply(BigDecimal.TEN.pow(18)).toBigInteger()
            blockchainService.sendEth(net.rpcUrl, credentials, toAddress, amountWei)
        } else {
            // Token
            val tokenId = asset.id.removePrefix("token-").toLongOrNull()
            val token = tokenId?.let { tokenDao.getTokenById(it) } ?: throw Exception("Token info not found")
            val amountRaw = amountValue.multiply(BigDecimal.TEN.pow(token.decimals)).toBigInteger()
            blockchainService.sendToken(net.rpcUrl, credentials, token.contractAddress!!, toAddress, amountRaw)
        }
        
        // Add to history
        transactionRepository.addTransaction(
            hash = txHash,
            from = credentials.address,
            to = toAddress,
            value = amount,
            symbol = asset.symbol,
            type = "send",
            status = "success", // Ideally pending then poll, but for now simple
            network = net.name
        )
        
        refreshAssets()
        txHash
    }
}
