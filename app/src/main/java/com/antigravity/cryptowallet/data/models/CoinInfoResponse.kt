package com.antigravity.cryptowallet.data.models

import com.google.gson.annotations.SerializedName

data class CoinInfoResponse(
    val id: String,
    val symbol: String,
    val name: String,
    val description: Map<String, String>,
    val links: Links?,
    val platforms: Map<String, String>? = null
)

data class Links(
    val homepage: List<String>?,
    @SerializedName("subreddit_url") val subredditUrl: String?
)
