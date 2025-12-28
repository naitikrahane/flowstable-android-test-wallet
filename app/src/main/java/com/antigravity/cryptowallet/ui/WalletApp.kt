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
                onNavigateToSecuritySetup = { navController.navigate("security_setup") }
            )
        }
        
        composable("unlock") {
            val viewModel = androidx.hilt.navigation.compose.hiltViewModel<com.antigravity.cryptowallet.ui.security.SecurityViewModel>()
            com.antigravity.cryptowallet.ui.security.LockScreen(
                mode = com.antigravity.cryptowallet.ui.security.LockMode.UNLOCK,
                onUnlock = {
                    navController.navigate("home") {
                        popUpTo("unlock") { inclusive = true }
                    }
                },
                checkPin = { viewModel.checkPin(it) },
                biometricEnabled = viewModel.isBiometricEnabled()
            )
        }

        composable("security_setup") {
            val viewModel = androidx.hilt.navigation.compose.hiltViewModel<com.antigravity.cryptowallet.ui.security.SecurityViewModel>()
            com.antigravity.cryptowallet.ui.security.LockScreen(
                mode = com.antigravity.cryptowallet.ui.security.LockMode.SETUP,
                onPinSet = { pin ->
                    viewModel.setPin(pin)
                    navController.popBackStack()
                },
                onUnlock = {}, // Not used in setup
                biometricEnabled = false
            )
        }
    }
}
