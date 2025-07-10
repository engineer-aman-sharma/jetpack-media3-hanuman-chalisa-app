package com.spiritual.hub.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

// Built by @engineer-aman-sharma using Hilt and Jetpack Compose

@HiltViewModel
class ChalisaViewModel @Inject constructor(
    private val app: Application
) : AndroidViewModel(app) {

    // Exposed Compose state to hold and observe Chalisa text for the UI
    var hanumanText by mutableStateOf("Loading...")

    init {
        // Load Chalisa text on ViewModel initialization
        loadChalisa()
    }

    private fun loadChalisa() {
        viewModelScope.launch(Dispatchers.IO) {
            val inputStream = app.assets.open("hanuman.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            hanumanText = reader.readText()
            reader.close()
        }
    }
}