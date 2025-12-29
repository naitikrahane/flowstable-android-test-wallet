package com.antigravity.cryptowallet.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.antigravity.cryptowallet.ui.theme.ThemeType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    var currentTheme by mutableStateOf(ThemeType.DEFAULT)
        private set

    fun setTheme(theme: ThemeType) {
        currentTheme = theme
    }
}
