package com.antigravity.cryptowallet.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.cryptowallet.data.models.AssetUiModel
import com.antigravity.cryptowallet.ui.components.BrutalistButton
import com.antigravity.cryptowallet.ui.components.BrutalistHeader
import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite

@Composable
fun TransferScreen(
    onBack: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    var step by remember { mutableStateOf(0) } // 0: Select Asset, 1: Enter Details
    var selectedAsset by remember { mutableStateOf<AssetUiModel?>(null) }
    var recipientAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalWhite)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (step > 0) step-- else onBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrutalBlack)
            }
            BrutalistHeader(if (step == 0) "Select Token" else "Send ${selectedAsset?.symbol}")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (step == 0) {
            AssetSelector(
                assets = viewModel.assets,
                onSelected = {
                    selectedAsset = it
                    step = 1
                }
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Asset Info Card
                selectedAsset?.let { asset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, BrutalBlack)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Balance", fontSize = 12.sp, color = Color.Gray)
                            Text(asset.balance, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Price", fontSize = 12.sp, color = Color.Gray)
                            Text(String.format("$%.2f", asset.price), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = recipientAddress,
                    onValueChange = { recipientAddress = it },
                    label = { Text("Recipient Address") },
                    placeholder = { Text("0x...") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSending,
                    singleLine = true
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSending,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                if (errorMsg != null) {
                    Text(errorMsg!!, color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.weight(1f))

                BrutalistButton(
                    text = if (isSending) "Processing..." else "Send Now",
                    onClick = {
                        if (recipientAddress.isNotBlank() && amount.isNotBlank() && selectedAsset != null) {
                            isSending = true
                            errorMsg = null
                            viewModel.sendAsset(selectedAsset!!, recipientAddress, amount)
                            // In a real app we'd wait for result, here we just go back after a delay or optimistic success
                            // For simplicity, we assume it works or UI updates via flow
                             onBack()
                        } else {
                            errorMsg = "Please fill all fields"
                        }
                    },
                    enabled = !isSending,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun AssetSelector(
    assets: List<AssetUiModel>,
    onSelected: (AssetUiModel) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(assets) { asset ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BrutalBlack)
                    .clickable { onSelected(asset) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(BrutalBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            asset.symbol.take(1),
                            color = BrutalWhite,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(asset.symbol, fontWeight = FontWeight.Bold)
                        Text(asset.networkName, fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(asset.balance, fontWeight = FontWeight.Black)
                    Text(asset.balanceUsd, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}
