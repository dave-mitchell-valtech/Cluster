package com.example.cluster.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ClusterViewModel : ViewModel() {
    // Game UI state
    private val _uiState = MutableStateFlow(ClusterUiState())
    val uiState: StateFlow<ClusterUiState> = _uiState.asStateFlow()
}
