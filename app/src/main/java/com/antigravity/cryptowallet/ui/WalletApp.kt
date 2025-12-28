package com.antigravity.cryptowallet.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.antigravity.cryptowallet.ui.onboarding.IntroScreen
import com.antigravity.cryptowallet.ui.wallet.WalletScreen

@Composable
fun WalletApp(startDestination: String = "intro") {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = startDestination) {
        composable("intro") {
            IntroScreen(
                onCreateWallet = { navController.navigate("create_wallet") },
                onImportWallet = { navController.navigate("import_wallet") }
            )
        }
        composable("create_wallet") {
            com.antigravity.cryptowallet.ui.onboarding.CreateWalletScreen(
                onWalletCreated = {
                    navController.navigate("home") {
                        popUpTo("intro") { inclusive = true }
                    }
                }
            )
        }
        composable("import_wallet") {
            com.antigravity.cryptowallet.ui.onboarding.ImportWalletScreen(
                onWalletImported = {
                    navController.navigate("home") {
                        popUpTo("intro") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            com.antigravity.cryptowallet.ui.wallet.WalletScreen()
        }
    }
}
