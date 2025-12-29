package com.antigravity.cryptowallet.data.repository

import com.antigravity.cryptowallet.data.api.CoinGeckoApi
import com.antigravity.cryptowallet.data.models.MarketChartResponse
import com.antigravity.cryptowallet.data.models.CoinInfoResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoinRepository @Inject constructor(
    private val api: CoinGeckoApi
) {
    suspend fun getMarketChart(id: String, days: String = "7"): MarketChartResponse {
        return api.getCoinMarketChart(id, days = days)
    }

    suspend fun getCoinInfo(id: String): CoinInfoResponse {
        return api.getCoinInfo(id)
    }
}
