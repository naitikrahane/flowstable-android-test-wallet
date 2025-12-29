package com.antigravity.cryptowallet.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoApi {
    @GET("simple/price")
    suspend fun getSimplePrice(
        @Query("ids") ids: String,
        @Query("vs_currencies") vsCurrencies: String = "usd"
    ): Map<String, Map<String, Double>>

    @GET("coins/{id}/market_chart")
    suspend fun getCoinMarketChart(
        @retrofit2.http.Path("id") id: String,
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("days") days: String = "7"
    ): com.antigravity.cryptowallet.data.models.MarketChartResponse

    @GET("coins/{id}")
    suspend fun getCoinInfo(
        @retrofit2.http.Path("id") id: String,
        @Query("localization") localization: Boolean = false,
        @Query("tickers") tickers: Boolean = false,
        @Query("market_data") marketData: Boolean = false,
        @Query("community_data") communityData: Boolean = false,
        @Query("developer_data") developerData: Boolean = false,
        @Query("sparkline") sparkline: Boolean = false
    ): com.antigravity.cryptowallet.data.models.CoinInfoResponse
}
