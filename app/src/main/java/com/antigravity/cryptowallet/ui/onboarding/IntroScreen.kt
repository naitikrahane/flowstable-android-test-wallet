package com.antigravity.cryptowallet.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.cryptowallet.ui.components.BrutalistButton
import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite

@Composable
fun IntroScreen(
    onCreateWallet: () -> Unit,
    onImportWallet: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalWhite)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = "NON\nCUSTODIAL\nWALLET",
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 52.sp,
                color = BrutalBlack
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "SECURE.\nPRIVATE.\nEVM COMPATIBLE.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BrutalBlack
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            BrutalistButton(
                text = "Create New Wallet",
                onClick = onCreateWallet
            )
            BrutalistButton(
                text = "Import Wallet",
                onClick = onImportWallet,
                inverted = true
            )
        }
    }
}
