package com.example.shared

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shared.ui.cashflow.CashflowApp
import com.example.shared.ui.cashflow.CashflowViewModel
import com.example.shared.ui.theme.TakaTrackTheme

@Composable
fun App(viewModel: CashflowViewModel = viewModel { CashflowViewModel() }) {
    val settings by viewModel.settings.collectAsState()
    TakaTrackTheme(
        darkTheme = settings?.isDarkMode ?: isSystemInDarkTheme(),
        colorTheme = "INK"
    ) {
        CashflowApp(viewModel)
    }
}
