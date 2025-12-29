package com.antigravity.cryptowallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.antigravity.cryptowallet.ui.WalletApp
import com.antigravity.cryptowallet.ui.theme.CryptoWalletTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : androidx.fragment.app.FragmentActivity() {

    @Inject
    lateinit var secureStorage: com.antigravity.cryptowallet.data.security.SecureStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        

        
        
        val startDestination = if (secureStorage.hasWallet()) {
            if (secureStorage.hasPin()) "unlock" else "security_setup"
        } else {
            "intro"
        }

        setContent {
            val settingsViewModel: com.antigravity.cryptowallet.ui.settings.SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            val currentTheme = settingsViewModel.currentTheme.collectAsState(initial = com.antigravity.cryptowallet.ui.theme.ThemeType.DEFAULT)
            CryptoWalletTheme(themeType = currentTheme.value) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    WalletApp(startDestination = startDestination)
                }
            }
        }
    }
}
