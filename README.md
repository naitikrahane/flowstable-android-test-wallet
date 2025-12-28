# FlowStable Wallet (EVM)

A high-performance, non-custodial EVM wallet for Android built with Jetpack Compose and following strict Brutalist design principles.

## Features
- **Strict Black & White UI**: No colors, gradients, or shadows.
- **Non-Custodial**: Keys generated via BIP-39 and stored encrypted using Android Keystore.
- **Multi-Chain**: Supports Ethereum, Arbitrum, Optimism, Polygon, BSC.
- **Secure**: App-level encryption, screenshot prevention, clipboard protection.
- **Performance**: Parallel RPC calls, aggressive caching.

## Architecture
- **MVVM**: Clean separation of UI and Logic.
- **Hilt**: Dependency Injection.
- **Web3j**: Blockchain interaction.
- **EncryptedSharedPreferences**: Secure storage.

## Threat Model (Summary)
- **Key Extraction**: Mitigated by using Android Keystore (Tee-backed where available). Keys are never stored in plain text.
- **Memory Dump**: Keys are kept in memory only while needed. `SecureStorage` handles persistence securely.
- **Screen Scrapers**: `FLAG_SECURE` prevents screenshots and screen recording.
- **Clipboard Attacks**: Seed phrase is never automatically copied needed. User must manually write it down.
- **Network Attacks**: All RPC communication is over HTTPS. Use of public RPCs entails some privacy leakage (IP, addresses), mitigated by allowing custom RPC URLs in future updates.

## Build Instructions
1. Open in Android Studio Hedgehog or later.
2. Sync Gradle.
3. Build & Run on Emulator/Device (Min SDK 26).
