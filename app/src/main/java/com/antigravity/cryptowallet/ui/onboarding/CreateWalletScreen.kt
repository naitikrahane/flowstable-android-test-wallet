package com.antigravity.cryptowallet.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.cryptowallet.data.wallet.WalletRepository
import com.antigravity.cryptowallet.ui.components.BrutalistButton
import com.antigravity.cryptowallet.ui.components.BrutalistHeader
import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateWalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {
    var mnemonic by mutableStateOf<List<String>>(emptyList())
    var isCreated by mutableStateOf(false)

    fun generateWallet() {
        if (!isCreated) {
            val phrase = walletRepository.createWallet()
            mnemonic = phrase.split(" ")
            isCreated = true
        }
    }
}

@Composable
fun CreateWalletScreen(
    onWalletCreated: () -> Unit,
    viewModel: CreateWalletViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.generateWallet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalWhite)
            .padding(16.dp)
    ) {
        BrutalistHeader("Secret Phase")

        Text(
            text = "WRITE THIS DOWN. IF YOU LOSE IT, YOU LOSE YOUR FUNDS FOREVER.",
            color = BrutalBlack,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(viewModel.mnemonic) { index, word ->
                Box(
                    modifier = Modifier
                        .border(1.dp, BrutalBlack)
                        .padding(12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row {
                        Text(
                            text = "${index + 1}. ",
                            fontWeight = FontWeight.Bold,
                            color = BrutalBlack
                        )
                        Text(
                            text = word,
                            color = BrutalBlack
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BrutalistButton(
            text = "I Have Saved It",
            onClick = onWalletCreated
        )
    }
}
