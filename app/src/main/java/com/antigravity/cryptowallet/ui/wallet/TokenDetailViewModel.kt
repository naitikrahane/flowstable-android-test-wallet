package com.antigravity.cryptowallet.ui.wallet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.cryptowallet.data.repository.CoinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TokenDetailViewModel @Inject constructor(
    private val coinRepository: CoinRepository
) : ViewModel() {

    var description by mutableStateOf("Loading...")
        private set
    
    var price by mutableStateOf("Loading...")
        private set

    var graphPoints by mutableStateOf<List<Double>>(emptyList())
        private set

    fun loadTokenData(symbol: String) {
        val id = when(symbol.uppercase()) {
            "ETH" -> "ethereum"
            "BNB" -> "binancecoin"
            "BTC" -> "bitcoin"
            "USDT" -> "tether"
            "USDC" -> "usd-coin"
            "LINK" -> "chainlink"
            "CAKE" -> "pancakeswap-token"
            else -> "ethereum" // Fallback
        }

        viewModelScope.launch {
            try {
                // Fetch Info
                val info = coinRepository.getCoinInfo(id)
                description = info.description.en.take(300) + "..." // Truncate

                // Fetch Chart
                val chart = coinRepository.getMarketChart(id)
                graphPoints = chart.prices.map { it[1] }
                
                // Fetch Price (use last point for now or specialized call)
                val currentPrice = graphPoints.lastOrNull() ?: 0.0
                price = String.format("$%.2f", currentPrice)

            } catch (e: Exception) {
                e.printStackTrace()
                description = "Failed to load info."
                price = "Error"
            }
        }
    }
}
