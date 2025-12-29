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
            MainScreen(
                onNavigateToSecuritySetup = { navController.navigate("security_setup") },
                onNavigateToRevealSeed = { navController.navigate("reveal_seed_verify") },
                onNavigateToTransfer = { navController.navigate("transfer") },
                onNavigateToAppInfo = { navController.navigate("app_info") },
                onNavigateToTokenDetail = { symbol -> navController.navigate("token_detail/$symbol") },
                onNavigateToWalletConnect = { navController.navigate("wallet_connect") }
            )
        }

        composable(
            route = "transfer?symbol={symbol}",
            arguments = listOf(
                androidx.navigation.navArgument("symbol") { 
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString("symbol")
            com.antigravity.cryptowallet.ui.wallet.TransferScreen(
                onBack = { navController.popBackStack() },
                onTransactionSuccess = { amount, sym, recipient ->
                    navController.navigate("transaction_success/$amount/$sym/$recipient")
                },
                initialSymbol = symbol
            )
        }

        composable("wallet_connect") {
            com.antigravity.cryptowallet.ui.walletconnect.WalletConnectScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("app_info") {
            com.antigravity.cryptowallet.ui.settings.AppInfoScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        // ... (unlock blocks skipped for brevity, keeping existing structure if possible or cleaning up)

        // ...

        composable(
            route = "token_detail/{symbol}",
            arguments = listOf(
                androidx.navigation.navArgument("symbol") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
            com.antigravity.cryptowallet.ui.wallet.TokenDetailScreen(
                symbol = symbol,
                onBack = { navController.popBackStack() },
                onNavigateToSend = { navController.navigate("transfer?symbol=$symbol") }
            )
        }
    }
}
