package com.antigravity.cryptowallet.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.antigravity.cryptowallet.data.wallet.WalletRepository
import com.antigravity.cryptowallet.ui.components.BrutalistButton
import com.antigravity.cryptowallet.ui.components.BrutalistHeader
import com.antigravity.cryptowallet.ui.components.BrutalistTextField
import com.antigravity.cryptowallet.ui.theme.BrutalWhite
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportWalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {
    fun importWallet(phrase: String): Boolean {
        return walletRepository.importWallet(phrase)
    }
}

@Composable
fun ImportWalletScreen(
    onWalletImported: () -> Unit,
    viewModel: ImportWalletViewModel = hiltViewModel()
) {
    var phrase by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalWhite)
            .padding(16.dp)
    ) {
        BrutalistHeader("Import Wallet")
        
        BrutalistTextField(
            value = phrase,
            onValueChange = { phrase = it },
            placeholder = "Enter 12/24 word seed phrase...",
            singleLine = false,
            modifier = Modifier.height(150.dp)
        )
        
        if (error != null) {
            Text(
                text = error!!,
                color = Color.Black, // Using Black for error as per B&W rule, maybe user all caps to stress it
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        BrutalistButton(
            text = "Import",
            onClick = {
                if (viewModel.importWallet(phrase.trim())) {
                    onWalletImported()
                } else {
                    error = "INVALID SEED PHRASE"
                }
            }
        )
    }
}
